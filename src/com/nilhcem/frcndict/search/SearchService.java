package com.nilhcem.frcndict.search;

import com.nilhcem.frcndict.core.list.AbstractSearchService;
import com.nilhcem.frcndict.database.DatabaseHelper;
import com.nilhcem.frcndict.utils.ChineseCharsHandler;

public final class SearchService extends AbstractSearchService {
	public static final int BACK_TO_EXIT_TIMER = 4000;
	private long mLastBackPressTime = 0l;

	public SearchService() {
		super();
		mSearchType = AbstractSearchService.SEARCH_UNDEFINED;
	}

	public void setLastBackPressTime(long value) {
		mLastBackPressTime = value;
	}

	public boolean isBackBtnPressedForTheFirstTime() {
		return (mLastBackPressTime < System.currentTimeMillis() - SearchService.BACK_TO_EXIT_TIMER);
	}

	@Override
	public void detectAndSetSearchType(String search) {
		if (mSearchType == AbstractSearchService.SEARCH_UNDEFINED) {
			// Checks if search is in hanzi
			for (char ch : search.toCharArray()) {
				if (ChineseCharsHandler.getInstance().charIsChinese(ch)) {
					mSearchType = AbstractSearchService.SEARCH_HANZI;
					return;
				}
			}

			// Determines if search is in pinyin or in french
			DatabaseHelper db = DatabaseHelper.getInstance();
			db.open();
			if (db.isPinyin(search)) {
				mSearchType = AbstractSearchService.SEARCH_PINYIN;
			} else {
				mSearchType = AbstractSearchService.SEARCH_FRENCH;
			}
			db.close();
		}
	}
}
