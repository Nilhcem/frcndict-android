package com.nilhcem.frcndict;

import java.io.File;
import java.util.Calendar;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.nilhcem.frcndict.core.AbstractDictActivity;
import com.nilhcem.frcndict.core.Config;
import com.nilhcem.frcndict.database.DatabaseHelper;
import com.nilhcem.frcndict.database.Tables;
import com.nilhcem.frcndict.search.SearchActivity;
import com.nilhcem.frcndict.settings.SettingsActivity;
import com.nilhcem.frcndict.updatedb.CheckForUpdatesService;
import com.nilhcem.frcndict.updatedb.ImportActivity;
import com.nilhcem.frcndict.updatedb.ImportUpdateService;
import com.nilhcem.frcndict.updatedb.UpdateActivity;

/**
 * Checks if database exists.
 * If yes, redirects to the main panel activity
 * If no, launches the import data activity
 */
public final class CheckDataActivity extends Activity {
	private static final long NB_MILLISEC_IN_A_DAY = 86400000l; // 24 * 60 * 60 * 1000

	@Override
	protected void onResume() {
		super.onResume();
		checkDatabase();
	}

	/**
	 * Checks if database exists and contains data. If yes, redirects to the main activity, otherwise, launches the import data activity.
	 */
	private void checkDatabase() {
		Intent intent;

		// Check if an import/update is currently in progress
		if (!AbstractDictActivity.checkForDatabaseImportOrUpdate(this)) {
			SharedPreferences prefs = getSharedPreferences(SettingsActivity.PREFS_NAME, MODE_PRIVATE);
			String dbPath = prefs.getString(SettingsActivity.KEY_DB_PATH, null);

			boolean importDatabase = true;
			if (dbPath != null) {
				DatabaseHelper dbHelper = DatabaseHelper.getInstance();
				dbHelper.setDatabasePath(new File(dbPath));
				importDatabase ^= dbHelper.isInitialized();
			}

			if (importDatabase) {
				if (Config.LOG_DEBUG) Log.d(CheckDataActivity.class.getSimpleName(), "[Check Database] Not available. Redirect to Import.");
				intent = new Intent(this, ImportActivity.class);
			} else {
				if (Config.LOG_DEBUG) Log.d(CheckDataActivity.class.getSimpleName(), "[Check Database] Available. Redirect to Search.");
				if (isDatabaseCompatible()) {
					checkForUpdates(prefs);
					intent = new Intent(this, SearchActivity.class);
				} else {
					intent = new Intent(this, UpdateActivity.class);
				}
			}
			intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			startActivity(intent);
		}
	}

	// Checks if current program version is compatible with installed database
	private boolean isDatabaseCompatible() {
		boolean isCompatible = true;

		DatabaseHelper db = DatabaseHelper.getInstance();
		db.open();
		int installedDbVersion = Integer.parseInt(db.getDbVersion().split(DatabaseHelper.VERSION_SEPARATOR)[1]);
		db.close();

		// Force a mandatory update if previous database is not compatible with this program version
		if (installedDbVersion != Tables.DATABASE_VERSION) {
			if (Config.LOG_INFO) Log.i(CheckDataActivity.class.getSimpleName(), "[Check Database] Database is not compatible. Redirect to Import.");
			isCompatible = false;
		}
		return isCompatible;
	}

	private void checkForUpdates(SharedPreferences prefs) {
		//If no update currently
		if (ImportUpdateService.getInstance() == null
				|| ImportUpdateService.getInstance().getStatus() == ImportUpdateService.STATUS_UNSTARTED) {

			boolean checkForUpdates = PreferenceManager.getDefaultSharedPreferences(getBaseContext())
					.getBoolean(SettingsActivity.KEY_DATABASE_UPDATES, false);

			if (checkForUpdates) {
				long curDate = getCurDateInMillisWithoutTimeValue();
				boolean startUpdate = checkIfUpdateServiceShouldBeStarted(prefs, curDate);

				// Set new last update checked
				SharedPreferences.Editor editor = prefs.edit();
				editor.putLong(SettingsActivity.KEY_LAST_UPDATE_CHECKED, curDate);
				editor.commit();

				if (startUpdate) {
					startService(new Intent(CheckDataActivity.this, CheckForUpdatesService.class));
				}
			}
		}
	}

	private long getCurDateInMillisWithoutTimeValue() {
		Calendar today = Calendar.getInstance();
		today.set(Calendar.HOUR, 0);
		today.set(Calendar.MINUTE, 0);
		today.set(Calendar.SECOND, 0);
		today.set(Calendar.MILLISECOND, 0);

		return today.getTimeInMillis();
	}

	private boolean checkIfUpdateServiceShouldBeStarted(SharedPreferences prefs, long curDate) {
		boolean startUpdate;
		long lastTimeChecked = prefs.getLong(SettingsActivity.KEY_LAST_UPDATE_CHECKED, 0l);

		if (lastTimeChecked == 0l) { // database has never been checked
			startUpdate = true;
		} else {
			long daysBetween = (curDate - lastTimeChecked) / CheckDataActivity.NB_MILLISEC_IN_A_DAY;
			startUpdate = (daysBetween > Config.CHECK_FOR_UPDATES_INTERVAL);
		}
		return startUpdate;
	}
}
