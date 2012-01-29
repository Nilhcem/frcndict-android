package com.nilhcem.frcndict.core;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.nilhcem.frcndict.CheckDataActivity;
import com.nilhcem.frcndict.R;
import com.nilhcem.frcndict.database.DatabaseHelper;
import com.nilhcem.frcndict.settings.SettingsActivity;

public abstract class AbstractDictActivity extends Activity {
	protected DatabaseHelper db = DatabaseHelper.getInstance();
	protected SharedPreferences prefs;

	// should be the first method called, once called, other code should be in a if (!isFinishing()) condition
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Get shared preferences
		prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

		// Check if database is initialized and open it (if it is not already opened), otherwise, redirects to the main activity
		if (db.getDatabasePath() == null) {
			// redirect to the main activity
			Intent intent = new Intent(this, CheckDataActivity.class);
			finish();
			intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			overridePendingTransition(0, 0);
			startActivity(intent);
		} else {
			db.open();
		}

		// Set night mode theme if set by user.
		if (prefs.getBoolean(SettingsActivity.KEY_DARK_THEME, false)) {
			setTheme(R.style.DarkTheme);
		}
	}
}
