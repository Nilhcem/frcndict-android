package com.nilhcem.frcndict.core.list;

import java.lang.ref.WeakReference;

import com.nilhcem.frcndict.database.DictDbHelper;
import com.nilhcem.frcndict.database.StarredDbHelper;
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
		if (searchAdapter == null) {
			mRefSearchAdapter = null;
		} else {
			mRefSearchAdapter = new WeakReference<ListAdapter>(searchAdapter);
		}
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

	public void runSearchThread(DictDbHelper db, StarredDbHelper mStarredDb, SearchActivity activity, String curPage, String search) { // curPage should be null if first page
		stopPreviousThread();

		ListAdapter adapter = null;
		if (mRefSearchAdapter != null) {
			adapter = mRefSearchAdapter.get();
		}
		if (adapter != null) {
			mLastTask = new SearchAsync(db, mStarredDb, adapter, this, activity);
			mLastTask.execute(curPage, search);
		}
	}
}
