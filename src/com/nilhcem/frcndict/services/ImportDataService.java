package com.nilhcem.frcndict.services;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Observable;
import java.util.Observer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import com.nilhcem.frcndict.ApplicationController;
import com.nilhcem.frcndict.ImportDataActivity;
import com.nilhcem.frcndict.R;
import com.nilhcem.frcndict.database.DatabaseHelper;
import com.nilhcem.frcndict.utils.FileReader;
import com.nilhcem.frcndict.utils.HttpDownloader;
import com.nilhcem.frcndict.utils.Md5;
import com.nilhcem.frcndict.utils.Unzip;

public final class ImportDataService extends Service {
	private static ImportDataService sInstance = null; // singleton
	private static WeakReference<ImportDataActivity> sActivity;

	private DownloadFileAsync downloadTask;
	private UnzipAsync unzipTask;
	private NotificationManager mNotificationMngr;

	private int curStatus; // activity status, see STATUS_*
	private int curErrorId; //error string id or 0 if no error

	public static final int STATUS_BEGIN = 0;
	public static final int STATUS_DOWNLOAD_STARTED = 1;
	public static final int STATUS_DOWNLOAD_COMPLETED = 2;
	public static final int STATUS_INSTALL_COMPLETED = 3;

	private static final int NOTIF_SERVICE_ID = 1;
	private static final int NOTIF_IMPORT_SUCCESS_ID = 2;
	private static final int NOTIF_IMPORT_FAILED_ID = 3;

