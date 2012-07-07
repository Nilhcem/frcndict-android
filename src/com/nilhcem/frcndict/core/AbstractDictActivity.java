package com.nilhcem.frcndict.core;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.nilhcem.frcndict.CheckDataActivity;
import com.nilhcem.frcndict.R;
import com.nilhcem.frcndict.database.DatabaseHelper;
import com.nilhcem.frcndict.settings.SettingsActivity;
import com.nilhcem.frcndict.updatedb.ImportActivity;
import com.nilhcem.frcndict.updatedb.ImportUpdateService;
import com.nilhcem.frcndict.updatedb.UpdateActivity;

public abstract class AbstractDictActivity extends Activity {
	protected DatabaseHelper mDb = DatabaseHelper.getInstance();
	protected SharedPreferences mPrefs;

	// should be the first method called, once called, other code should be in a if (!isFinishing()) condition
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!AbstractDictActivity.checkForDatabaseImportOrUpdate(this)) {
			// Get shared preferences
			mPrefs = PreferenceManager.getDefaultSharedPreferences(this);

			// Check if database is initialized and open it (if it is not already opened), otherwise, redirects to the main activity
			if (mDb.getDatabasePath() == null) {
				// redirect to the main activity
				finish();
				Intent intent = new Intent(this, CheckDataActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
				overridePendingTransition(0, 0);
				startActivity(intent);
			}
			AbstractDictActivity.checkForNightModeTheme(this, mPrefs);
		}
	}

	// Check if upgrade service is running, in that case, redirects there
	@Override
	protected void onResume() {
		super.onResume();
		if (!AbstractDictActivity.checkForDatabaseImportOrUpdate(this)) {
			mDb.open();
		}
	}

	@Override
	protected void onPause() {
		mDb.close();
		super.onPause();
	}

	public static boolean checkForDatabaseImportOrUpdate(Activity activity) {
		ImportUpdateService service = ImportUpdateService.getInstance();

		boolean isRunning;
		// If upgrade service is running, redirects to there
		if (service == null || service.getStatus() == ImportUpdateService.STATUS_UNSTARTED) {
			isRunning = false;
		} else {
			if (Config.LOG_DEBUG) Log.d(AbstractDictActivity.class.getSimpleName(), "[Check Import/Update service] Already running. Redirecting.");
			activity.finish();
			Intent intent = new Intent(activity, service.isImport() ? ImportActivity.class : UpdateActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			activity.overridePendingTransition(0, 0);
			activity.startActivity(intent);
			isRunning = true;
		}
		return isRunning;
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
