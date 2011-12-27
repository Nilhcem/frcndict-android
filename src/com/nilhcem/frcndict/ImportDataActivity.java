package com.nilhcem.frcndict;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nilhcem.frcndict.services.ImportDataService;

public final class ImportDataActivity extends Activity {
	private static final String PERCENT_CHAR = "%";
	private ImportDataService mService;

	private Button mDownloadButton;
	private Button mExitButton;
	private Button mCancelButton;

	private ProgressBar mDownloadBar;
	private TextView mDownloadPercent;
	private ProgressBar mInstallBar;
	private TextView mInstallPercent;

	private View mImportButtonsLayout;
	private View mImportProgressLayout;

	private AlertDialog mCompletedDialog;
	private AlertDialog mErrorDialog;

	private static final int NOTIFICATION_ID = 1;
	private NotificationManager mNotificationMngr;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.import_data);
		mService = ((ApplicationController) getApplication()).importService;

		initDialogs();
		initButtons();
		initLayouts();
		initNotification();
		initProgressData();

		restore(savedInstanceState);
	}

	@Override
	protected void onPause() {
		super.onPause();
		mService.setActivity(null); // doesn't need to update UI since application is paused
	}

	@Override
	protected void onResume() {
		super.onResume();
		mService.setActivity(this);
		updateDisplay();
	}

	private void restore(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			mDownloadPercent.setText(savedInstanceState.getCharSequence("dl-percent"));
			mInstallPercent.setText(savedInstanceState.getCharSequence("in-percent"));
		}
		displayError(mService.getErrorId());
	}

	private void initButtons() {
		mDownloadButton = (Button) findViewById(R.id.importDwnldBtn);
		mDownloadButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				displayImportNotification();
				mService.startDownload();
			}
		});

		mExitButton = (Button) findViewById(R.id.importExitBtn);
		mExitButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ImportDataActivity.this.finish();
			}
		});

		mCancelButton = (Button) findViewById(R.id.importCancelBtn);
		mCancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mService.cancelTasks();
				stopActivityAndStartIntent(null);
			}
		});
	}

	private void initLayouts() {
		mImportButtonsLayout = findViewById(R.id.importButtonsLayout);
		mImportProgressLayout = findViewById(R.id.importProgressLayout);
	}

	private void initNotification() {
		mNotificationMngr = ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE));
	}

	private void initProgressData() {
		mDownloadBar = (ProgressBar) findViewById(R.id.importDownloadingBar);
		mInstallBar = (ProgressBar) findViewById(R.id.importInstallingBar);

		mDownloadPercent = (TextView) findViewById(R.id.importDownloadingPercent);
		mInstallPercent = (TextView) findViewById(R.id.importInstallingPercent);
	}

	private void initDialogs() {
		mCompletedDialog = new AlertDialog.Builder(this)
			.setTitle(R.string.import_dialog_title)
			.setMessage(R.string.import_dialog_msg)
			.setIcon(android.R.drawable.checkbox_on_background)
			.setPositiveButton(R.string.import_dialog_btn, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					stopActivityAndStartIntent(new Intent(ImportDataActivity.this, CheckDataActivity.class));
				}
			})
			.create();

		mErrorDialog = new AlertDialog.Builder(this)
			.setTitle(R.string.import_err_dialog_title)
			.setIcon(android.R.drawable.ic_delete)
			.setPositiveButton(R.string.import_err_retry, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					stopActivityAndStartIntent(getIntent()); //reload activity
				}
			})
			.setNegativeButton(R.string.import_err_cancel, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					stopActivityAndStartIntent(null); // exit
				}
			})
			.create();
	}

	public void updateProgressData(boolean downloadBar, Integer progress) {
		final String progressStr = Integer.toString(progress) + PERCENT_CHAR;

		if (downloadBar) {
			mDownloadBar.setProgress(progress);
			mDownloadPercent.setText(progressStr);
		} else {
			mInstallBar.setProgress(progress);
			mInstallPercent.setText(progressStr);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putCharSequence("dl-percent", mDownloadPercent.getText());
		outState.putCharSequence("in-percent", mInstallPercent.getText());
		mCompletedDialog.dismiss();
		mErrorDialog.dismiss();
	}

	public void updateDisplay() {
		int status = mService.getStatus();

		if (status == ImportDataService.STATUS_BEGIN) {
			mImportButtonsLayout.setVisibility(View.VISIBLE);
			mImportProgressLayout.setVisibility(View.GONE);
		} else {
			mImportButtonsLayout.setVisibility(View.GONE);
			mImportProgressLayout.setVisibility(View.VISIBLE);

			if (status != ImportDataService.STATUS_DOWNLOAD_STARTED) {
				updateProgressData(true, 100);

				if (status == ImportDataService.STATUS_INSTALL_COMPLETED) {
					mInstallPercent.setText(getString(R.string.import_done));
					hideNotification();
					mCompletedDialog.show();
				}
			}
		}
	}

	public void displayError(int errorId) {
		if (errorId != 0) {
			mErrorDialog.setMessage(getString(errorId));
			mErrorDialog.show();
		}
	}

	private void stopActivityAndStartIntent(Intent intent) {
		hideNotification();
		finish();

		if (intent != null) {
			intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			overridePendingTransition(0, 0);
		}

		((ApplicationController) getApplication()).importService = new ImportDataService(); // reset data
		if (intent != null) {
			startActivity(intent);
		}
	}

	private void displayImportNotification() {
		String title = getString(R.string.import_notification_import_title);
		String message = getString(R.string.import_notification_import_msg);

		// Instantiate the notification
		Notification notification = new Notification(
				android.R.drawable.stat_notify_sync_noanim,
				title, 0l);
		notification.flags |= Notification.FLAG_ONGOING_EVENT;
		notification.flags |= Notification.FLAG_ONLY_ALERT_ONCE;

		notification.when = System.currentTimeMillis();

		// Define the notification message and pending intent
		Intent notificationIntent = new Intent(this, ImportDataActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
		notification.setLatestEventInfo(getApplicationContext(), title, message, contentIntent);

		// Display notification
		mNotificationMngr.notify(NOTIFICATION_ID, notification);
	}

	private void hideNotification() {
		mNotificationMngr.cancel(NOTIFICATION_ID);
	}
}
