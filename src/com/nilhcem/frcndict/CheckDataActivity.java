package com.nilhcem.frcndict;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.google.android.gcm.GCMRegistrar;
import com.nilhcem.frcndict.core.AbstractDictActivity;
import com.nilhcem.frcndict.core.Config;
import com.nilhcem.frcndict.database.DatabaseHelper;
import com.nilhcem.frcndict.database.Tables;
import com.nilhcem.frcndict.gcm.GCMServerUtilities;
import com.nilhcem.frcndict.search.SearchActivity;
import com.nilhcem.frcndict.settings.SettingsActivity;
import com.nilhcem.frcndict.updatedb.ImportActivity;
import com.nilhcem.frcndict.updatedb.ImportUpdateService;
import com.nilhcem.frcndict.updatedb.UpdateActivity;
import com.nilhcem.frcndict.utils.FileHandler;
import com.nilhcem.frcndict.utils.Log;

/**
 * Checks if database exists and register to GCM for getting updates.
 * <p>
 * If database exists, redirects to the main panel activity.<br />
 * If no, launches the import data activity.
 * </p>
 */
public final class CheckDataActivity extends Activity {
	private static final String TAG = "CheckDataActivity";

	private String mDbInstalledVersion = null;
	private DatabaseHelper mDb = DatabaseHelper.getInstance();
	private AsyncTask<Void, Void, Void> mRegisterTask;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (!AbstractDictActivity.checkForDatabaseImportOrUpdate(this)) {
			setDatabasePath();
			registerGcm();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		// Get database version (if null, db is not initialized)
		mDb.open();
		mDbInstalledVersion = mDb.getDbVersion();
		mDb.close();

		// Proceed if no import/update are currently processing
		if (!AbstractDictActivity.checkForDatabaseImportOrUpdate(this)) {
			if (!checkLocalUpdate()) {
				checkForUpdates();
				checkDatabase(false);
			}
		}
	}

	@Override
	protected void onDestroy() {
		Log.d(TAG, "Destroying activity");
		if (Config.isGcmEnabled()) {
			if (mRegisterTask != null) {
				mRegisterTask.cancel(true);
			}
			try {
			    GCMRegistrar.onDestroy(this);
			} catch (IllegalArgumentException e) {
			    Log.e(TAG, e);
			}
		}
		super.onDestroy();
    }

	private void setDatabasePath() {
		SharedPreferences prefs = getSharedPreferences(SettingsActivity.PREFS_NAME, MODE_PRIVATE);
		String pathLocation = prefs.getString(SettingsActivity.KEY_DB_PATH, null);

		File dbPath = null;
		if (pathLocation != null) {
			dbPath = new File(pathLocation);
			if (!dbPath.isFile()) {
				dbPath = null;
			}
		}
		mDb.setDatabasePath(dbPath);
	}

	private void registerGcm() {
		// Do not register if user doesn't want to
		boolean register = PreferenceManager.getDefaultSharedPreferences(this)
				.getBoolean(SettingsActivity.KEY_DATABASE_UPDATES, true);

		if (Config.isGcmEnabled() && register) {
			try {
				GCMRegistrar.checkDevice(this);
				// GCMRegistrar.checkManifest(this);
				final String regId = GCMRegistrar.getRegistrationId(this);
				if (regId.length() == 0) {
					// Automatically registers application on startup
					Log.i(TAG, "Register device");
					GCMRegistrar.register(this, Config.GCM_SENDER_ID);
				} else {
					Log.d(TAG, "Device is already registered on GCM");
					if (GCMRegistrar.isRegisteredOnServer(this)) {
						Log.d(TAG, "Device is already registered on server. Skip registration");
					} else {
						Log.d(TAG, "Device is registered on GCM, but not on our server");

						// Try to register again, but not in the UI thread
						final Context context = this;
						mRegisterTask = new AsyncTask<Void, Void, Void>() {
							@Override
							protected Void doInBackground(Void... params) {
								boolean registered = GCMServerUtilities.register(context, regId);
								if (!registered) {
									Log.d(TAG, "All attempts to register with the app server failed. Unregister device from GCM");
									// GCMRegistrar.unregister(context);
									// Note that the app will try to register again when it is restarted
								}

								// Note, we should theoretically not comment the unregister call above,
								// and not set as RegisteredOnServer (as below),
								// but this is to avoid the case where, ie, user is in China and site is blocked,
								// so we avoid always trying to register device again once app is restarted,
								GCMRegistrar.setRegisteredOnServer(context, true);
								return null;
							}

							@Override
							protected void onPostExecute(Void result) {
								mRegisterTask = null;
							}
						};
						mRegisterTask.execute(null, null, null);
					}
				}
			} catch (UnsupportedOperationException e) {
				Log.e(TAG, e, "Device doesn't support GCM");
			}
		}
	}

