package com.nilhcem.frcndict;

import android.app.Application;

import com.nilhcem.frcndict.database.DatabaseHelper;
import com.nilhcem.frcndict.search.SearchService;

// must stay in the root package. See ImportDataService.getAppRootDir()
public final class ApplicationController extends Application {
	// Shared preferences
	public static final String PREFS_NAME = "SharedPrefs";
	public static final String PREFS_DB_PATH = "dbPath";

	// Other preferences
	public static final int NB_ENTRIES_PER_LIST = 20;

	// Services
	private final SearchService searchDictService = new SearchService();

	@Override
	public void onTerminate() {
		super.onTerminate();
		DatabaseHelper.getInstance().close();
	}

	public SearchService getSearchDictService() {
		return searchDictService;
	}
}
