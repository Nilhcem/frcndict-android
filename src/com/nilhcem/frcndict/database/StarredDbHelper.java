package com.nilhcem.frcndict.database;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

public final class StarredDbHelper extends SQLiteOpenHelper {
	public static final String STARRED_TABLE_NAME = "starred";
	public static final String STARRED_KEY_SIMPLIFIED = "simplified";
	public static final String STARRED_KEY_DATE = "date";

	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

	private static final String DATABASE_NAME = "starred.db";
	private static final int DATABASE_VERSION = 1;

	private static final String DATABASE_CREATE = "CREATE TABLE `"
			+ STARRED_TABLE_NAME + "` (`" + STARRED_KEY_SIMPLIFIED
			+ "` text not null, `" + STARRED_KEY_DATE + "` text);";

	public StarredDbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DATABASE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

	public List<String> getAllStarred() {
		List<String> starred = new ArrayList<String>();

		SQLiteDatabase db = getReadableDatabase();
		Cursor c = db.query(StarredDbHelper.STARRED_TABLE_NAME,
				new String[] { StarredDbHelper.STARRED_KEY_SIMPLIFIED }, null,
				null, null, null,
				String.format(Locale.US, "%s DESC", StarredDbHelper.STARRED_KEY_DATE));

		if (c != null) {
			while (c.moveToNext()) {
				starred.add(c.getString(c
						.getColumnIndex(StarredDbHelper.STARRED_KEY_SIMPLIFIED)));
			}
			c.close();
		}
		return starred;
	}

	public String getStarredDate(String simplified) {
		String starredDate = null;

		SQLiteDatabase db = getReadableDatabase();
		Cursor c = db.query(StarredDbHelper.STARRED_TABLE_NAME,
				new String[] { StarredDbHelper.STARRED_KEY_DATE },
				String.format(Locale.US, "%s=?", StarredDbHelper.STARRED_KEY_SIMPLIFIED),
				new String[] { simplified }, null, null, null, "1");

		if (c != null) {
			if (c.moveToFirst()) {
				starredDate = c.getString(c
						.getColumnIndex(StarredDbHelper.STARRED_KEY_DATE));
			}
			c.close();
		}

		return starredDate;
	}

	public void setStarredDate(String simplified, Date starredDate) {
		String dateStr = null;

		if (starredDate != null) {
			dateStr = DATE_FORMAT.format(starredDate);
		}
		setStarredDate(simplified, dateStr);
	}

	public void setStarredDate(String simplified, String starredDate) {
		SQLiteDatabase db = getReadableDatabase();

		// Delete
		db.delete(StarredDbHelper.STARRED_TABLE_NAME,
				String.format(Locale.US, "%s=?", StarredDbHelper.STARRED_KEY_SIMPLIFIED),
				new String[] { simplified });

		// Insert
		if (starredDate != null) {
			ContentValues values = new ContentValues();
			values.put(StarredDbHelper.STARRED_KEY_SIMPLIFIED, simplified);
			values.put(StarredDbHelper.STARRED_KEY_DATE, starredDate);
			db.insert(STARRED_TABLE_NAME, null, values);
		}
	}

	public long getNbStarred() {
		StringBuilder query = new StringBuilder("SELECT count(`")
				.append(StarredDbHelper.STARRED_KEY_SIMPLIFIED)
				.append("`) FROM `").append(StarredDbHelper.STARRED_TABLE_NAME)
				.append("`");

		SQLiteDatabase db = getReadableDatabase();
		SQLiteStatement statement = db.compileStatement(query.toString());
		return statement.simpleQueryForLong();
	}

	public Cursor getAllStarredCursor() {
		SQLiteDatabase db = getReadableDatabase();
		return db.query(StarredDbHelper.STARRED_TABLE_NAME, new String[] {
				StarredDbHelper.STARRED_KEY_SIMPLIFIED,
				StarredDbHelper.STARRED_KEY_DATE }, null, null, null, null,
				null);
	}
}
