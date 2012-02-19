package com.nilhcem.frcndict.updatedb;

import java.io.File;
import java.io.IOException;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.IBinder;

import com.nilhcem.frcndict.ApplicationController;
import com.nilhcem.frcndict.R;
import com.nilhcem.frcndict.database.DatabaseHelper;
import com.nilhcem.frcndict.utils.FileHandler;
import com.nilhcem.frcndict.utils.HttpDownloader;

// Check if database should be updated
public class CheckForUpdatesService extends Service {
	private static final String TEMP_VERSION_FILE = "version";
	private static final String VERSION_SEPARATOR = "-";

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		DatabaseHelper db = DatabaseHelper.getInstance();
		db.open();
		String curDbVersion = db.getDbVersion();
		db.close();

		new CheckForUpdatesAsync().execute(curDbVersion);
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
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

	private class CheckForUpdatesAsync extends AsyncTask<String, Void, Void> {
		private static final String VERSION_URL = "version";

		@Override
		protected Void doInBackground(String... params) {
			// Download version file
			File rootDir = FileHandler.getAppRootDir(getApplication(), FileHandler.isDatabaseInstalledOnSDcard());
			File versionFile = new File(rootDir, CheckForUpdatesService.TEMP_VERSION_FILE);

			try {
				HttpDownloader downloader = new HttpDownloader(ApplicationController.DICT_URL + CheckForUpdatesAsync.VERSION_URL, versionFile);
				downloader.start();

				// Open file
				String versionStr = FileHandler.readFile(versionFile);
				String[] splitted = versionStr.split(CheckForUpdatesService.VERSION_SEPARATOR);
				if (splitted.length > 1 && splitted[1] != null && splitted[0].length() > 0 && splitted[1].length() > 0) {
					// Check App version code
					int curVersionCode;
					try {
						curVersionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
					} catch (NameNotFoundException e) {
						// Skip update notification
						curVersionCode = Integer.MAX_VALUE;
					}
					int minVersionCode = Integer.parseInt(splitted[1]);

					if (curVersionCode >= minVersionCode && !params[0].equals(splitted[0])) {
						// Check if database number differs
						CheckForUpdatesService.displayUpdateNotification(CheckForUpdatesService.this);
					}
				}
			} catch (IOException e) {
				// TODO
			} finally {
				versionFile.delete();
			}
			return null;
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			stopSelf();
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			stopSelf();
		}
	}
}
