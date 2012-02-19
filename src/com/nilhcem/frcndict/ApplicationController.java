package com.nilhcem.frcndict;

import android.app.Application;

import com.nilhcem.frcndict.search.SearchService;
import com.nilhcem.frcndict.settings.OnPreferencesChangedListener;
import com.nilhcem.frcndict.starred.StarredService;
import com.nilhcem.frcndict.utils.ChineseCharsHandler;

// must stay in the root package. See ImportDataService.getAppRootDir()
public final class ApplicationController extends Application {
	// Dictionary URL
	public static final String DICT_URL = "http://192.168.1.2/cfdict/";

	// Notifications
	public static final int NOTIF_IMPORTING = 1;
	public static final int NOTIF_IMPORT_SUCCESS = 2;
	public static final int NOTIF_IMPORT_FAILED = 3;
	public static final int NOTIF_UPDATE_AVAILABLE = 4;

	// Services
	private final SearchService mSearchDictService = new SearchService();
	private final StarredService mStarredService = new StarredService();
	private final OnPreferencesChangedListener mOnPreferencesChangedListener = new OnPreferencesChangedListener();

	@Override
	public void onCreate() {
		// Set color Array to ChineseCharsHandler to color hanzi
		ChineseCharsHandler.getInstance().setColorsArray(getResources().getStringArray(R.array.hanziColors));
		super.onCreate();
	}

	public SearchService getSearchDictService() {
		return mSearchDictService;
	}

	public StarredService getStarredService() {
		return mStarredService;
	}

	public OnPreferencesChangedListener getOnPreferencesChangedListener() {
		return mOnPreferencesChangedListener;
	}
}
