package com.nilhcem.frcndict;

import android.app.Application;

import com.nilhcem.frcndict.search.SearchService;
import com.nilhcem.frcndict.settings.OnPreferencesChangedListener;
import com.nilhcem.frcndict.starred.StarredService;
import com.nilhcem.frcndict.utils.ChineseCharsHandler;

// Must stay in the root package. See FileHandler.getAppRootDir()
public final class ApplicationController extends Application {
	// Services
	private final SearchService mSearchDictService = new SearchService(this);
	private final StarredService mStarredService = new StarredService();
	private final OnPreferencesChangedListener mOnPreferencesChangedListener = new OnPreferencesChangedListener();

	@Override
	public void onCreate() {
		// Fill ChineseCharsHandler's colors array with some colors used later to color hanzi
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
