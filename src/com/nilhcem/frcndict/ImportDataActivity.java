package com.nilhcem.frcndict;

import java.io.File;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nilhcem.frcndict.utils.FileReader;
import com.nilhcem.frcndict.utils.HttpDownloader;
import com.nilhcem.frcndict.utils.Md5;
import com.nilhcem.frcndict.utils.Unzip;

public final class ImportDataActivity extends Activity {
	private static final String PERCENT_CHAR = "%";

	private Button mDownloadButton;
	private Button mExitButton;

	private ProgressBar mDownloadBar;
	private TextView mDownloadPercent;
	private ProgressBar mInstallBar;
	private TextView mInstallPercent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.import_data);

		initButtons();
		initProgressData();
	}

	private void initButtons() {
		mDownloadButton = (Button) findViewById(R.id.importDwnldBtn);
		mExitButton = (Button) findViewById(R.id.importExitBtn);

		mDownloadButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new DownloadFileAsync().execute("");
			}
		});

		mExitButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ImportDataActivity.this.finish();
			}
		});
	}

	private void initProgressData() {
		mDownloadBar = (ProgressBar) findViewById(R.id.importDownloadingBar);
		mInstallBar = (ProgressBar) findViewById(R.id.importInstallingBar);

		mDownloadPercent = (TextView) findViewById(R.id.importDownloadingPercent);
		mInstallPercent = (TextView) findViewById(R.id.importInstallingPercent);
	}

	private class DownloadFileAsync extends AsyncTask<String, Integer, String> implements Observer {
		private static final String DICT_URL = "http://192.168.1.2/cfdict/dictionary.zip";
		private static final String MD5_URL = "http://192.168.1.2/cfdict/md5sum";
		private static final String TEMP_ZIP_FILE = "download.tmp";
		private static final String TEMP_MD5_FILE = "md5sum";
		private File rootDir;
		private File zipFile;

		public DownloadFileAsync() {
			super();

			ApplicationController app = (ApplicationController) getApplication();
			rootDir = app.getRootDir();
			zipFile = new File(rootDir, TEMP_ZIP_FILE);
		}

		@Override
		protected String doInBackground(String... params) {
			HttpDownloader downloader = new HttpDownloader(DICT_URL, zipFile);
			downloader.addObserver(this);
			downloader.start();
			checkMd5();
			return null;
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

		@Override
		protected void onCancelled() {
			super.onCancelled();
			// TODO
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			findViewById(R.id.importButtonsLayout).setVisibility(View.GONE);
			findViewById(R.id.importProgressLayout).setVisibility(View.VISIBLE);
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			mDownloadPercent.setText(ImportDataActivity.this.getString(R.string.import_done));
			new UnzipAsync().execute(zipFile, rootDir);
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
			mDownloadBar.setProgress(values[0]);
			mDownloadPercent.setText(Integer.toString(values[0]) + PERCENT_CHAR);
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
		protected void onCancelled() {
			super.onCancelled();
			// TODO
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mInstallPercent.setText("0%");
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (zipFile != null && zipFile.exists()) {
				zipFile.delete();
			}
			mInstallPercent.setText(ImportDataActivity.this.getString(R.string.import_done));
			// launch pop up

			// Create completed dialog
			AlertDialog dialog = new AlertDialog.Builder(ImportDataActivity.this)
				.setTitle(R.string.import_dialog_title)
				.setMessage(R.string.import_dialog_msg)
				.setIcon(android.R.drawable.checkbox_on_background)
				.setPositiveButton(R.string.import_dialog_btn, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(ImportDataActivity.this, CheckDataActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
						startActivity(intent);
					}
				})
				.create();

			dialog.show();
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
			mInstallBar.setProgress(values[0]);
			mInstallPercent.setText(Integer.toString(values[0]) + PERCENT_CHAR);
		}
	}
}