	@Override
	public void onCreate() {
		super.onCreate();
		sInstance = this;
		mNotificationMngr = ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE));
		resetStatus();
		downloadTask = new DownloadFileAsync();
		unzipTask = new UnzipAsync();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		changeStatus(STATUS_DOWNLOAD_STARTED);
		displayImportNotification();
		downloadTask.execute();
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mNotificationMngr.cancel(NOTIF_SERVICE_ID);
		downloadTask.cancel(true);
		unzipTask.cancel(true);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	// Singleton
	public static ImportDataService getInstance() {
		return sInstance;
	}

	public static void setActivity(ImportDataActivity activity) {
		ImportDataService.sActivity = new WeakReference<ImportDataActivity>(activity);
	}

	public int getStatus() {
		return curStatus;
	}

	public void changeStatus(int newStatus) {
		curStatus = newStatus;
		if (sActivity.get() != null) {
			sActivity.get().updateDisplay();
		}
	}

	public void resetStatus() {
		curStatus = ImportDataService.STATUS_BEGIN;
		curErrorId = 0;
		mNotificationMngr.cancelAll();
	}

	public int getErrorId() {
		return curErrorId;
	}

	private void setErrorAndStopService(int errorId) {
		curErrorId = errorId;

		// Display error
		if (sActivity.get() != null) {
			sActivity.get().displayError(errorId);
		} else {
			displayErrorNotification();
		}
		stopSelf();
	}

	private void displayImportNotification() {
		String title = getString(R.string.import_notification_import_title);
		String message = getString(R.string.import_notification_import_msg);

		// Instantiate the notification
		Notification notification = new Notification(android.R.drawable.stat_notify_sync_noanim, title, 0l);
		notification.flags |= Notification.FLAG_ONGOING_EVENT;
		notification.flags |= Notification.FLAG_ONLY_ALERT_ONCE;
		notification.when = System.currentTimeMillis();

		// Define the notification message and pending intent
		Intent notificationIntent = new Intent(this, ImportDataActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
		notification.setLatestEventInfo(getApplicationContext(), title, message, contentIntent);

		// Display notification
		mNotificationMngr.notify(NOTIF_SERVICE_ID, notification);
	}

	private void displaySuccessNotification() {
		String title = getString(R.string.import_notification_success_title);
		String message = getString(R.string.import_notification_success_msg);

		// Instantiate the notification
		Notification notification = new Notification(android.R.drawable.presence_online, title, 0l);
		notification.when = System.currentTimeMillis();

		// Define the notification message and pending intent
		Intent notificationIntent = new Intent(this, ImportDataActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
		notification.setLatestEventInfo(getApplicationContext(), title, message, contentIntent);

		// Display notification
		mNotificationMngr.notify(NOTIF_IMPORT_SUCCESS_ID, notification);
	}

	private void displayErrorNotification() {
		String title = getString(R.string.import_notification_failed_title);
		String message = getString(R.string.import_notification_failed_msg);

		// Instantiate the notification
		Notification notification = new Notification(android.R.drawable.ic_delete, title, 0l);
		notification.when = System.currentTimeMillis();

		// Define the notification message and pending intent
		Intent notificationIntent = new Intent(this, ImportDataActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
		notification.setLatestEventInfo(getApplicationContext(), title, message, contentIntent);

		// Display notification
		mNotificationMngr.notify(NOTIF_IMPORT_FAILED_ID, notification);
	}

	private class DownloadFileAsync extends AsyncTask<Void, Integer, Integer> implements Observer {
		private static final String TAG = "DownloadFileAsync";
		private static final String DICT_URL = "http://192.168.1.2/cfdict/dictionary.zip";
		private static final String MD5_URL = "http://192.168.1.2/cfdict/md5sum";
		private static final String TEMP_ZIP_FILE = "download.tmp";
		private static final String TEMP_MD5_FILE = "md5sum";
		private File rootDir;
		private File zipFile;
		private File md5File;
		private HttpDownloader downloader = null;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			ApplicationController app = (ApplicationController) getApplication();
			rootDir = app.getRootDir();
			zipFile = new File(rootDir, TEMP_ZIP_FILE);
			md5File = new File(rootDir, TEMP_MD5_FILE);
		}

		@Override
		protected Integer doInBackground(Void... unused) {
			Integer errorCode = null;
			try {
				downloader = new HttpDownloader(DICT_URL, zipFile);
				downloader.addObserver(this);
				downloader.start();
				if (!isCancelled()) {
					errorCode = checkMd5();
				}
			} catch (Exception e) {
				Log.e(TAG, "doInBackground() exception", e);
				errorCode = R.string.import_err_cannot_download;
			}
			return errorCode;
		}

		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);

			if (result == null) { // no error
				changeStatus(ImportDataService.STATUS_DOWNLOAD_COMPLETED);
				unzipTask.execute(zipFile, rootDir);
			} else {
				removeFiles();
				setErrorAndStopService(result);
			}
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			if (downloader != null) {
				downloader.cancel();
			}
			removeFiles();
		}

		@Override
		public void update(Observable observable, Object data) {
			if (observable instanceof HttpDownloader) {
				publishProgress((Integer) data); // will call onProgressUpdate
			}
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			if (sActivity.get() != null) {
				sActivity.get().updateProgressData(true, values[0]);
			}
		}

		private Integer checkMd5() {
			Integer errorCode = null;

			try {
				String md5 = Md5.getMd5Sum(zipFile);
				downloader = new HttpDownloader(MD5_URL, md5File);
				downloader.start();

				String remoteMd5 = FileReader.readFile(md5File);
				if (!md5.equalsIgnoreCase(remoteMd5)) {
					Log.e(TAG, "md5 doesn't match");
					errorCode = R.string.import_err_wrong_dictionary_file;
				}
			} catch (IOException e) {
				Log.e(TAG, "checkMd5() exception", e);
				errorCode = R.string.import_err_wrong_dictionary_file;
			} finally {
				md5File.delete();
			}

			return errorCode;
		}

		private void removeFiles() {
			if (zipFile != null && zipFile.exists()) {
				zipFile.delete();
			}
			if (md5File != null && md5File.exists()) {
				md5File.delete();
			}
		}
	}

	private class UnzipAsync extends AsyncTask<File, Integer, Integer> implements Observer {
		private static final String TAG = "UnzipAsync";
		private File zipFile = null;
		private File zippedFile = null;
		private Unzip unzip = null;

		@Override
		protected Integer doInBackground(File... params) {
			Integer errorCode = null;

			zipFile = params[0];
			zippedFile = new File(params[1], DatabaseHelper.DATABASE_NAME);
			unzip = new Unzip(zipFile, params[1]);
			unzip.addObserver(this);
			try {
				unzip.start();
				if (!isCancelled() && !zippedFile.exists()) {
					throw new IOException("File cannot be found");
				}
			} catch (IOException e) {
				Log.e(TAG, "checkMd5() exception", e);
				errorCode = R.string.import_err_wrong_dictionary_file;
			}
			return errorCode;
		}

		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);

			if (zipFile != null && zipFile.exists()) {
				zipFile.delete();
			}

			if (result == null) {
				mNotificationMngr.cancel(NOTIF_SERVICE_ID);
				changeStatus(ImportDataService.STATUS_INSTALL_COMPLETED);
				if (sActivity.get() == null) {
					displaySuccessNotification();
				}
				stopSelf();
			} else {
				removeFiles();
				setErrorAndStopService(result);
			}
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			if (unzip != null) {
				unzip.cancel();
			}
			removeFiles();
		}

		@Override
		public void update(Observable observable, Object data) {
			if (!isCancelled()) {
				if (observable instanceof Unzip) {
					publishProgress((Integer) data); // will call onProgressUpdate
				}
			}
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			if (sActivity.get() != null) {
				sActivity.get().updateProgressData(false, values[0]);
			}
		}

		private void removeFiles() {
			if (zipFile != null && zipFile.exists()) {
				zipFile.delete();
			}
			if (zippedFile != null && zippedFile.exists()) {
				zippedFile.delete();
			}
		}
	}
}
