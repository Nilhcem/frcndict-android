package com.nilhcem.frcndict.search;

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;
import android.os.AsyncTask;

import com.nilhcem.frcndict.ApplicationController;
import com.nilhcem.frcndict.database.DatabaseHelper;
import com.nilhcem.frcndict.database.Entry;
import com.nilhcem.frcndict.database.Tables;

/* package-private */
final class SearchAsync extends AsyncTask<String, String, List<Entry>> {
	private SearchAdapter refAdapter;
	private SearchService refService;

	SearchAsync(SearchAdapter adapter, SearchService service) {
		this.refAdapter = adapter;
		this.refService = service;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	@Override
	protected List<Entry> doInBackground(String... params) {
		List<Entry> entries = new ArrayList<Entry>();

		if (!refAdapter.searchIsOver()) {
			// Get parameters
			int currentPage = (params[0] == null) ? 0 : Integer.parseInt(params[0]);
			String search = params[1];

			// Do the query depending on the searchType
			refService.detectAndSetSearchType(search);
			Cursor c = search(search, refService.getSearchType(), currentPage);
			if (c.moveToFirst()) {
				do {
					Entry entry = new Entry();
					entry.setId(c.getInt(c.getColumnIndex(Tables.ENTRIES_KEY_ROWID)));
					entry.setSimplified(c.getString(c.getColumnIndex(Tables.ENTRIES_KEY_SIMPLIFIED)));
					entry.setPinyin(c.getString(c.getColumnIndex(Tables.ENTRIES_KEY_PINYIN)));
					entry.setDesc(c.getString(c.getColumnIndex(Tables.ENTRIES_KEY_TRANSLATION)));
					entries.add(entry);
				} while (c.moveToNext() && !isCancelled());
			}
			c.close();
		}
		return entries;
	}

	@Override
	protected void onPostExecute(List<Entry> result) {
		super.onPostExecute(result);
		boolean stillLeft = false;
		if (result.size() > ApplicationController.NB_ENTRIES_PER_LIST) {
			stillLeft = true;
			result.remove(ApplicationController.NB_ENTRIES_PER_LIST); // remove last one, just used to know if there are still some elements left
		}
		refAdapter.removeLoading();
		refAdapter.add(result, stillLeft);
		refService.changeLangButton();
	}

	@Override
	protected void onCancelled() {
		super.onCancelled();
	}

	private Cursor search(String search, int searchType, int currentPage) {
		DatabaseHelper db = DatabaseHelper.getInstance();

		if (searchType == SearchService.SEARCH_HANZI) {
			return db.searchHanzi(search, currentPage);
		} else if (searchType == SearchService.SEARCH_PINYIN) {
			return db.searchPinyin(search, currentPage);
		}
		return db.searchFrench(search, currentPage); // by default
	}
}
