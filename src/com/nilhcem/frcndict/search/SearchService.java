package com.nilhcem.frcndict.search;

import java.lang.ref.WeakReference;

import com.nilhcem.frcndict.database.DatabaseHelper;
import com.nilhcem.frcndict.utils.ChineseCharsHandler;

public final class SearchService {
	private int searchType = SearchService.SEARCH_UNDEFINED;
	private long lastBackPressTime = 0l;

	// Search Thread
	private SearchAsync lastTask = null;

	// Reference
	private WeakReference<SearchAdapter> refSearchAdapter;

	public static final int BACK_TO_EXIT_TIMER = 4000;

	public static final int SEARCH_UNDEFINED = 0;
	public static final int SEARCH_FRENCH = 1;
	public static final int SEARCH_PINYIN = 2;
	public static final int SEARCH_HANZI = 3;

	public void setAdapter(SearchAdapter searchAdapter) {
		this.refSearchAdapter = new WeakReference<SearchAdapter>(searchAdapter);
	}

	public int getSearchType() {
		return searchType;
	}

	public void setSearchType(int searchType) {
		this.searchType = searchType;
	}

	public void setLastBackPressTime(long value) {
		lastBackPressTime = value;
	}

	public boolean isBackBtnPressedForTheFirstTime() {
		return (lastBackPressTime < System.currentTimeMillis() - SearchService.BACK_TO_EXIT_TIMER);
	}

	public void detectAndSetSearchType(String search) {
		if (searchType == SearchService.SEARCH_UNDEFINED) {
			// Checks if search is in hanzi
			for (char ch : search.toCharArray()) {
				if (ChineseCharsHandler.charIsChinese(ch)) {
					searchType = SearchService.SEARCH_HANZI;
					return ;
				}
			}

			// Determines if search is in pinyin or in french
			if (DatabaseHelper.getInstance().isPinyin(search)) {
				searchType = SearchService.SEARCH_PINYIN;
			} else {
				searchType = SearchService.SEARCH_FRENCH;
			}
		}
	}

	public void stopPreviousThread() {
		if (lastTask != null) {
			lastTask.cancel(true);
			lastTask = null;
		}
	}

	public void runSearchThread(String curPage, String search, SearchActivity activity) { // curPage should be null if first page
		stopPreviousThread();

		if (refSearchAdapter != null && refSearchAdapter.get() != null) {
			lastTask = new SearchAsync(refSearchAdapter.get(), this, activity);
			lastTask.execute(curPage, search);
		}
	}
}
