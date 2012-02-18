package com.nilhcem.frcndict.core.list;

import java.lang.ref.WeakReference;

import com.nilhcem.frcndict.search.SearchActivity;
import com.nilhcem.frcndict.search.SearchAsync;

public abstract class AbstractSearchService {
	public static final int SEARCH_UNDEFINED = 0;
	public static final int SEARCH_FRENCH = 1;
	public static final int SEARCH_PINYIN = 2;
	public static final int SEARCH_HANZI = 3;
	public static final int SEARCH_STARRED = 4;

	protected int searchType;

	// Search Thread
	protected SearchAsync lastTask = null;

	// Reference
	protected WeakReference<ListAdapter> refSearchAdapter;

	public abstract void detectAndSetSearchType(String search);

	public void setAdapter(ListAdapter searchAdapter) {
		this.refSearchAdapter = new WeakReference<ListAdapter>(searchAdapter);
	}

	public int getSearchType() {
		return searchType;
	}

	public void setSearchType(int searchType) {
		this.searchType = searchType;
	}

	public void stopPreviousThread() {
		if (lastTask != null) {
			lastTask.cancel(true);
			lastTask = null;
		}
	}

	public void runSearchThread(SearchActivity activity, String curPage, String search) { // curPage should be null if first page
		stopPreviousThread();

		if (refSearchAdapter != null && refSearchAdapter.get() != null) {
			lastTask = new SearchAsync(refSearchAdapter.get(), this, activity);
			lastTask.execute(curPage, search);
		}
	}
}
