package com.nilhcem.frcndict.search;

import com.nilhcem.frcndict.core.list.AbstractSearchService;
import com.nilhcem.frcndict.database.DatabaseHelper;
import com.nilhcem.frcndict.utils.ChineseCharsHandler;

public final class SearchService extends AbstractSearchService {
	public static final int BACK_TO_EXIT_TIMER = 4000;

	private long lastBackPressTime = 0l;

	public SearchService() {
		searchType = AbstractSearchService.SEARCH_UNDEFINED;
	}

	public void setLastBackPressTime(long value) {
		lastBackPressTime = value;
	}

	public boolean isBackBtnPressedForTheFirstTime() {
		return (lastBackPressTime < System.currentTimeMillis() - SearchService.BACK_TO_EXIT_TIMER);
	}

	@Override
	public void detectAndSetSearchType(String search) {
		if (searchType == AbstractSearchService.SEARCH_UNDEFINED) {
			// Checks if search is in hanzi
			for (char ch : search.toCharArray()) {
				if (ChineseCharsHandler.getInstance().charIsChinese(ch)) {
					searchType = AbstractSearchService.SEARCH_HANZI;
					return ;
				}
			}

			// Determines if search is in pinyin or in french
			if (DatabaseHelper.getInstance().isPinyin(search)) {
				searchType = AbstractSearchService.SEARCH_PINYIN;
			} else {
				searchType = AbstractSearchService.SEARCH_FRENCH;
			}
		}
	}
}
