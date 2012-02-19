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

	protected int mSearchType;

	// Search Thread
	protected SearchAsync mLastTask = null;

	// Reference
	protected WeakReference<ListAdapter> mRefSearchAdapter;

	public abstract void detectAndSetSearchType(String search);

	public void setAdapter(ListAdapter searchAdapter) {
		mRefSearchAdapter = new WeakReference<ListAdapter>(searchAdapter);
	}

	public int getSearchType() {
		return mSearchType;
	}

	public void setSearchType(int searchType) {
		mSearchType = searchType;
	}

	public void stopPreviousThread() {
		if (mLastTask != null) {
			mLastTask.cancel(true);
			mLastTask = null;
		}
	}

	public void runSearchThread(SearchActivity activity, String curPage, String search) { // curPage should be null if first page
		stopPreviousThread();

		if (mRefSearchAdapter != null && mRefSearchAdapter.get() != null) {
			mLastTask = new SearchAsync(mRefSearchAdapter.get(), this, activity);
			mLastTask.execute(curPage, search);
		}
	}
}
