package com.nilhcem.frcndict.search;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;
import android.os.AsyncTask;

import com.nilhcem.frcndict.core.list.AbstractSearchService;
import com.nilhcem.frcndict.core.list.ListAdapter;
import com.nilhcem.frcndict.database.DatabaseHelper;
import com.nilhcem.frcndict.database.Entry;
import com.nilhcem.frcndict.database.Tables;
import com.nilhcem.frcndict.settings.SettingsActivity;

public final class SearchAsync extends AsyncTask<String, String, List<Entry>> {
	private final ListAdapter refAdapter;
	private final AbstractSearchService refService;
	private final WeakReference<SearchActivity> refActivity;

	public SearchAsync(ListAdapter adapter, AbstractSearchService service, SearchActivity activity) {
		super();
		this.refAdapter = adapter;
		this.refService = service;
		this.refActivity = new WeakReference<SearchActivity>(activity);
	}

	@Override
	protected List<Entry> doInBackground(String... params) {
		List<Entry> entries = new ArrayList<Entry>();

		if (!refAdapter.isSearchOver()) {
			// Get parameters
			int currentPage = (params[0] == null) ? 0 : Integer.parseInt(params[0]);
			String search = params[1];

			// Do the query depending on the searchType
			refService.detectAndSetSearchType(search);
			DatabaseHelper db = DatabaseHelper.getInstance();
			db.open();
			Cursor c = search(db, search, refService.getSearchType(), currentPage);
			if (c.moveToFirst()) {
				do {
					Entry entry = new Entry();
					entry.setId(c.getInt(c.getColumnIndex(Tables.ENTRIES_KEY_ROWID)));
					entry.setSimplified(c.getString(c.getColumnIndex(Tables.ENTRIES_KEY_SIMPLIFIED)));
					entry.setTraditional(c.getString(c.getColumnIndex(Tables.ENTRIES_KEY_TRADITIONAL)));
					entry.setPinyin(c.getString(c.getColumnIndex(Tables.ENTRIES_KEY_PINYIN)));
					entry.setDesc(c.getString(c.getColumnIndex(Tables.ENTRIES_KEY_TRANSLATION)));
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
		refAdapter.removeLoading();
		refAdapter.add(result, stillLeft);
		if (refActivity != null) {
			SearchActivity activity = refActivity.get();
			if (activity != null) {
				activity.changeSearchButtonBackground();
			}
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
