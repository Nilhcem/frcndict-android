package com.nilhcem.frcndict.updatedb;

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
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import com.nilhcem.frcndict.ApplicationController;
import com.nilhcem.frcndict.R;
import com.nilhcem.frcndict.database.DatabaseHelper;
import com.nilhcem.frcndict.settings.SettingsActivity;
import com.nilhcem.frcndict.updatedb.xml.BackupXmlWriter;
import com.nilhcem.frcndict.updatedb.xml.RestoreXmlReader;
import com.nilhcem.frcndict.utils.FileHandler;
import com.nilhcem.frcndict.utils.HttpDownloader;
import com.nilhcem.frcndict.utils.Md5;
import com.nilhcem.frcndict.utils.Unzip;

public final class ImportUpdateService extends Service {
	private static ImportUpdateService sInstance = null; // singleton
	private static WeakReference<AbstractImportUpdateActivity> sActivity;

	private BackupAsync backupTask;
	private DownloadFileAsync downloadTask;
	private UnzipAsync unzipTask;
	private RestoreAsync restoreTask;
	private NotificationManager mNotificationMngr;

	private File xmlFile; // "starred words" backup
	private int curStatus; // activity status, see STATUS_*
	private int curErrorId; //error string id or 0 if no error
	private boolean mImport; //import or update
	private int[] progressPercents = new int[ImportUpdateService.PROGRESS_BAR_RESTORE + 1]; // percent of each progressbar, see PROGRESS_BAR_* for idx

	// Intent keys
	public static final String INTENT_IMPORT_KEY = "is-import";
	public static final String INTENT_SDCARD_KEY = "install-on-sdcard";

	// File names
	private static final String TEMP_XML_FILE = "backup.xml";
	private static final String TEMP_ZIP_FILE = "download.tmp";
	private static final String TEMP_MD5_FILE = "md5sum";

	// Status (sorted)
	public static final int STATUS_UNSTARTED = 0;
	public static final int STATUS_PROCESSING = 1;
	public static final int STATUS_COMPLETED = 2;

	// Progress bars
	public static final int PROGRESS_BAR_BACKUP = 0;
	public static final int PROGRESS_BAR_DOWNLOAD = 1;
	public static final int PROGRESS_BAR_INSTALL = 2;
	public static final int PROGRESS_BAR_RESTORE = 3;

	// Singleton
	public static ImportUpdateService getInstance() {
		return sInstance;
	}

