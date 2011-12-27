package com.nilhcem.frcndict.services;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Observable;
import java.util.Observer;

import android.os.AsyncTask;
import android.util.Log;

import com.nilhcem.frcndict.ApplicationController;
import com.nilhcem.frcndict.ImportDataActivity;
import com.nilhcem.frcndict.R;
import com.nilhcem.frcndict.database.DatabaseHelper;
import com.nilhcem.frcndict.utils.FileReader;
import com.nilhcem.frcndict.utils.HttpDownloader;
import com.nilhcem.frcndict.utils.Md5;
import com.nilhcem.frcndict.utils.Unzip;

public final class ImportDataService {
	private WeakReference<ImportDataActivity> activity;
	private DownloadFileAsync downloadTask;
	private UnzipAsync unzipTask;
	private int curStatus; // activity status, see STATUS_*
	private int curErrorId; //error string id or 0 if no error currently

	public static final int STATUS_BEGIN = 0;
	public static final int STATUS_DOWNLOAD_STARTED = 1;
	public static final int STATUS_DOWNLOAD_COMPLETED = 2;
	public static final int STATUS_INSTALL_COMPLETED = 3;

	public ImportDataService() {
		activity = null;
		curStatus = STATUS_BEGIN;
		curErrorId = 0;
		downloadTask = new DownloadFileAsync();
		unzipTask = new UnzipAsync();
	}

	public void startDownload() {
		downloadTask.execute();
		changeStatus(STATUS_DOWNLOAD_STARTED);
	}

	public void setActivity(ImportDataActivity activity) {
		this.activity = new WeakReference<ImportDataActivity>(activity);
	}

	public void cancelTasks() {
		downloadTask.cancel(true);
		unzipTask.cancel(true);
	}

	public void changeStatus(int newStatus) {
		curStatus = newStatus;

		if (activity.get() != null) {
			activity.get().updateDisplay();
		}
	}

	private void setError(int errorId) {
		curErrorId = errorId;

		// Display error
		if (activity.get() != null) {
			activity.get().displayError(errorId);
		}
	}

	public int getStatus() {
		return curStatus;
	}

	public int getErrorId() {
		return curErrorId;
	}

	private class DownloadFileAsync extends AsyncTask<Void, Integer, Integer> implements Observer {
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

			if (activity.get() != null) {
				ApplicationController app = (ApplicationController) activity.get().getApplication();
				rootDir = app.getRootDir();
				zipFile = new File(rootDir, TEMP_ZIP_FILE);
				md5File = new File(rootDir, TEMP_MD5_FILE);
			}
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
				Log.e("DownloadFileAsync", "doInBackground() exception", e);
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
				setError(result);
			}
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();

			if (downloader != null) {
				downloader.cancel();
			}
			zipFile.delete();
			md5File.delete();
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
			if (activity.get() != null) {
				activity.get().updateProgressData(true, values[0]);
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
					Log.e("DownloadFileAsync", "md5 doesn't match");
					errorCode = R.string.import_err_wrong_dictionary_file;
				}
			} catch (IOException e) {
				Log.e("DownloadFileAsync", "checkMd5() exception", e);
				errorCode = R.string.import_err_wrong_dictionary_file;
			} finally {
				md5File.delete();
			}

			return errorCode;
		}
	}

	private class UnzipAsync extends AsyncTask<File, Integer, Integer> implements Observer {
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
				Log.e("UnzipAsync", "checkMd5() exception", e);
				errorCode = R.string.import_err_wrong_dictionary_file;
			}
			return errorCode;
		}

		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);

			if (zipFile != null) {
				zipFile.delete();
			}

			if (result == null) {
				changeStatus(ImportDataService.STATUS_INSTALL_COMPLETED);
			} else {
				setError(result);
			}
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();

			if (unzip != null) {
				unzip.cancel();
			}
			if (zipFile != null) {
				zipFile.delete();
			}
			if (zippedFile != null) {
				zippedFile.delete();
			}
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
			if (activity.get() != null) {
				activity.get().updateProgressData(false, values[0]);
			}
		}
	}
}
