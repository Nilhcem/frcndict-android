package com.nilhcem.frcndict.search;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.database.Cursor;
import android.os.AsyncTask;

import com.nilhcem.frcndict.core.Log;
import com.nilhcem.frcndict.core.list.AbstractSearchService;
import com.nilhcem.frcndict.core.list.ListAdapter;
import com.nilhcem.frcndict.database.DictDbHelper;
import com.nilhcem.frcndict.database.Entry;
import com.nilhcem.frcndict.database.StarredDbHelper;
import com.nilhcem.frcndict.database.Tables;
import com.nilhcem.frcndict.settings.SettingsActivity;

public final class SearchAsync extends AsyncTask<String, String, List<Entry>> {

	private final ListAdapter mRefAdapter;
	private final DictDbHelper mDatabase;
	private final StarredDbHelper mStarredDb;
	private final AbstractSearchService mRefService;
	private final WeakReference<SearchActivity> mRefActivity;

	public SearchAsync(DictDbHelper db, StarredDbHelper starredDb,
			ListAdapter adapter, AbstractSearchService service, SearchActivity activity) {
		super();
		mDatabase = db;
		mStarredDb = starredDb;
		mRefAdapter = adapter;
		mRefService = service;
		if (activity == null) {
			mRefActivity = null;
		} else {
			mRefActivity = new WeakReference<SearchActivity>(activity);
		}
	}

	@Override
	protected List<Entry> doInBackground(String... params) {
		List<Entry> entries = new ArrayList<Entry>();

		if (!mRefAdapter.isSearchOver()) {
			// Get parameters
			int currentPage = (params[0] == null) ? 0 : Integer.parseInt(params[0]);
			String search = params[1];

			// Do the query depending on the searchType
			mRefService.detectAndSetSearchType(search);
			Log.d(SearchAsync.class.getSimpleName(), "[Query] Started");
			Cursor c = search(search, mRefService.getSearchType(), currentPage);
			Log.d(SearchAsync.class.getSimpleName(), "[Query] Stopped");

			HashMap<String, Integer> columnsIndexCache = new HashMap<String, Integer>();
			if (c != null) {
				if (c.moveToFirst()) {
					fillColumnsIndexCache(columnsIndexCache, c);
					do {
						Entry entry = new Entry();
						entry.setId(c.getInt(columnsIndexCache.get(Tables.ENTRIES_KEY_ROWID)));
						entry.setSimplified(c.getString(columnsIndexCache.get(Tables.ENTRIES_KEY_SIMPLIFIED)));
						entry.setTraditional(c.getString(columnsIndexCache.get(Tables.ENTRIES_KEY_TRADITIONAL)));
						entry.setPinyin(c.getString(columnsIndexCache.get(Tables.ENTRIES_KEY_PINYIN)));
						entry.setDesc(c.getString(columnsIndexCache.get(Tables.ENTRIES_KEY_TRANSLATION)));
						entries.add(entry);
					} while (c.moveToNext() && !isCancelled());
				}
				c.close();
			}
		}
		return entries;
	}

	@Override
	protected void onPostExecute(List<Entry> result) {
		super.onPostExecute(result);
		boolean stillLeft = false;
		if (result.size() > SettingsActivity.NB_ENTRIES_PER_LIST) {
			stillLeft = true;
			result.remove(SettingsActivity.NB_ENTRIES_PER_LIST); // remove last one, just used to know if there are still some elements left
		}
		mRefAdapter.removeLoading();
		mRefAdapter.add(result, stillLeft);
		if (mRefActivity != null) {
			SearchActivity activity = mRefActivity.get();
			if (activity != null) {
				activity.changeSearchButtonBackground();
			}
		}
	}

	private void fillColumnsIndexCache(HashMap<String, Integer> cache, Cursor c) {
		if (cache.isEmpty()) {
			cache.put(Tables.ENTRIES_KEY_ROWID, c.getColumnIndex(Tables.ENTRIES_KEY_ROWID));
			cache.put(Tables.ENTRIES_KEY_SIMPLIFIED, c.getColumnIndex(Tables.ENTRIES_KEY_SIMPLIFIED));
			cache.put(Tables.ENTRIES_KEY_TRADITIONAL, c.getColumnIndex(Tables.ENTRIES_KEY_TRADITIONAL));
			cache.put(Tables.ENTRIES_KEY_PINYIN, c.getColumnIndex(Tables.ENTRIES_KEY_PINYIN));
			cache.put(Tables.ENTRIES_KEY_TRANSLATION, c.getColumnIndex(Tables.ENTRIES_KEY_TRANSLATION));
		}
	}

	private Cursor search(String search, int searchType, int currentPage) {
		if (searchType == AbstractSearchService.SEARCH_STARRED) {
			return mDatabase.searchStarred(mStarredDb, currentPage);
		}

		String curSearch = search.trim().toLowerCase(Locale.getDefault());
		if (searchType == AbstractSearchService.SEARCH_HANZI) {
			return mDatabase.searchHanzi(curSearch, currentPage);
		} else if (searchType == AbstractSearchService.SEARCH_PINYIN) {
			return mDatabase.searchPinyin(curSearch, currentPage);
		}
		return mDatabase.searchFrench(curSearch, currentPage); // by default
	}
}
