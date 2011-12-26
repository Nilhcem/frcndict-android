package com.nilhcem.frcndict;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public final class ImportDataActivity extends Activity {
	private static final String PERCENT_CHAR = "%";

	private Button mDownloadButton;
	private Button mExitButton;

	private ProgressBar mDownloadBar;
	private TextView mDownloadPercent;
	private ProgressBar mInstallBar;
	private TextView mInstallPercent;
	private ApplicationController application;

	private View mImportButtonsLayout;
	private View mImportProgressLayout;

	private AlertDialog completedDialog;
	private AlertDialog errorDialog;
	private int curErrorId = 0;

	private Status curStatus;

	public static enum Status {
		STATUS_BEGIN,
		STATUS_DOWNLOAD_STARTED,
		STATUS_DOWNLOAD_COMPLETED,
		STATUS_INSTALL_COMPLETED;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.import_data);
		application = (ApplicationController) getApplication();
		application.importService.resetTasks();

		initButtons();
		initLayouts();
		initProgressData();
		initDialogs();

		restore(savedInstanceState);
		application.importService.setActivity(this);
	}

	private void restore(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			curStatus = (Status) savedInstanceState.get("status");
			mDownloadPercent.setText(savedInstanceState.getCharSequence("dl-percent"));
			mInstallPercent.setText(savedInstanceState.getCharSequence("in-percent"));
			curErrorId = savedInstanceState.getInt("error");
		} else {
			curStatus = Status.STATUS_BEGIN;
		}
		changeStatus(curStatus);
		if (curErrorId != 0) {
			displayError(curErrorId);
		}
	}

	private void initButtons() {
		mDownloadButton = (Button) findViewById(R.id.importDwnldBtn);
		mExitButton = (Button) findViewById(R.id.importExitBtn);

		mDownloadButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				application.importService.startDownload();
				changeStatus(Status.STATUS_DOWNLOAD_STARTED);
			}
		});

		mExitButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ImportDataActivity.this.finish();
			}
		});
	}

	private void initLayouts() {
		mImportButtonsLayout = findViewById(R.id.importButtonsLayout);
		mImportProgressLayout = findViewById(R.id.importProgressLayout);
	}

	private void initProgressData() {
		mDownloadBar = (ProgressBar) findViewById(R.id.importDownloadingBar);
		mInstallBar = (ProgressBar) findViewById(R.id.importInstallingBar);

		mDownloadPercent = (TextView) findViewById(R.id.importDownloadingPercent);
		mInstallPercent = (TextView) findViewById(R.id.importInstallingPercent);
	}

	private void initDialogs() {
		completedDialog = new AlertDialog.Builder(this)
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

		errorDialog = new AlertDialog.Builder(this)
			.setTitle(R.string.import_err_dialog_title)
			.setIcon(android.R.drawable.ic_delete)
			.setPositiveButton(R.string.import_err_retry, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// Reload activity
					Intent intent = getIntent();
					intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
					finish();
					overridePendingTransition(0, 0);
				    startActivity(intent);
				}
			})
			.setNegativeButton(R.string.import_err_cancel, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					ImportDataActivity.this.finish();
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
		outState.putInt("error", curErrorId);
		outState.putSerializable("status", curStatus);
		outState.putCharSequence("dl-percent", mDownloadPercent.getText());
		outState.putCharSequence("in-percent", mInstallPercent.getText());
		completedDialog.dismiss();
		errorDialog.dismiss();
	}

	public void changeStatus(Status newStatus) {
		if (newStatus.equals(Status.STATUS_BEGIN)) {
			mImportButtonsLayout.setVisibility(View.VISIBLE);
			mImportProgressLayout.setVisibility(View.GONE);
		} else {
			mImportButtonsLayout.setVisibility(View.GONE);
			mImportProgressLayout.setVisibility(View.VISIBLE);

			if (!newStatus.equals(Status.STATUS_DOWNLOAD_STARTED)) {
				mDownloadPercent.setText(getString(R.string.import_done));

				if (newStatus.equals(Status.STATUS_DOWNLOAD_COMPLETED)) {
					updateProgressData(false, 0);
				} else if (newStatus.equals((Status.STATUS_INSTALL_COMPLETED))) {
					mInstallPercent.setText(getString(R.string.import_done));
					completedDialog.show();
				}
			}
		}
		curStatus = newStatus;
	}

	public void displayError(int strId) {
		errorDialog.setMessage(getString(strId));
		errorDialog.show();
		curErrorId = strId;
	}
}