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
	private WeakReference<ImportDataActivity> activity = null;
	private DownloadFileAsync downloadTask = new DownloadFileAsync();
	private UnzipAsync unzipTask = new UnzipAsync();

	public void startDownload() {
		downloadTask.execute();
	}

	public void setActivity(ImportDataActivity activity) {
		this.activity = new WeakReference<ImportDataActivity>(activity);
	}

	public void resetTasks() {
		downloadTask = new DownloadFileAsync();
		unzipTask = new UnzipAsync();
	}

	private class DownloadFileAsync extends AsyncTask<Void, Integer, Integer> implements Observer {
		private static final String DICT_URL = "http://192.168.1.2/cfdict/dictionary.zip";
		private static final String MD5_URL = "http://192.168.1.2/cfdict/md5sum";
		private static final String TEMP_ZIP_FILE = "download.tmp";
		private static final String TEMP_MD5_FILE = "md5sum";
		private File rootDir;
		private File zipFile;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			if (activity.get() == null) {
				Log.w("DownloadFileAsync", "onPreExecute() skipped -- no activity");
			} else {
				ApplicationController app = (ApplicationController) activity.get().getApplication();
				rootDir = app.getRootDir();
				zipFile = new File(rootDir, TEMP_ZIP_FILE);
			}
		}

		@Override
		protected Integer doInBackground(Void... unused) {
			Integer errorCode = null;
			try {
				HttpDownloader downloader = new HttpDownloader(DICT_URL, zipFile);
				downloader.addObserver(this);
				downloader.start();
				errorCode = checkMd5();
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
				if (activity.get() == null) {
					Log.w("DownloadFileAsync", "onPostExecute() skipped -- no activity");
				} else {
					activity.get().changeStatus(ImportDataActivity.Status.STATUS_DOWNLOAD_COMPLETED);
				}
				unzipTask.execute(zipFile, rootDir);
			} else {
				if (activity.get() != null) {
					activity.get().displayError(result);
				}
			}
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
			if (activity.get() == null) {
				Log.w("DownloadFileAsync", "onProgressUpdate() skipped -- no activity");
			} else {
				activity.get().updateProgressData(true, values[0]);
			}
		}

		private Integer checkMd5() {
			Integer errorCode = null;
			File md5File = null;

			try {
				String md5 = Md5.getMd5Sum(zipFile);
				md5File = new File(rootDir, TEMP_MD5_FILE);
				HttpDownloader downloader = new HttpDownloader(MD5_URL, md5File);
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
				if (md5File != null && md5File.exists()) {
					md5File.delete();
				}
			}

			return errorCode;
		}
	}

	private class UnzipAsync extends AsyncTask<File, Integer, Integer> implements Observer {
		private File zipFile = null;

		@Override
		protected Integer doInBackground(File... params) {
			Integer errorCode = null;

			zipFile = params[0];
			Unzip unzip = new Unzip(zipFile, params[1]);
			unzip.addObserver(this);
			try {
				unzip.start();
				if (!new File(params[1], DatabaseHelper.DATABASE_NAME).exists()) {
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

			if (zipFile != null && zipFile.exists()) {
				zipFile.delete();
			}

			if (activity.get() == null) {
				Log.w("UnzipAsync", "onPostExecute() skipped -- no activity");
			} else {
				if (result == null) { // no error
					activity.get().changeStatus(ImportDataActivity.Status.STATUS_INSTALL_COMPLETED);
				} else {
					activity.get().displayError(result);
				}
			}
		}

		@Override
		public void update(Observable observable, Object data) {
			if (observable instanceof Unzip) {
				publishProgress((Integer) data); // will call onProgressUpdate
			}
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			if (activity.get() == null) {
				Log.w("UnzipAsync", "onProgressUpdate() skipped -- no activity");
			} else {
				activity.get().updateProgressData(false, values[0]);
			}
		}
	}
}
