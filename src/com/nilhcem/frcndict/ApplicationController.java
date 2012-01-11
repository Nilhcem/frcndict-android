package com.nilhcem.frcndict;

import com.nilhcem.frcndict.database.DatabaseHelper;
import com.nilhcem.frcndict.search.SearchService;

import android.app.Application;

// must stay in the root package. see ImportDataService.getAppRootDir()
public final class ApplicationController extends Application {
	// Shared preferences
	public static final String PREFS_NAME = "SharedPrefs";
	public static final String PREFS_DB_PATH = "dbPath";

	// Other preferences
	public static final int NB_ENTRIES_PER_LIST = 20;

	// Services
	private final DatabaseHelper db = DatabaseHelper.getInstance(); // create singleton
	private final SearchService searchDictService = new SearchService();

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		db.close();
	}

	public SearchService getSearchDictService() {
		return searchDictService;
	}
}
