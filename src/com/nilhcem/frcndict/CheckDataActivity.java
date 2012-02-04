package com.nilhcem.frcndict;

import java.io.File;
import java.util.Calendar;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.nilhcem.frcndict.core.AbstractDictActivity;
import com.nilhcem.frcndict.database.DatabaseHelper;
import com.nilhcem.frcndict.search.SearchActivity;
import com.nilhcem.frcndict.settings.SettingsActivity;
import com.nilhcem.frcndict.updatedb.CheckForUpdatesService;
import com.nilhcem.frcndict.updatedb.ImportActivity;
import com.nilhcem.frcndict.updatedb.ImportUpdateService;

/**
 * Checks if database exists.
 * If yes, redirects to the main panel activity
 * If no, launches the import data activity
 * TODO: Can be a service?
 */
public final class CheckDataActivity extends Activity {
	private static final long NB_MILLISEC_IN_A_DAY = 86400000l; //24 * 60 * 60 * 1000

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
		boolean initDatabase = true;

		// Check if an import/update is currently in progress
		if (!AbstractDictActivity.checkForDatabaseImportOrUpdate(this)) {
			SharedPreferences prefs = getSharedPreferences(SettingsActivity.PREFS_NAME, MODE_PRIVATE);
			String dbPath = prefs.getString(SettingsActivity.KEY_DB_PATH, null);

			if (dbPath != null) {
				DatabaseHelper dbHelper = DatabaseHelper.getInstance();
				dbHelper.setDatabasePath(new File(dbPath));
				initDatabase = !dbHelper.isInitialized();
			}

			if (initDatabase) {
				intent = new Intent(this, ImportActivity.class);
			} else {
				checkForUpdates(prefs);
				intent = new Intent(this, SearchActivity.class);
			}
			intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			startActivity(intent);
		}
	}

	private void checkForUpdates(SharedPreferences prefs) {
		//If no update currently
		if (ImportUpdateService.getInstance() == null
				|| ImportUpdateService.getInstance().getStatus() == ImportUpdateService.STATUS_UNSTARTED) {
			String updatePrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext())
					.getString(SettingsActivity.KEY_DATABASE_UPDATES, SettingsActivity.VAL_DATABASE_UPDATES_NEVER);

			if (!updatePrefs.equals(SettingsActivity.VAL_DATABASE_UPDATES_NEVER)) {
				long curDate = getCurDateInMillisWithoutTimeValue();
				boolean startUpdateService = checkIfUpdateServiceShouldBeStarted(prefs, curDate, updatePrefs);

				// Set new last update checked
				SharedPreferences.Editor editor = prefs.edit();
				editor.putLong(SettingsActivity.KEY_LAST_UPDATE_CHECKED, curDate);
				editor.commit();

				if (startUpdateService) {
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

	private boolean checkIfUpdateServiceShouldBeStarted(SharedPreferences prefs, long curDate, String updatePrefs) {
		boolean startUpdateService = false;
		long lastTimeChecked = prefs.getLong(SettingsActivity.KEY_LAST_UPDATE_CHECKED, 0l);

		if (lastTimeChecked == 0l) { // never checked
			startUpdateService = true;
		} else {
			long daysBetween = (curDate - lastTimeChecked) / CheckDataActivity.NB_MILLISEC_IN_A_DAY;
			if ((daysBetween > 31) // monthly
				|| (updatePrefs.equals(SettingsActivity.VAL_DATABASE_UPDATES_WEEKLY) && daysBetween > 7)
				|| (updatePrefs.equals(SettingsActivity.VAL_DATABASE_UPDATES_DAILY) && daysBetween > 0)) {
				startUpdateService = true;
			}
		}

		return startUpdateService;
	}
}
