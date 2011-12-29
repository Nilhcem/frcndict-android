package com.nilhcem.frcndict;

import java.io.File;

import com.nilhcem.frcndict.database.DatabaseHelper;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

/**
 * Checks if database exists.
 * If yes, redirects to the main panel activity
 * If no, launches the import data activity
 * TODO: Can be a service?
 */
public final class CheckDataActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onResume() {
		super.onResume();
		checkDatabase();
	}

	/**
	 * Checks if database exists and contains data. If yes, redirects to the main activity, otherwise, launches the import data activity.
	 */
	private void checkDatabase() {
		boolean initDatabase = true;

		SharedPreferences prefs = getSharedPreferences(ApplicationController.PREFS_NAME, MODE_PRIVATE);
		String dbPath = prefs.getString(ApplicationController.PREFS_DB_PATH, null);

		if (dbPath != null) {
			DatabaseHelper dbHelper = DatabaseHelper.getInstance();
			dbHelper.setDatabasePath(new File(dbPath));
			initDatabase = !dbHelper.isInitialized();
		}

		if (initDatabase) {
			Intent intent = new Intent(this, ImportDataActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			startActivity(intent);
		} else {
			// TODO
		}
	}
}