	private void checkForUpdates() {
		SharedPreferences prefs = getSharedPreferences(SettingsActivity.PREFS_NAME, MODE_PRIVATE);
		String updateVersion = prefs.getString(SettingsActivity.KEY_DATABASE_UPDATES, null);

		if (mDbInstalledVersion != null && updateVersion != null
				&& isVersionCompatible(this, mDbInstalledVersion, updateVersion)) {
			// Display notification
			CheckDataActivity.displayUpdateNotification(this);

			// Remove key from preferences
			SharedPreferences.Editor editor = prefs.edit();
			editor.remove(SettingsActivity.KEY_DATABASE_UPDATES);
			editor.commit();
		}
	}

	private boolean isVersionCompatible(Context context, String installedVersion, String updateVersion) {
		boolean isCompatible = false;
		String[] splitted = updateVersion.split(DatabaseHelper.VERSION_SEPARATOR);
		if (splitted.length > 1 && splitted[1] != null && splitted[0].length() > 0 && splitted[1].length() > 0) {
			// Check App version code
			int curVersionCode;
			try {
				curVersionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
			} catch (NameNotFoundException e) {
				// Skip update notification
				curVersionCode = Integer.MAX_VALUE;
			}
			int minVersionCode = 0;
			try {
				minVersionCode = Integer.parseInt(splitted[1]);
			} catch (NumberFormatException e) {
				Log.e(TAG, e);
			}

			// Check if database number differs
			if (curVersionCode >= minVersionCode && !installedVersion.equals(splitted[0])) {
				Log.d(TAG, "[Update] Update is available");
				Log.d(TAG, "[Update] Current DB version: %d", curVersionCode);
				Log.d(TAG, "[Update] Available DB version: %d", minVersionCode);
				isCompatible = true;
			}
		}
		return isCompatible;
	}

	/**
	 * Checks if user put the dictionary in its external storage to update with no Internet.
	 *
	 * @return true if local dictionary file exists.
	 */
	private boolean checkLocalUpdate() {
		File localDictionary = FileHandler.getDictionaryFileOnExternalStorage();
		if (localDictionary.isFile()) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.install_local_title);
			builder.setMessage(String.format(getString(R.string.install_local_desc),
					FileHandler.EXTERNAL_DICT_PATH));
			builder.setCancelable(false);
			builder.setPositiveButton(R.string.install_local_yes, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					checkDatabase(true);
				}
			});
			builder.setNegativeButton(R.string.install_local_no, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					checkForUpdates();
					checkDatabase(false);
				}
			});
			AlertDialog alert = builder.create();
			alert.show();
			return true;
		}
		return false;
	}

	/**
	 * Checks if database exists and contains data.
	 * <p>
	 * If database exists, redirects to the main panel activity.<br />
	 * If no, launches the import data activity.
	 * </p>
	 */
	private void checkDatabase(boolean localInstall) {
		Intent intent;

		if (mDbInstalledVersion == null) {
			Log.d(CheckDataActivity.class.getSimpleName(), "[Check Database] Not available. Redirect to Import.");
			intent = new Intent(this, ImportActivity.class);
		} else {
			Log.d(CheckDataActivity.class.getSimpleName(), "[Check Database] Available. Redirect to Search.");
			if (!localInstall && isDatabaseCompatible()) {
				intent = new Intent(this, SearchActivity.class);
			} else {
				// Happens when a dictionary with an old database was updated
				// and database is not compatible with the new version.
				intent = new Intent(this, UpdateActivity.class);
			}
		}

		// Check local install
		if (localInstall) {
			intent.putExtra(ImportUpdateService.INTENT_LOCAL_INSTALL, true);
		}

		intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		startActivity(intent);
	}

	// Checks if current program version is compatible with installed database
	private boolean isDatabaseCompatible() {
		boolean isCompatible = true;

		int installedDbVersion;
		try {
			installedDbVersion = Integer.parseInt(mDbInstalledVersion.split(DatabaseHelper.VERSION_SEPARATOR)[1]);
		} catch (NumberFormatException e) {
			Log.e(CheckDataActivity.class.getSimpleName(), "", e);
			installedDbVersion = Tables.DATABASE_VERSION;
		}

		// Force a mandatory update if previous database is not compatible with this program version
		if (installedDbVersion != Tables.DATABASE_VERSION) {
			Log.i(CheckDataActivity.class.getSimpleName(), "[Check Database] Database is not compatible. Redirect to Import.");
			isCompatible = false;
		}
		return isCompatible;
	}

	public static void displayUpdateNotification(Context context) {
		String title = context.getString(R.string.update_notification_title);
		String message = context.getString(R.string.update_notification_msg);

		// Instantiate the notification
		Notification notification = new Notification(R.drawable.ic_launcher, title, 0l);
		notification.when = System.currentTimeMillis();

		// Define the notification message and pending intent
		Intent notificationIntent = new Intent(context, UpdateActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
		notification.setLatestEventInfo(context.getApplicationContext(), title, message, contentIntent);

		// Display notification
		NotificationManager notificationMngr = ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE));
		notificationMngr.notify(ApplicationController.NOTIF_UPDATE_AVAILABLE, notification);
	}
}
