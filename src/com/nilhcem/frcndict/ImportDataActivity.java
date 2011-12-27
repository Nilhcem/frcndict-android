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

import com.nilhcem.frcndict.services.ImportDataService;

public final class ImportDataActivity extends Activity {
	private static final String PERCENT_CHAR = "%";
	private ImportDataService service;

	private Button mDownloadButton;
	private Button mExitButton;
	private Button mCancelButton;

	private ProgressBar mDownloadBar;
	private TextView mDownloadPercent;
	private ProgressBar mInstallBar;
	private TextView mInstallPercent;

	private View mImportButtonsLayout;
	private View mImportProgressLayout;

	private AlertDialog completedDialog;
	private AlertDialog errorDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.import_data);
		service = ((ApplicationController) getApplication()).importService;

		initDialogs();
		initButtons();
		initLayouts();
		initProgressData();

		restore(savedInstanceState);
	}

	@Override
	protected void onPause() {
		super.onPause();
		service.setActivity(null); // doesn't need to update UI since application is paused
	}

	@Override
	protected void onResume() {
		super.onResume();
		service.setActivity(this);
		updateDisplay();
	}

	private void restore(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			mDownloadPercent.setText(savedInstanceState.getCharSequence("dl-percent"));
			mInstallPercent.setText(savedInstanceState.getCharSequence("in-percent"));
		}
		displayError(service.getErrorId());
	}

	private void initButtons() {
		mDownloadButton = (Button) findViewById(R.id.importDwnldBtn);
		mDownloadButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				service.startDownload();
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
				service.cancelTasks();
				stopActivityAndStartIntent(null);
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
					stopActivityAndStartIntent(new Intent(ImportDataActivity.this, CheckDataActivity.class));
				}
			})
			.create();

		errorDialog = new AlertDialog.Builder(this)
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
		completedDialog.dismiss();
		errorDialog.dismiss();
	}

	public void updateDisplay() {
		int status = service.getStatus();

		if (status == ImportDataService.STATUS_BEGIN) {
			mImportButtonsLayout.setVisibility(View.VISIBLE);
			mImportProgressLayout.setVisibility(View.GONE);
		} else {
			mImportButtonsLayout.setVisibility(View.GONE);
			mImportProgressLayout.setVisibility(View.VISIBLE);

			if (status != ImportDataService.STATUS_DOWNLOAD_STARTED) {
				mDownloadPercent.setText(getString(R.string.import_done));

				if (status == ImportDataService.STATUS_INSTALL_COMPLETED) {
					mInstallPercent.setText(getString(R.string.import_done));
					completedDialog.show();
				}
			}
		}
	}

	public void displayError(int errorId) {
		if (errorId != 0) {
			errorDialog.setMessage(getString(errorId));
			errorDialog.show();
		}
	}

	private void stopActivityAndStartIntent(Intent intent) {
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
}
