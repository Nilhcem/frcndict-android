package com.nilhcem.frcndict.importdb;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nilhcem.frcndict.CheckDataActivity;
import com.nilhcem.frcndict.R;
import com.nilhcem.frcndict.core.DictActivity;

public final class ImportDataActivity extends DictActivity {
	private static final String PERCENT_CHAR = "%";

	private Button mDownloadButton;
	private Button mExitButton;
	private Button mCancelButton;

	private ProgressBar mDownloadBar;
	private TextView mDownloadPercent;
	private ProgressBar mInstallBar;
	private TextView mInstallPercent;

	private View mImportButtonsLayout;
	private View mImportProgressLayout;

	private AlertDialog mStorageDialog;
	private AlertDialog mCompletedDialog;
	private AlertDialog mErrorDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.import_data);

		initDialogs();
		initButtons();
		initLayouts();
		initProgressData();

		restore(savedInstanceState);
	}

	@Override
	protected void onResume() {
		super.onResume();
		ImportDataService.setActivity(this);
		updateDisplay();
	}

	@Override
	protected void onPause() {
		super.onPause();
		ImportDataService.setActivity(null);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putCharSequence("dl-percent", mDownloadPercent.getText());
		outState.putCharSequence("in-percent", mInstallPercent.getText());

		if (mStorageDialog.isShowing()) {
			outState.putBoolean("select-storage", true);
			mStorageDialog.dismiss();
		}
		mCompletedDialog.dismiss();
		mErrorDialog.dismiss();
	}

	private void restore(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			mDownloadPercent.setText(savedInstanceState.getCharSequence("dl-percent"));
			mInstallPercent.setText(savedInstanceState.getCharSequence("in-percent"));
			if (savedInstanceState.getBoolean("select-storage", false)) {
				mStorageDialog.show();
			}
		}

		if (ImportDataService.getInstance() != null) {
			displayError(ImportDataService.getInstance().getErrorId());
		}
	}

	private void initDialogs() {
		mStorageDialog = new AlertDialog.Builder(this)
			.setTitle(R.string.import_storage_dialog_title)
			.setMessage(R.string.import_storage_dialog_msg)
			.setIcon(android.R.drawable.ic_menu_help)
			.setPositiveButton(R.string.import_storage_dialog_sdcard, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					startDownload(true);
				}
			})
			.setNeutralButton(R.string.import_storage_dialog_device, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					startDownload(false);
				}
			})
			.create();

		mCompletedDialog = new AlertDialog.Builder(this)
			.setTitle(R.string.import_dialog_title)
			.setMessage(R.string.import_dialog_msg)
			.setIcon(android.R.drawable.checkbox_on_background)
			.setNegativeButton(R.string.import_dialog_btn, new DialogInterface.OnClickListener() {
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

	private void initButtons() {
		mDownloadButton = (Button) findViewById(R.id.importDwnldBtn);
		mDownloadButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
			    String state = Environment.getExternalStorageState();
			    if (Environment.MEDIA_MOUNTED.equals(state)) {
					mStorageDialog.show();
			    } else {
					startDownload(false);
			    }
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

	private void stopActivityAndStartIntent(Intent intent) {
		stopService(new Intent(ImportDataActivity.this, ImportDataService.class));
		finish();

		if (ImportDataService.getInstance() != null) {
			ImportDataService.getInstance().resetStatus();
		}

		if (intent != null) {
			intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			overridePendingTransition(0, 0);
			startActivity(intent);
		}
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

	public void updateDisplay() {
		int status;

		if (ImportDataService.getInstance() != null) {
			status = ImportDataService.getInstance().getStatus();
		} else {
			status = ImportDataService.STATUS_BEGIN;
		}

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

	private void startDownload(boolean installOnSDcard) {
		Intent intent = new Intent(ImportDataActivity.this, ImportDataService.class);
		intent.putExtra("install-on-sdcard", installOnSDcard);
		startService(intent);
	}
}
