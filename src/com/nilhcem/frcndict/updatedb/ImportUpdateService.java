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
import com.nilhcem.frcndict.core.Config;
import com.nilhcem.frcndict.database.DatabaseHelper;
import com.nilhcem.frcndict.database.Tables;
import com.nilhcem.frcndict.settings.SettingsActivity;
import com.nilhcem.frcndict.updatedb.xml.BackupXmlWriter;
import com.nilhcem.frcndict.updatedb.xml.RestoreXmlReader;
import com.nilhcem.frcndict.utils.FileHandler;
import com.nilhcem.frcndict.utils.HttpDownloader;
import com.nilhcem.frcndict.utils.Md5;
import com.nilhcem.frcndict.utils.Unzip;

public final class ImportUpdateService extends Service {
	private static ImportUpdateService sInstance = null; // singleton
	private static WeakReference<AbstractImportUpdateActivity> sActivity = null;

	private BackupAsync mBackupTask;
	private DownloadFileAsync mDownloadTask;
	private UnzipAsync mUnzipTask;
	private RestoreAsync mRestoreTask;
	private NotificationManager mNotificationMngr;

	private File mXmlFile; // "starred words" backup
	private int mCurStatus; // activity status, see STATUS_*
	private int mCurErrorId; //error string id or 0 if no error
	private boolean mImport; //import or update
	private int[] mProgressPercents = new int[ImportUpdateService.PROGRESS_BAR_RESTORE + 1]; // percent of each progressbar, see PROGRESS_BAR_* for idx

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
		mBackupTask = new BackupAsync();
		mDownloadTask = new DownloadFileAsync();
		mUnzipTask = new UnzipAsync();
		mRestoreTask = new RestoreAsync();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		changeStatus(ImportUpdateService.STATUS_PROCESSING);

		mImport = intent.getBooleanExtra(ImportUpdateService.INTENT_IMPORT_KEY, true);
		displayImportNotification();

		if (mImport) {
			File rootDir = FileHandler.getAppRootDir(getApplication(), intent.getBooleanExtra(ImportUpdateService.INTENT_SDCARD_KEY, false));
			mDownloadTask.execute(rootDir);
		} else {
			mNotificationMngr.cancel(ApplicationController.NOTIF_UPDATE_AVAILABLE);
			mBackupTask.execute();
		}
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mNotificationMngr.cancel(ApplicationController.NOTIF_IMPORTING);
		mBackupTask.cancel(true);
		mDownloadTask.cancel(true);
		mUnzipTask.cancel(true);
		mRestoreTask.cancel(true);
		rollbackUpdate();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	public int getStatus() {
		return mCurStatus;
	}

	public void changeStatus(int newStatus) {
		mCurStatus = newStatus;
		if (sActivity != null) {
			AbstractImportUpdateActivity activity = sActivity.get();
			if (activity != null) {
				activity.updateDisplay();
			}
		}
	}

	public int getErrorId() {
		return mCurErrorId;
	}

	public int getPercent(int progressBarId) {
		return mProgressPercents[progressBarId];
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
		mCurStatus = ImportUpdateService.STATUS_UNSTARTED;
		mCurErrorId = 0;

		mNotificationMngr.cancel(ApplicationController.NOTIF_IMPORTING);
		mNotificationMngr.cancel(ApplicationController.NOTIF_IMPORT_SUCCESS);
		mNotificationMngr.cancel(ApplicationController.NOTIF_IMPORT_FAILED);
		resetProgressPercents();
	}

	private void resetProgressPercents() {
		mProgressPercents[ImportUpdateService.PROGRESS_BAR_BACKUP] = 0;
		mProgressPercents[ImportUpdateService.PROGRESS_BAR_DOWNLOAD] = 0;
		mProgressPercents[ImportUpdateService.PROGRESS_BAR_INSTALL] = 0;
		mProgressPercents[ImportUpdateService.PROGRESS_BAR_RESTORE] = 0;
	}

	private void stopService(int errorId) {
		if (errorId == 0) { // no error: finish service properly
			changeStatus(ImportUpdateService.STATUS_COMPLETED);
			if (sActivity == null || sActivity.get() == null) {
				displaySuccessNotification();
			}
		} else { // an error appeared: display error
			mCurErrorId = errorId;

			if (sActivity == null || sActivity.get() == null) {
				displayErrorNotification();
			} else if (sActivity != null) {
				sActivity.get().displayError(errorId);
			}
		}
		stopSelf();
	}

