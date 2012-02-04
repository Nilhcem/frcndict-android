package com.nilhcem.frcndict;

import android.app.Application;

import com.nilhcem.frcndict.database.DatabaseHelper;
import com.nilhcem.frcndict.search.SearchService;
import com.nilhcem.frcndict.settings.OnPreferencesChangedListener;
import com.nilhcem.frcndict.utils.ChineseCharsHandler;

// must stay in the root package. See ImportDataService.getAppRootDir()
public final class ApplicationController extends Application {
	// Urls
	public static final String DICT_URL = "http://192.168.1.2/cfdict/dictionary.zip";
	public static final String MD5_URL = "http://192.168.1.2/cfdict/md5sum";
	public static final String VERSION_URL = "http://192.168.1.2/cfdict/version";

	// Notifications
	public static final int NOTIF_IMPORTING = 1;
	public static final int NOTIF_IMPORT_SUCCESS = 2;
	public static final int NOTIF_IMPORT_FAILED = 3;
	public static final int NOTIF_UPDATE_AVAILABLE = 4;

	// Services
	private final SearchService searchDictService = new SearchService();
	private final OnPreferencesChangedListener onPreferencesChangedListener = new OnPreferencesChangedListener();

	@Override
	public void onCreate() {
		// Set color Array to ChineseCharsHandler to color hanzi
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
