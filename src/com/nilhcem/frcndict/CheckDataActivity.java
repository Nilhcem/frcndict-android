package com.nilhcem.frcndict;

import android.app.Activity;
import android.content.Intent;
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

//	@Override
//	protected void onStop() {
//		super.onStop();
//		DatabaseHelper.getInstance().close();
//	}

	/**
	 * Checks if database exists and contains data.
	 */
	private void checkDatabase() {
		Log.i("CheckDataActivity", "Check database");
		DatabaseHelper.getInstance();

		if (DatabaseHelper.getInstance().isInitialized()) {
			// TODO
		} else {
			Intent intent = new Intent(this, ImportDataActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			startActivity(intent);
		}
	}
}