	private void displayImportNotification() {
		String title = getString(mImport ? R.string.import_notification_import_title : R.string.update_notification_import_title);
		String message = getString(R.string.import_notification_import_msg);

		// Instantiate the notification
		Notification notification = new Notification(R.drawable.stat_notify_sync, title, 0l);
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
		Notification notification = new Notification(R.drawable.presence_online, title, 0l);
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
		Notification notification = new Notification(R.drawable.ic_delete, title, 0l);
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
		mProgressPercents[progressBarId] = value;
		if (sActivity != null) {
			AbstractImportUpdateActivity activity = sActivity.get();
			if (activity != null) {
				activity.updateProgressData(progressBarId, value);
			}
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
		if (mXmlFile != null && mXmlFile.exists()) {
			mXmlFile.delete();
		}
	}

	private class BackupAsync extends AsyncTask<Void, Integer, Void> implements Observer {
		private static final String TAG = "BackupAsync";
		private static final String BACKUP_EXTENS = ".bak";
		private File mRootDir;
		private BackupXmlWriter mXmlWriter = null;

		public BackupAsync() {
			super();
			mXmlFile = null;
		}

		@Override
		protected Void doInBackground(Void... params) {
			DatabaseHelper db = DatabaseHelper.getInstance();
			File dbPath = db.getDatabasePath();

			mRootDir = FileHandler.getAppRootDir(getApplication(), FileHandler.isDatabaseInstalledOnSDcard());
			mXmlFile = new File(mRootDir, TEMP_XML_FILE);

			// Backup starred words in the XML file
			try {
				mXmlWriter = new BackupXmlWriter(db, mXmlFile);
				mXmlWriter.addObserver(this);
				mXmlWriter.start();
			} catch (IOException ex) {
				if (Config.LOG_ERROR) Log.e(BackupAsync.TAG, "Failed backing up starred words", ex);
				// Do nothing
			}

			// Rename current db to create a backup
			if (!isCancelled() && dbPath.exists()) {
				File backup = new File(dbPath.getAbsolutePath() + BackupAsync.BACKUP_EXTENS);
				dbPath.renameTo(backup);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			mDownloadTask.execute(mRootDir);
		}

		@Override
		protected void onCancelled() {
			if (mXmlWriter != null) {
				mXmlWriter.cancel();
			}
		}

		@Override
		public void update(Observable observable, Object data) {
			if (!isCancelled() && observable instanceof BackupXmlWriter) {
				publishProgress((Integer) data); // will call onProgressUpdate
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
		private File mRootDir;
		private File mZipFile;
		private File mMd5File;
		private HttpDownloader mDownloader = null;

		@Override
		protected Integer doInBackground(File... params) {
			Integer errorCode = null;
			mRootDir = params[0];
			mZipFile = new File(mRootDir, TEMP_ZIP_FILE);
			mMd5File = new File(mRootDir, TEMP_MD5_FILE);

			try {
				errorCode = checkVersion();
				if (errorCode == null) {
					mDownloader = new HttpDownloader(Config.DICT_URL + DownloadFileAsync.ZIP_FILE, mZipFile);
					mDownloader.addObserver(this);
					mDownloader.start();
					if (!isCancelled()) {
						errorCode = checkMd5();
					}
				}
			} catch (IOException ex) {
				if (Config.LOG_ERROR) Log.e(DownloadFileAsync.TAG, "Error downloading dictionary", ex);
				errorCode = R.string.import_err_cannot_download;
			}
			return errorCode;
		}

		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);

			if (result == null) { // no error
				mUnzipTask.execute(mZipFile, mRootDir);
			} else {
				rollBack();
				stopService(result);
			}
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			if (mDownloader != null) {
				mDownloader.cancel();
			}
			rollBack();
		}

		@Override
		public void update(Observable observable, Object data) {
			if (!isCancelled() && observable instanceof HttpDownloader) {
				publishProgress((Integer) data);
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
				String md5 = Md5.getMd5Sum(mZipFile);
				mDownloader = new HttpDownloader(Config.DICT_URL + DownloadFileAsync.MD5_FILE, mMd5File);
				mDownloader.start();

				String remoteMd5 = FileHandler.readFile(mMd5File);
				if (!md5.equalsIgnoreCase(remoteMd5)) {
					if (Config.LOG_ERROR) Log.e(DownloadFileAsync.TAG, "MD5 doesn't match");
					errorCode = R.string.import_err_wrong_dictionary_file;
				}
			} catch (IOException ex) {
				if (Config.LOG_ERROR) Log.e(DownloadFileAsync.TAG, "Failed while checking MD5", ex);
				errorCode = R.string.import_err_wrong_dictionary_file;
			} finally {
				mMd5File.delete();
			}

			return errorCode;
		}

		private void rollBack() {
			if (mZipFile != null && mZipFile.exists()) {
				mZipFile.delete();
			}
			if (mMd5File != null && mMd5File.exists()) {
				mMd5File.delete();
			}
		}

		private Integer checkVersion() throws IOException {
			Integer errorCode = null;

			// If import, check if database we are going to download is compatible with current version
			if (mImport) {
				File versionFile = new File(mRootDir, CheckForUpdatesService.VERSION_FILE);
				try {
					HttpDownloader downloader = new HttpDownloader(Config.DICT_URL + CheckForUpdatesService.VERSION_FILE, versionFile);
					downloader.start();

					// Open file
					String versionStr = FileHandler.readFile(versionFile);
					String[] splitted = versionStr.split(DatabaseHelper.VERSION_SEPARATOR);

					if (splitted.length > 1 && splitted[1] != null && splitted[1].length() > 0) {
						int onlineVersion = Integer.parseInt(splitted[1]);
						if (Tables.DATABASE_VERSION != onlineVersion) {
							if (Config.LOG_ERROR) {
								Log.e(DownloadFileAsync.TAG, "Program is too old for this database");
								Log.e(DownloadFileAsync.TAG, "Database version required: " + Tables.DATABASE_VERSION);
								Log.e(DownloadFileAsync.TAG, "Database version online: " + onlineVersion);
							}
							errorCode = R.string.import_err_too_old;
						}
					}
				} finally {
					versionFile.delete();
				}
			}
			return errorCode;
		}
	}

	private class UnzipAsync extends AsyncTask<File, Integer, Integer> implements Observer {
		private static final String TAG = "UnzipAsync";
		private File mZipFile = null;
		private File mZippedFile = null;
		private Unzip mUnzip = null;

		@Override
		protected Integer doInBackground(File... params) {
			Integer errorCode = null;

			mZipFile = params[0];
			mZippedFile = new File(params[1], DatabaseHelper.DATABASE_NAME);
			mUnzip = new Unzip(mZipFile, params[1]);
			mUnzip.addObserver(this);
			try {
				mUnzip.start();
				if (!isCancelled() && !mZippedFile.exists()) {
					throw new IOException("File cannot be found");
				}
			} catch (IOException ex) {
				if (Config.LOG_ERROR) Log.e(UnzipAsync.TAG, "Failed unzipping dictionary", ex);
				errorCode = R.string.import_err_wrong_dictionary_file;
			}
			return errorCode;
		}

		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);

			if (mZipFile != null && mZipFile.exists()) {
				mZipFile.delete();
			}

			if (result == null) {
				mNotificationMngr.cancel(ApplicationController.NOTIF_IMPORTING);
				saveDatabasePath();

				if (mImport) {
					stopService(0);
				} else {
					mRestoreTask.execute();
				}
			} else {
				rollback();
				stopService(result);
			}
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			if (mUnzip != null) {
				mUnzip.cancel();
			}
			rollback();
		}

		@Override
		public void update(Observable observable, Object data) {
			if (!isCancelled() && observable instanceof Unzip) {
				publishProgress((Integer) data);
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
			editor.putString(SettingsActivity.KEY_DB_PATH, mZippedFile.getAbsolutePath());
			editor.commit();
		}

		private void rollback() {
			if (mZipFile != null && mZipFile.exists()) {
				mZipFile.delete();
			}
			if (mZippedFile != null && mZippedFile.exists()) {
				mZippedFile.delete();
			}
		}
	}

	private class RestoreAsync extends AsyncTask<Void, Integer, Void> implements Observer {
		private static final String TAG = "RestoreAsync";
		private RestoreXmlReader mXmlReader = null;

		@Override
		protected Void doInBackground(Void... params) {
			// Restore starred words from the XML file
			try {
				mXmlReader = new RestoreXmlReader(DatabaseHelper.getInstance(), mXmlFile);
				mXmlReader.addObserver(this);
				mXmlReader.start();
			} catch (IOException ex) {
				if (Config.LOG_ERROR) Log.e(RestoreAsync.TAG, "Failed restoring starred words", ex);
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
			if (!isCancelled() && observable instanceof RestoreXmlReader) {
				publishProgress((Integer) data);
			}
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			if (mXmlReader != null) {
				mXmlReader.cancel();
			}
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			updateProgressData(ImportUpdateService.PROGRESS_BAR_RESTORE, values[0]);
		}
	}
}
