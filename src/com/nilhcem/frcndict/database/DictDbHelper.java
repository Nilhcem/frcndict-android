package com.nilhcem.frcndict.database;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.nilhcem.frcndict.settings.SettingsActivity;
import com.nilhcem.frcndict.utils.ChineseCharsHandler;
import com.nilhcem.frcndict.utils.FileHandler;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

public final class DictDbHelper extends SQLiteAssetHelper {
	private static final String DATABASE_NAME = "cfdict";
	private static final int DATABASE_VERSION = 3;

	private static final String QUERY_IS_PINYIN;
	private static final String QUERY_GET_ID_BY_HANZI;
	private static final String QUERY_HANZI;
	private static final String QUERY_PINYIN;
	private static final String QUERY_FRENCH;
	private static final String QUERY_FRENCH_NO_ACCENT;
	private static final String QUERY_STARRED;
	private static final String QUERY_FIND_BY_ID;

	private static final String REGEX_ACCENT = ".*[àâäçéèêëîïôöùûüæœÀÂÄÇÉÈÊËÎÏÔÖÙÛÜÆŒ].*";

	static {
		StringBuilder sb;

		// Query for detecting if input is pinyin or french
		sb = new StringBuilder("SELECT `")
			.append(Tables.ENTRIES_KEY_ROWID)
			.append("` FROM `")
			.append(Tables.ENTRIES_TABLE_NAME)
			.append("` WHERE `")
			.append(Tables.ENTRIES_KEY_PINYIN2)
			.append("` GLOB '%s*'");
		QUERY_IS_PINYIN = sb.toString();

		// Query for getting id by Hanzi
		sb = new StringBuilder("SELECT `")
			.append(Tables.ENTRIES_KEY_ROWID)
			.append("` FROM `")
			.append(Tables.ENTRIES_TABLE_NAME)
			.append("` WHERE `")
			.append(Tables.ENTRIES_KEY_SIMPLIFIED)
			.append("` = '%s' OR `")
			.append(Tables.ENTRIES_KEY_TRADITIONAL)
			.append("` = '%s' ORDER BY `")
			.append(Tables.ENTRIES_KEY_ROWID)
			.append("` ASC LIMIT 1");
		QUERY_GET_ID_BY_HANZI = sb.toString();

		// add 1 entry we won't display but which is just to know if there are still some elements after.
		String nbToDisplay = Integer.toString(SettingsActivity.NB_ENTRIES_PER_LIST + 1);
		StringBuilder selectAllFromWhere = new StringBuilder("SELECT `")
			.append(Tables.ENTRIES_KEY_ROWID).append("`, `")
			.append(Tables.ENTRIES_KEY_SIMPLIFIED).append("`, `")
			.append(Tables.ENTRIES_KEY_TRADITIONAL).append("`, `")
			.append(Tables.ENTRIES_KEY_PINYIN).append("`, `")
			.append(Tables.ENTRIES_KEY_TRANSLATION).append("` FROM `")
			.append(Tables.ENTRIES_TABLE_NAME).append("` WHERE ");

		// Hanzi query
		sb = new StringBuilder(selectAllFromWhere)
			.append("(`")
			.append(Tables.ENTRIES_KEY_SIMPLIFIED)
			.append("` LIKE ? OR `")
			.append(Tables.ENTRIES_KEY_TRADITIONAL)
			.append("` LIKE ?) ORDER BY length(`")
			.append(Tables.ENTRIES_KEY_SIMPLIFIED)
			.append("`) ASC, `")
			.append(Tables.ENTRIES_KEY_SIMPLIFIED)
			.append("` ASC LIMIT ?,")
			.append(nbToDisplay);
		QUERY_HANZI = sb.toString();

		// Pinyin query
		sb = new StringBuilder(selectAllFromWhere)
			.append("`")
			.append(Tables.ENTRIES_KEY_PINYIN2)
			.append("` LIKE ? AND lower(`")
			.append(Tables.ENTRIES_KEY_PINYIN)
			.append("`) LIKE ? ORDER BY length(`")
			.append(Tables.ENTRIES_KEY_PINYIN)
			.append("`) ASC, `")
			.append(Tables.ENTRIES_KEY_PINYIN2)
			.append("` ASC LIMIT ?,")
			.append(nbToDisplay);
		QUERY_PINYIN = sb.toString();

		// French query
		sb = new StringBuilder("'/' || lower(`")
			.append(Tables.ENTRIES_KEY_TRANSLATION)
			.append("`) LIKE ? ORDER BY `")
			.append(Tables.ENTRIES_KEY_TRANS_AVG_LENGTH)
			.append("` ASC, `")
			.append(Tables.ENTRIES_KEY_ROWID)
			.append("` ASC LIMIT ?,")
			.append(nbToDisplay);
		QUERY_FRENCH = new StringBuilder(selectAllFromWhere).append(sb).toString();
		QUERY_FRENCH_NO_ACCENT = new StringBuilder(selectAllFromWhere)
			.append(sb.toString().replaceAll(Tables.ENTRIES_KEY_TRANSLATION,
					Tables.ENTRIES_KEY_TRANS_NO_ACCENT)).toString();

		// Starred query
		sb = new StringBuilder(selectAllFromWhere)
			.append(" `").append(Tables.ENTRIES_KEY_SIMPLIFIED).append("` IN (%s) LIMIT ?,")
			.append(nbToDisplay);
		QUERY_STARRED = sb.toString();

		// Find by id
		sb = new StringBuilder("SELECT `")
			.append(Tables.ENTRIES_KEY_SIMPLIFIED).append("`, `")
			.append(Tables.ENTRIES_KEY_TRADITIONAL).append("`, `")
			.append(Tables.ENTRIES_KEY_PINYIN).append("`, `")
			.append(Tables.ENTRIES_KEY_TRANSLATION).append("` FROM `")
			.append(Tables.ENTRIES_TABLE_NAME)
			.append("` WHERE `").append(Tables.ENTRIES_KEY_ROWID).append("`=? LIMIT 1");
		QUERY_FIND_BY_ID = sb.toString();
	}

