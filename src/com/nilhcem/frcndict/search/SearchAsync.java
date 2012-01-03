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
	private SearchAdapter adapter;

	SearchAsync(SearchAdapter adapter) {
		this.adapter = adapter;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	@Override
	protected List<Entry> doInBackground(String... params) {
		List<Entry> entries = new ArrayList<Entry>();

		if (!adapter.searchIsOver()) {
			int currentPage;
			if (params[0] == null) {
				currentPage = 0;
			} else {
				currentPage = Integer.parseInt(params[0]);
			}
			String search = params[1];
			Cursor c = DatabaseHelper.getInstance().searchDesc(search, currentPage);

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
		adapter.removeLoading();
		adapter.add(result, stillLeft);
	}

	@Override
	protected void onCancelled() {
		super.onCancelled();
	}
}
