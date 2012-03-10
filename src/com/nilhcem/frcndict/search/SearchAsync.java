package com.nilhcem.frcndict.search;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

import com.nilhcem.frcndict.core.Config;
import com.nilhcem.frcndict.core.list.AbstractSearchService;
import com.nilhcem.frcndict.core.list.ListAdapter;
import com.nilhcem.frcndict.database.DatabaseHelper;
import com.nilhcem.frcndict.database.Entry;
import com.nilhcem.frcndict.database.Tables;
import com.nilhcem.frcndict.settings.SettingsActivity;

public final class SearchAsync extends AsyncTask<String, String, List<Entry>> {
	private final ListAdapter mRefAdapter;
	private final AbstractSearchService mRefService;
	private final WeakReference<SearchActivity> mRefActivity;

	public SearchAsync(ListAdapter adapter, AbstractSearchService service, SearchActivity activity) {
		super();
		mRefAdapter = adapter;
		mRefService = service;
		mRefActivity = new WeakReference<SearchActivity>(activity);
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
			if (Config.LOG_DEBUG) Log.d(SearchAsync.class.getSimpleName(), "[Query] Started");
			DatabaseHelper db = DatabaseHelper.getInstance();
			db.open();
			Cursor c = search(db, search, mRefService.getSearchType(), currentPage);
			if (Config.LOG_DEBUG) Log.d(SearchAsync.class.getSimpleName(), "[Query] Stopped");

			HashMap<String, Integer> columnsIndexCache = new HashMap<String, Integer>();
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
			db.close();
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

	private Cursor search(DatabaseHelper db, String search, int searchType, int currentPage) {
		if (searchType == AbstractSearchService.SEARCH_STARRED) {
			return db.searchStarred(currentPage);
		}

		String curSearch = search.trim().toLowerCase();
		if (searchType == AbstractSearchService.SEARCH_HANZI) {
			return db.searchHanzi(curSearch, currentPage);
		} else if (searchType == AbstractSearchService.SEARCH_PINYIN) {
			return db.searchPinyin(curSearch, currentPage);
		}
		return db.searchFrench(curSearch, currentPage); // by default
	}
}