	public DictDbHelper(Context context) {
		super(context, DATABASE_NAME, FileHandler.getDbStorageDirectory(context), null, DATABASE_VERSION);
		setForcedUpgradeVersion(DATABASE_VERSION);
	}

	public Map<String, String> findById(int id, StarredDbHelper starredHelper) {
		Map<String, String> found = null;
		SQLiteDatabase db = getReadableDatabase();
		Cursor c = db.rawQuery(DictDbHelper.QUERY_FIND_BY_ID, new String[] { Integer.toString(id) });

		if (c != null && c.getCount() == 1 && c.moveToFirst()) {
			found = new HashMap<String, String>();

			// Get info from dict database
			final String[] keys = new String[] {
				Tables.ENTRIES_KEY_SIMPLIFIED,
				Tables.ENTRIES_KEY_TRADITIONAL,
				Tables.ENTRIES_KEY_PINYIN,
				Tables.ENTRIES_KEY_TRANSLATION
			};
			for (String key : keys) {
				found.put(key, c.getString(c.getColumnIndex(key)));
			}

			// Get starred date from starred database
			found.put(StarredDbHelper.STARRED_KEY_DATE,
					starredHelper.getStarredDate(found.get(Tables.ENTRIES_KEY_SIMPLIFIED)));
		}
		c.close();

		return found;
	}

	public int getIdByHanzi(String hanzi) {
		int id = 0;
		SQLiteDatabase db = getReadableDatabase();
		Cursor c = db.rawQuery(String.format(Locale.US, DictDbHelper.QUERY_GET_ID_BY_HANZI, hanzi, hanzi), null);
		if (c.moveToFirst()) {
			id = c.getInt(c.getColumnIndex(Tables.ENTRIES_KEY_ROWID));
		}
		c.close();
		return id;
	}

