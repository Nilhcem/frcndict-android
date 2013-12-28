package com.nilhcem.frcndict.core;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.nilhcem.frcndict.R;
import com.nilhcem.frcndict.database.DictDbHelper;
import com.nilhcem.frcndict.database.StarredDbHelper;
import com.nilhcem.frcndict.settings.SettingsActivity;

public abstract class AbstractDictActivity extends Activity {

	protected DictDbHelper mDb;
	protected StarredDbHelper mStarredDb;
	protected SharedPreferences mPrefs;

	// should be the first method called by the activities.
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mDb = new DictDbHelper(this);
		mStarredDb = new StarredDbHelper(this);
		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		AbstractDictActivity.checkForNightModeTheme(this, mPrefs);
	}

	@Override
	protected void onDestroy() {
		mDb.close();
		mStarredDb.close();
		super.onDestroy();
	}

	public static void checkForNightModeTheme(Activity activity, SharedPreferences prefs) {
		SharedPreferences curPrefs = prefs;

		// Display night mode theme if set by user.
		if (curPrefs == null) {
			curPrefs = PreferenceManager.getDefaultSharedPreferences(activity);
		}
		if (curPrefs.getBoolean(SettingsActivity.KEY_DARK_THEME, false)) {
			activity.setTheme(R.style.DarkTheme);
		}
	}
}
