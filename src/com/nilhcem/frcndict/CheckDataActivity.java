package com.nilhcem.frcndict;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

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
	 * Checks if database exists and contains data.
	 */
	private void checkDatabase() {
		Log.i("CheckDataActivity", "Check database");
		DatabaseHelper.getInstance();

		if (DatabaseHelper.getInstance().isInitialized()) {
			// TODO
		} else {
			// TODO
		}
		DatabaseHelper.getInstance().close();
	}
}
