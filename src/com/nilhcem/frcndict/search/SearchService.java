package com.nilhcem.frcndict.search;

import java.lang.ref.WeakReference;

import android.view.View;

import com.nilhcem.frcndict.R;
import com.nilhcem.frcndict.database.DatabaseHelper;
import com.nilhcem.frcndict.utils.ChineseCharsHandler;

public final class SearchService {
	private int searchType = SearchService.SEARCH_UNDEFINED;
	private long lastBackPressTime = 0l;

	// Lang button
	private int langBtnResId = 0;
	private int langBtnVisibility = View.GONE;

	// Search Thread
	private SearchAsync lastTask = null;

	// References
	private WeakReference<SearchAdapter> refSearchAdapter;
	private WeakReference<SearchActivity> refSearchActivity;

	public static final int BACK_TO_EXIT_TIMER = 4000;

	public static final int SEARCH_UNDEFINED = 0;
	public static final int SEARCH_FRENCH = 1;
	public static final int SEARCH_PINYIN = 2;
	public static final int SEARCH_HANZI = 3;

	public void setAdapter(SearchAdapter searchAdapter) {
		this.refSearchAdapter = new WeakReference<SearchAdapter>(searchAdapter);
	}

	public void setActivity(SearchActivity searchActivity) {
		this.refSearchActivity = new WeakReference<SearchActivity>(searchActivity);
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

	public void runSearchThread(String curPage, String search) { // curPage should be null if first page
		stopPreviousThread();

		if (refSearchAdapter != null && refSearchAdapter.get() != null) {
			lastTask = new SearchAsync(refSearchAdapter.get(), this);
			lastTask.execute(curPage, search);
		}
	}

	public void changeLangButton() {
		if (searchType == SearchService.SEARCH_PINYIN) {
			langBtnResId = R.string.search_btn_pinyin;
			langBtnVisibility = View.VISIBLE;
		} else if (searchType == SearchService.SEARCH_FRENCH) {
			langBtnResId = R.string.search_btn_french;
			langBtnVisibility = View.VISIBLE;
		} else {
			langBtnResId = 0;
			langBtnVisibility = View.GONE;
		}

		if (refSearchActivity != null && refSearchActivity.get() != null) {
			refSearchActivity.get().changeButtonLangState();
		}
	}

	public int getLangBtnVisibility() {
		return langBtnVisibility;
	}
	public void setLangBtnVisibility(int visibility) {
		langBtnVisibility = visibility;
	}

	public int getLangBtnResId() {
		return langBtnResId;
	}
}
