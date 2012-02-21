package com.nilhcem.frcndict.search;

import android.util.Log;

import com.nilhcem.frcndict.core.Config;
import com.nilhcem.frcndict.core.list.AbstractSearchService;
import com.nilhcem.frcndict.database.DatabaseHelper;
import com.nilhcem.frcndict.utils.ChineseCharsHandler;

public final class SearchService extends AbstractSearchService {
	public static final int BACK_TO_EXIT_TIMER = 4000;
	private static final String TAG = "SearchService";
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
			boolean isHanzi = false;
			for (char ch : search.toCharArray()) {
				if (ChineseCharsHandler.getInstance().charIsChinese(ch)) {
					mSearchType = AbstractSearchService.SEARCH_HANZI;
					isHanzi = true;
					break;
				}
			}

			if (!isHanzi) {
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
			if (Config.LOG_DEBUG) {
				Log.d(SearchService.TAG, "[Search type] Detected: "
						+ (mSearchType == AbstractSearchService.SEARCH_HANZI ? "Hanzi"
								: (mSearchType == AbstractSearchService.SEARCH_PINYIN ? "Pinyin"
										: (mSearchType == AbstractSearchService.SEARCH_FRENCH ? "French" : "Undefined"))));
			}
		}
	}
}