	public static void setActivity(AbstractImportUpdateActivity activity) {
		ImportUpdateService.sActivity = new WeakReference<AbstractImportUpdateActivity>(activity);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		ImportUpdateService.setInstance(this);
		mNotificationMngr = ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE));
		resetService();
		backupTask = new BackupAsync();
		downloadTask = new DownloadFileAsync();
		unzipTask = new UnzipAsync();
		restoreTask = new RestoreAsync();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		changeStatus(ImportUpdateService.STATUS_PROCESSING);

		mImport = intent.getBooleanExtra(ImportUpdateService.INTENT_IMPORT_KEY, true);
		displayImportNotification();

		if (mImport) {
			File rootDir = FileHandler.getAppRootDir(getApplication(), intent.getBooleanExtra(ImportUpdateService.INTENT_SDCARD_KEY, false));
			downloadTask.execute(rootDir);
		} else {
			mNotificationMngr.cancel(ApplicationController.NOTIF_UPDATE_AVAILABLE);
			backupTask.execute();
		}
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mNotificationMngr.cancel(ApplicationController.NOTIF_IMPORTING);
		backupTask.cancel(true);
		downloadTask.cancel(true);
		unzipTask.cancel(true);
		restoreTask.cancel(true);
		rollbackUpdate();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	public int getStatus() {
		return curStatus;
	}

	public void changeStatus(int newStatus) {
		curStatus = newStatus;
		AbstractImportUpdateActivity activity = sActivity.get();
		if (activity != null) {
			activity.updateDisplay();
		}
	}

	public int getErrorId() {
		return curErrorId;
	}

	public int getPercent(int progressBarId) {
		return progressPercents[progressBarId];
	}

	public boolean isImport() {
		return mImport;
	}

	public void setAsFinished() {
		resetService();
		ImportUpdateService.setInstance(null);
	}

	private static void setInstance(ImportUpdateService instance) {
		sInstance = instance;
	}

	private void resetService() {
		curStatus = ImportUpdateService.STATUS_UNSTARTED;
		curErrorId = 0;

		mNotificationMngr.cancel(ApplicationController.NOTIF_IMPORTING);
		mNotificationMngr.cancel(ApplicationController.NOTIF_IMPORT_SUCCESS);
		mNotificationMngr.cancel(ApplicationController.NOTIF_IMPORT_FAILED);
		resetProgressPercents();
	}

	private void resetProgressPercents() {
		progressPercents[ImportUpdateService.PROGRESS_BAR_BACKUP] = 0;
		progressPercents[ImportUpdateService.PROGRESS_BAR_DOWNLOAD] = 0;
		progressPercents[ImportUpdateService.PROGRESS_BAR_INSTALL] = 0;
		progressPercents[ImportUpdateService.PROGRESS_BAR_RESTORE] = 0;
	}

	private void stopService(int errorId) {
		if (errorId == 0) { // no error: finish service properly
			changeStatus(ImportUpdateService.STATUS_COMPLETED);
			if (sActivity.get() == null) {
				displaySuccessNotification();
			}
		} else { // an error appeared: display error
			curErrorId = errorId;

			if (sActivity.get() != null) {
				sActivity.get().displayError(errorId);
			} else {
				displayErrorNotification();
			}
		}
		stopSelf();
	}

	private void displayImportNotification() {
		String title = getString(mImport ? R.string.import_notification_import_title : R.string.update_notification_import_title);
		String message = getString(R.string.import_notification_import_msg);

		// Instantiate the notification
		Notification notification = new Notification(android.R.drawable.stat_notify_sync_noanim, title, 0l);
		notification.flags |= Notification.FLAG_ONGOING_EVENT;
		notification.flags |= Notification.FLAG_ONLY_ALERT_ONCE;
		notification.when = System.currentTimeMillis();

		// Define the notification message and pending intent
		Intent notificationIntent = new Intent(this, mImport ? ImportActivity.class : UpdateActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
		notification.setLatestEventInfo(getApplicationContext(), title, message, contentIntent);

		// Display notification
		mNotificationMngr.notify(ApplicationController.NOTIF_IMPORTING, notification);
	}

	private void displaySuccessNotification() {
		String title = getString(mImport ? R.string.import_notification_success_title : R.string.update_notification_success_title);
		String message = getString(mImport ? R.string.import_notification_success_msg : R.string.update_notification_success_msg);

		// Instantiate the notification
		Notification notification = new Notification(android.R.drawable.presence_online, title, 0l);
		notification.when = System.currentTimeMillis();

		// Define the notification message and pending intent
		Intent notificationIntent = new Intent(this, mImport ? ImportActivity.class : UpdateActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
		notification.setLatestEventInfo(getApplicationContext(), title, message, contentIntent);

		// Display notification
		mNotificationMngr.notify(ApplicationController.NOTIF_IMPORT_SUCCESS, notification);
	}

	private void displayErrorNotification() {
		String title = getString(R.string.import_notification_failed_title);
		String message = getString(R.string.import_notification_failed_msg);

		// Instantiate the notification
		Notification notification = new Notification(android.R.drawable.ic_delete, title, 0l);
		notification.when = System.currentTimeMillis();

		// Define the notification message and pending intent
		Intent notificationIntent = new Intent(this, mImport ? ImportActivity.class : UpdateActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
		notification.setLatestEventInfo(getApplicationContext(), title, message, contentIntent);

		// Display notification
		mNotificationMngr.notify(ApplicationController.NOTIF_IMPORT_FAILED, notification);
	}

	// Saves progress percent, useful if activity resume from pause, get the progress data directly.
	private void updateProgressData(int progressBarId, Integer value) {
		progressPercents[progressBarId] = value;
		AbstractImportUpdateActivity activity = sActivity.get();
		if (activity != null) {
			activity.updateProgressData(progressBarId, value);
		}
	}

	private void rollbackUpdate() {
		if (!mImport) {
			// Rollback dictionary backup rename
			File dbPath = DatabaseHelper.getInstance().getDatabasePath();
			File backup = new File(dbPath.getAbsolutePath() + BackupAsync.BACKUP_EXTENS);
			if (backup.exists()) {
				backup.renameTo(dbPath);
			}
		}

		// Delete "starred words" backup
		if (xmlFile != null && xmlFile.exists()) {
			xmlFile.delete();
		}
	}

	private class BackupAsync extends AsyncTask<Void, Integer, Void> implements Observer {
		private static final String TAG = "BackupAsync";
		private static final String BACKUP_EXTENS = ".bak";
		private File rootDir;
		private BackupXmlWriter xmlWriter = null;

		public BackupAsync() {
			xmlFile = null;
		}

		@Override
		protected Void doInBackground(Void... params) {
			DatabaseHelper db = DatabaseHelper.getInstance();
			File dbPath = db.getDatabasePath();

			rootDir = FileHandler.getAppRootDir(getApplication(), FileHandler.isDatabaseInstalledOnSDcard());
			xmlFile = new File(rootDir, TEMP_XML_FILE);

			// Backup starred words in the XML file
			try {
				xmlWriter = new BackupXmlWriter(db, xmlFile);
				xmlWriter.addObserver(this);
				xmlWriter.start();
			} catch (IOException e) {
				Log.e(BackupAsync.TAG, "doInBackground() exception", e);
				// Do nothing
			}

			// Rename current db to create a backup
			if (!isCancelled()) {
				if (dbPath.exists()) {
					File backup = new File(dbPath.getAbsolutePath() + BackupAsync.BACKUP_EXTENS);
					dbPath.renameTo(backup);
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			downloadTask.execute(rootDir);
		}

		@Override
		protected void onCancelled() {
			if (xmlWriter != null) {
				xmlWriter.cancel();
			}
		}

		@Override
		public void update(Observable observable, Object data) {
			if (!isCancelled()) {
				if (observable instanceof BackupXmlWriter) {
					publishProgress((Integer) data); // will call onProgressUpdate
				}
			}
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			updateProgressData(ImportUpdateService.PROGRESS_BAR_BACKUP, values[0]);
		}
	}

	private class DownloadFileAsync extends AsyncTask<File, Integer, Integer> implements Observer {
		private static final String TAG = "DownloadFileAsync";
		private static final String ZIP_FILE = "dictionary.zip";
		private static final String MD5_FILE = "md5sum";
		private File rootDir;
		private File zipFile;
		private File md5File;
		private HttpDownloader downloader = null;

		@Override
		protected Integer doInBackground(File... params) {
			Integer errorCode = null;
			rootDir = params[0];
			zipFile = new File(rootDir, TEMP_ZIP_FILE);
			md5File = new File(rootDir, TEMP_MD5_FILE);

			try {
				downloader = new HttpDownloader(ApplicationController.DICT_URL + DownloadFileAsync.ZIP_FILE, zipFile);
				downloader.addObserver(this);
				downloader.start();
				if (!isCancelled()) {
					errorCode = checkMd5();
				}
			} catch (Exception e) {
				Log.e(DownloadFileAsync.TAG, "doInBackground() exception", e);
				errorCode = R.string.import_err_cannot_download;
			}
			return errorCode;
		}

		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);

			if (result == null) { // no error
				unzipTask.execute(zipFile, rootDir);
			} else {
				rollBack();
				stopService(result);
			}
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			if (downloader != null) {
				downloader.cancel();
			}
			rollBack();
		}

		@Override
		public void update(Observable observable, Object data) {
			if (!isCancelled()) {
				if (observable instanceof HttpDownloader) {
					publishProgress((Integer) data);
				}
			}
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			updateProgressData(ImportUpdateService.PROGRESS_BAR_DOWNLOAD, values[0]);
		}

		private Integer checkMd5() {
			Integer errorCode = null;

			try {
				String md5 = Md5.getMd5Sum(zipFile);
				downloader = new HttpDownloader(ApplicationController.DICT_URL + DownloadFileAsync.MD5_FILE, md5File);
				downloader.start();

				String remoteMd5 = FileHandler.readFile(md5File);
				if (!md5.equalsIgnoreCase(remoteMd5)) {
					Log.e(DownloadFileAsync.TAG, "md5 doesn't match");
					errorCode = R.string.import_err_wrong_dictionary_file;
				}
			} catch (IOException e) {
				Log.e(DownloadFileAsync.TAG, "checkMd5() exception", e);
				errorCode = R.string.import_err_wrong_dictionary_file;
			} finally {
				md5File.delete();
			}

			return errorCode;
		}

		private void rollBack() {
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
				Log.e(UnzipAsync.TAG, "checkMd5() exception", e);
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
				mNotificationMngr.cancel(ApplicationController.NOTIF_IMPORTING);
				saveDatabasePath();

				if (mImport) {
					stopService(0);
				} else {
					restoreTask.execute();
				}
			} else {
				rollback();
				stopService(result);
			}
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			if (unzip != null) {
				unzip.cancel();
			}
			rollback();
		}

		@Override
		public void update(Observable observable, Object data) {
			if (!isCancelled()) {
				if (observable instanceof Unzip) {
					publishProgress((Integer) data);
				}
			}
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			updateProgressData(ImportUpdateService.PROGRESS_BAR_INSTALL, values[0]);
		}

		private void saveDatabasePath() {
			SharedPreferences prefs = getSharedPreferences(SettingsActivity.PREFS_NAME, MODE_PRIVATE);
			SharedPreferences.Editor editor = prefs.edit();
			editor.putString(SettingsActivity.KEY_DB_PATH, zippedFile.getAbsolutePath());
			editor.commit();
		}

		private void rollback() {
			if (zipFile != null && zipFile.exists()) {
				zipFile.delete();
			}
			if (zippedFile != null && zippedFile.exists()) {
				zippedFile.delete();
			}
		}
	}

	private class RestoreAsync extends AsyncTask<Void, Integer, Void> implements Observer {
		private static final String TAG = "RestoreAsync";
		private RestoreXmlReader xmlReader = null;

		@Override
		protected Void doInBackground(Void... params) {
			// Restore starred words from the XML file
			try {
				xmlReader = new RestoreXmlReader(DatabaseHelper.getInstance(), xmlFile);
				xmlReader.addObserver(this);
				xmlReader.start();
			} catch (IOException e) {
				Log.e(RestoreAsync.TAG, "doInBackground() exception", e);
				// Do nothing
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			// Delete previous backup
			File backup = new File(DatabaseHelper.getInstance().getDatabasePath().getAbsolutePath() + BackupAsync.BACKUP_EXTENS);
			if (backup.exists()) {
				backup.delete();
			}
			stopService(0);
		}

		@Override
		public void update(Observable observable, Object data) {
			if (!isCancelled()) {
				if (observable instanceof RestoreXmlReader) {
					publishProgress((Integer) data);
				}
			}
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			if (xmlReader != null) {
				xmlReader.cancel();
			}
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			updateProgressData(ImportUpdateService.PROGRESS_BAR_RESTORE, values[0]);
		}
	}
}
