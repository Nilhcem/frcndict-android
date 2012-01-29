package com.nilhcem.frcndict;

import android.app.Application;

import com.nilhcem.frcndict.database.DatabaseHelper;
import com.nilhcem.frcndict.search.SearchService;
import com.nilhcem.frcndict.settings.OnPreferencesChangedListener;
import com.nilhcem.frcndict.utils.ChineseCharsHandler;

// must stay in the root package. See ImportDataService.getAppRootDir()
public final class ApplicationController extends Application {
	// Services
	private final SearchService searchDictService = new SearchService();
	private final OnPreferencesChangedListener onPreferencesChangedListener = new OnPreferencesChangedListener();

	@Override
	public void onCreate() {
		// Set color Array to ChineseCharsHandler to colorize hanzi
		ChineseCharsHandler.getInstance().setColorsArray(getResources().getStringArray(R.array.hanziColors));
		super.onCreate();
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		DatabaseHelper.getInstance().close();
	}

	public SearchService getSearchDictService() {
		return searchDictService;
	}

	public OnPreferencesChangedListener getOnPreferencesChangedListener() {
		return onPreferencesChangedListener;
	}
}