	public Cursor searchHanzi(String search, Integer curPage) {
		String criteria = String.format(Locale.US, "%%%s%%", search);
		SQLiteDatabase db = getReadableDatabase();
		return db.rawQuery(DictDbHelper.QUERY_HANZI,
			new String[] {criteria, criteria,
				Integer.toString(SettingsActivity.NB_ENTRIES_PER_LIST * curPage)
			});
	}

	public Cursor searchPinyin(String search, Integer curPage) {
		String curSearch = ChineseCharsHandler.getInstance().pinyinTonesToNb(search);
		SQLiteDatabase db = getReadableDatabase();
		return db.rawQuery(DictDbHelper.QUERY_PINYIN,
			new String[] {
				String.format(Locale.US, "%%%s%%", curSearch.replaceAll("[^a-zA-Z]", "")),
				convertToQueryReadyPinyin(curSearch),
				Integer.toString(SettingsActivity.NB_ENTRIES_PER_LIST * curPage)
			});
	}

	public Cursor searchFrench(String search, Integer curPage) {
		// Detect query (with or without accents)
		String query;
		if (Pattern.matches(DictDbHelper.REGEX_ACCENT, search)) {
			query = DictDbHelper.QUERY_FRENCH;
		} else {
			query = DictDbHelper.QUERY_FRENCH_NO_ACCENT;
		}

		SQLiteDatabase db = getReadableDatabase();
		return db.rawQuery(query, new String[] {String.format(Locale.US, "%%/%s%%", search),
			Integer.toString(SettingsActivity.NB_ENTRIES_PER_LIST * curPage)
		});
	}

	public Cursor searchStarred(StarredDbHelper starredDb, Integer curPage) {
		List<String> starred = starredDb.getAllStarred();
		int size = starred.size();

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < size; i++) {
			if (i != 0) {
				sb.append(",");
			}
			sb.append("?");
		}

		starred.add(Integer.toString(SettingsActivity.NB_ENTRIES_PER_LIST * curPage));
		SQLiteDatabase db = getReadableDatabase();
		return db.rawQuery(String.format(Locale.US, DictDbHelper.QUERY_STARRED, sb.toString()),
				starred.toArray(new String[size + 1]));
	}

	// Checks if search is a pinyin or a french search
	public boolean isPinyin(String search) {
		boolean isPinyin = false;

		String formattedSearch = ChineseCharsHandler.getInstance().pinyinTonesToNb(search).replaceAll("[^a-zA-Z]", "");
		if (!TextUtils.isEmpty(formattedSearch.trim())) {
			SQLiteDatabase db = getReadableDatabase();
			Cursor c = db.rawQuery(String.format(Locale.US, DictDbHelper.QUERY_IS_PINYIN, formattedSearch), null);
			isPinyin = (c.getCount() > 0);
			c.close();
		}
		return isPinyin;
	}

	private String convertToQueryReadyPinyin(String pinyin) {
		boolean prevCharWasSpace = false;
		boolean prevCharWasTone = false;

		StringBuilder newPinyin = new StringBuilder();

		for (char ch : pinyin.toCharArray()) {
			if (ch == ' ' && prevCharWasSpace) {
				continue;
			}

			if (ch == ' ') {
				prevCharWasSpace = true;
				if (!prevCharWasTone) {
					newPinyin.append("%");  // No % for a character before a space (ex "%n%i3 %", not "%n%i3% %")
				}
			} else {
				if (ch != ':' && (ch < '1' || ch > '5')) { // No % for a character before a tone (ex "%h%a%o3%", not "%h%a%o%3%")
					newPinyin.append("%");
				}
				prevCharWasSpace = false;
			}
			newPinyin.append(ch);
		}
		newPinyin.append("%");
		return newPinyin.toString();
	}
}
