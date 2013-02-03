package com.nilhcem.frcndict;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.nilhcem.frcndict.core.Log;
import com.nilhcem.frcndict.database.DictDbHelper;
import com.nilhcem.frcndict.database.StarredDbHelper;
import com.nilhcem.frcndict.database.Tables;
import com.nilhcem.frcndict.search.SearchActivity;
import com.nilhcem.frcndict.settings.SettingsActivity;

/**
 * Makes sure database already exists, otherwise creates it - then launches the
 * Search activity. Migrate data if needed.
 */
public final class SplashActivity extends Activity {
	private static final String TAG = "SplashActivity";

	private DictDbHelper mDictDb;
	private StarredDbHelper mStarredDb;
	private InitDatabaseTask mInitTask;
	SharedPreferences mPrefs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash_screen);
		mPrefs = getSharedPreferences(SettingsActivity.PREFS_NAME, MODE_PRIVATE);
	}

	@Override
	protected void onResume() {
		super.onResume();
		displayInstallText();
		mInitTask = new InitDatabaseTask();
		mInitTask.execute();
	}

	@Override
	protected void onPause() {
		if (mInitTask != null) {
			mInitTask.cancel(true);
		}
		super.onPause();
	}

	private final class InitDatabaseTask extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... params) {
			boolean success = false;

			try {
				Map<String, String> starredWords = getStarredWordsFromDeprecatedDb();

				mStarredDb = new StarredDbHelper(SplashActivity.this);
				mStarredDb.getReadableDatabase();
				migrateData(starredWords);
				mStarredDb.close();

				mDictDb = new DictDbHelper(SplashActivity.this);
				mDictDb.getReadableDatabase();
				success = checkDictionaryInstallation();
				mStarredDb.close();
				mDictDb.close();
			} catch (Exception e) {
				Log.e(TAG, e);
			}

			return success;
		}

		@Override
		protected void onPostExecute(Boolean success) {
			super.onPostExecute(success);

			if (isCancelled()) {
				finish();
			} else {
				if (success) {
					setAsInstalled();
					Intent intent = new Intent(SplashActivity.this,
							SearchActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
					startActivity(intent);
				} else {
					Log.i(TAG, "Error installing dictionary");
					Toast.makeText(SplashActivity.this, R.string.install_error,
							Toast.LENGTH_LONG).show();
					finish();
				}
			}
		}

		// Compatibility - Starting from v9 the dictionary logic has changed.
		// There is a new database for starred words.
		private Map<String, String> getStarredWordsFromDeprecatedDb() {
			Map<String, String> starredWords = new HashMap<String, String>();

			// Get previous database path if any
			String pathLocation = mPrefs.getString(
					SettingsActivity.KEY_DB_PATH_OLD, null);

			if (pathLocation != null) {
				File dbPath = null;
				dbPath = new File(pathLocation);
				if (!dbPath.isFile()) {
					dbPath = null;
				}

				// Get previous data
				if (dbPath != null) {
					Log.d(TAG, "Migrating previous data");
					SQLiteDatabase db = SQLiteDatabase.openDatabase(
							dbPath.getAbsolutePath(), null,
							SQLiteDatabase.OPEN_READWRITE);
					Cursor c = db.query("entries", new String[] { "simplified",
							"starred_date" },
							String.format(Locale.US, "`%s` IS NOT NULL", "starred_date"),
							null, null, null, null);

					if (c != null) {
						while (c.moveToNext()) {
							starredWords.put(c.getString(0), c.getString(1));
						}
						c.close();
					}
					db.close();

					// Delete old database reference + previous database
					SharedPreferences.Editor editor = mPrefs.edit();
					editor.remove(SettingsActivity.KEY_DB_PATH_OLD);
					editor.commit();
					dbPath.delete();
				}
			}

			return starredWords;
		}

		private void migrateData(Map<String, String> starredWords) {
			for (Entry<String, String> entry : starredWords.entrySet()) {
				mStarredDb.setStarredDate(entry.getKey(), entry.getValue());
			}
		}

		private boolean checkDictionaryInstallation() {
			Map<String, String> data = mDictDb.findById(1, mStarredDb);
			return (!TextUtils.isEmpty(data.get(Tables.ENTRIES_KEY_SIMPLIFIED)));
		}
	}

	private void displayInstallText() {
		boolean isInstalled = mPrefs.getBoolean(SettingsActivity.KEY_INSTALLED, false);

		if (!isInstalled) {
			findViewById(R.id.installTitle).setVisibility(View.VISIBLE);
			findViewById(R.id.installSubtitle).setVisibility(View.VISIBLE);
		}
	}

	private void setAsInstalled() {
		SharedPreferences.Editor editor = mPrefs.edit();
		editor.putBoolean(SettingsActivity.KEY_INSTALLED, true);
		editor.commit();
	}
}
