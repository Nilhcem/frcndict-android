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
import com.nilhcem.frcndict.utils.FileReader;
import com.nilhcem.frcndict.utils.HttpDownloader;
import com.nilhcem.frcndict.utils.Md5;
import com.nilhcem.frcndict.utils.Unzip;

public final class ImportDataService {
	private WeakReference<ImportDataActivity> activity = null;
	private DownloadFileAsync downloadTask = new DownloadFileAsync();
	private UnzipAsync unzipTask = new UnzipAsync();

	public void startDownload() {
		downloadTask.execute("");
	}

	public void setActivity(ImportDataActivity activity) {
		this.activity = new WeakReference<ImportDataActivity>(activity);
	}

	private class DownloadFileAsync extends AsyncTask<String, Integer, String> implements Observer {
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
		protected String doInBackground(String... params) {
			HttpDownloader downloader = new HttpDownloader(DICT_URL, zipFile);
			downloader.addObserver(this);
			downloader.start();
			checkMd5();
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);

			if (activity.get() == null) {
				Log.w("DownloadFileAsync", "onPostExecute() skipped -- no activity");
			} else {
				activity.get().changeStatus(ImportDataActivity.Status.STATUS_DOWNLOAD_COMPLETED);
			}
			unzipTask.execute(zipFile, rootDir);
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			// TODO
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

		private void checkMd5() {
			try {
				String md5 = Md5.getMd5Sum(zipFile);
				File md5File = new File(rootDir, TEMP_MD5_FILE);
				HttpDownloader downloader = new HttpDownloader(MD5_URL, md5File);
				downloader.start();

				String remoteMd5 = FileReader.readFile(md5File);
				if (!md5.equalsIgnoreCase(remoteMd5)) {
					// TODO
				}
				md5File.delete();
			} catch (IOException e) {
				// TODO
			}
		}
	}

	private class UnzipAsync extends AsyncTask<File, Integer, String> implements Observer {
		private File zipFile = null;

		@Override
		protected String doInBackground(File... params) {
			zipFile = params[0];
			Unzip unzip = new Unzip(zipFile, params[1]);
			unzip.addObserver(this);
			unzip.start();
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);

			if (zipFile != null && zipFile.exists()) {
				zipFile.delete();
			}

			if (activity.get() == null) {
				Log.w("UnzipAsync", "onPostExecute() skipped -- no activity");
			} else {
				activity.get().changeStatus(ImportDataActivity.Status.STATUS_INSTALL_COMPLETED);
			}
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			// TODO
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
