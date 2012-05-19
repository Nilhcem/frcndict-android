package com.nilhcem.frcndict.updatedb;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.nilhcem.frcndict.R;
import com.nilhcem.frcndict.core.AbstractDictActivity;
import com.nilhcem.frcndict.core.layout.ProgressBar;

public abstract class AbstractImportUpdateActivity extends Activity {
	protected boolean mImport; // true if import, false if update
	protected View.OnClickListener mStartServiceListener;
	protected DialogInterface.OnClickListener mCompletedListener;

	// Welcome form
	protected TextView mWelcomeTitle;
	protected TextView mImportMessage;
	protected View mStartLayout;
	protected Button mStartService;
	protected Button mQuitActivity;

	// Progress
	private View mProgressLayout;
	protected ProgressBar mDownloadProgress;
	protected ProgressBar mInstallProgress;
	protected Button mCancelProgressButton;

	private AlertDialog mCompletedDialog;
	private AlertDialog mErrorDialog;
	private AlertDialog mUpdateDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		AbstractDictActivity.checkForNightModeTheme(this, null);
		setContentView(R.layout.import_update_data);

		initWelcomeTitle();
		initDialogs();
		initButtons();
		initLayouts();
		initProgressData();
		restore(savedInstanceState);
	}

	@Override
	protected void onResume() {
		super.onResume();
		ImportUpdateService.setActivity(this);
		refreshProgresses();
		updateDisplay();
	}

	@Override
	protected void onPause() {
		super.onPause();
		ImportUpdateService.setActivity(null);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// No call for super(). Bug on API Level > 11
		mCompletedDialog.dismiss();
		mErrorDialog.dismiss();
		mUpdateDialog.dismiss();
	}

	protected void restore(Bundle savedInstanceState) {
		if (ImportUpdateService.getInstance() != null) {
			displayError(ImportUpdateService.getInstance().getErrorId());
		}
	}

	public void displayError(int errorId) {
		if (errorId != 0) {
			if (errorId == R.string.import_err_too_old) {
				mUpdateDialog.show();
			} else {
				mErrorDialog.setMessage(getString(errorId));
				mErrorDialog.show();
			}
		}
	}

	public void updateProgressData(int progressId, Integer progress) {
		if (progressId == ImportUpdateService.PROGRESS_BAR_DOWNLOAD) {
			mDownloadProgress.setProgress(progress);
		} else if (progressId == ImportUpdateService.PROGRESS_BAR_INSTALL) {
			mInstallProgress.setProgress(progress);
		}
	}

	public void updateDisplay() {
		int status = (ImportUpdateService.getInstance() == null)
				? ImportUpdateService.STATUS_UNSTARTED : ImportUpdateService.getInstance().getStatus();

		if (status == ImportUpdateService.STATUS_UNSTARTED) {
			mImportMessage.setText(mImport ? R.string.import_welcome_msg : R.string.update_welcome_msg);
			mStartLayout.setVisibility(View.VISIBLE);
			mProgressLayout.setVisibility(View.GONE);
		} else {
			mImportMessage.setText(mImport ? R.string.import_processing_msg : R.string.update_processing_msg);
			mStartLayout.setVisibility(View.GONE);
			mProgressLayout.setVisibility(View.VISIBLE);

			if (status == ImportUpdateService.STATUS_COMPLETED) {
				mCompletedDialog.show();
			}
		}
	}

	private void initWelcomeTitle() {
		mImportMessage = (TextView) findViewById(R.id.importMessage);
		mWelcomeTitle = (TextView) findViewById(R.id.importTitle);
		mWelcomeTitle.setText(mImport ? R.string.import_welcome_title : R.string.update_welcome_title);
	}

	protected void initDialogs() {
		// Exit
		DialogInterface.OnClickListener exitClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				stopActivityAndStartIntent(null); // exit
			}
		};

		mCompletedDialog = new AlertDialog.Builder(this)
			.setTitle(R.string.import_dialog_title)
			.setMessage(mImport ? R.string.import_dialog_msg : R.string.update_dialog_msg)
			.setIcon(R.drawable.checkbox_on_background)
			.setCancelable(false)
			.setPositiveButton(R.string.import_dialog_btn, mCompletedListener)
			.create();

		mErrorDialog = new AlertDialog.Builder(this)
			.setTitle(R.string.import_err_dialog_title)
			.setIcon(R.drawable.ic_delete)
			.setCancelable(false)
			.setPositiveButton(R.string.import_err_retry, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					stopActivityAndStartIntent(getIntent()); //reload activity
				}
			})
			.setNegativeButton(R.string.import_err_cancel, exitClickListener)
			.create();

		mUpdateDialog = new AlertDialog.Builder(this)
			.setTitle(R.string.import_err_dialog_title)
			.setMessage(R.string.import_err_too_old)
			.setIcon(R.drawable.ic_delete)
			.setCancelable(false)
			.setPositiveButton(R.string.import_err_too_old_update, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// Redirect to market
					Uri marketUri = Uri.parse("market://details?id=" + getApplication().getClass().getPackage().getName());
					Intent marketIntent = new Intent(Intent.ACTION_VIEW, marketUri);
					stopActivityAndStartIntent(marketIntent);
				}
			})
			.setNegativeButton(R.string.import_quit_activity_btn, exitClickListener)
			.create();
	}

	private void initButtons() {
		mStartService = (Button) findViewById(R.id.importStartService);
		mStartService.setText(mImport ? R.string.import_start_service_btn : R.string.update_start_service_btn);
		mStartService.setOnClickListener(mStartServiceListener);

		mQuitActivity = (Button) findViewById(R.id.importQuitActivity);
		mQuitActivity.setText(mImport ? R.string.import_quit_activity_btn : R.string.update_quit_activity_btn);
		mQuitActivity.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		mCancelProgressButton = (Button) findViewById(R.id.importCancelButton);
		mCancelProgressButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onCancelProgressButtonClicked();
			}
		});
	}

	private void initLayouts() {
		mStartLayout = findViewById(R.id.importStartLayout);
		mProgressLayout = findViewById(R.id.importProgressLayout);
	}

	protected void initProgressData() {
		mDownloadProgress = (ProgressBar) findViewById(R.id.importDownloadProgress);
		mDownloadProgress.setTitle(R.string.import_download_text);

		mInstallProgress = (ProgressBar) findViewById(R.id.importInstallProgress);
		mInstallProgress.setTitle(R.string.import_install_text);
	}

	// reset progress values if activity has been resumed
	protected void refreshProgresses() {
		ImportUpdateService service = ImportUpdateService.getInstance();

		if (service != null) {
			mDownloadProgress.setProgress(service.getPercent(ImportUpdateService.PROGRESS_BAR_DOWNLOAD));
			mInstallProgress.setProgress(service.getPercent(ImportUpdateService.PROGRESS_BAR_INSTALL));
		}
	}

	protected void startProcess(Boolean installOnSdcard) {
		Intent intent = new Intent(this, ImportUpdateService.class);
		intent.putExtra(ImportUpdateService.INTENT_IMPORT_KEY, mImport);

		if (mImport) {
			intent.putExtra(ImportUpdateService.INTENT_SDCARD_KEY, installOnSdcard);
		}
		startService(intent);
	}

	protected void onCancelProgressButtonClicked() {
		stopActivityAndStartIntent(null);
	}

	protected void stopActivityAndStartIntent(Intent intent) {
		stopService(new Intent(this, ImportUpdateService.class));
		finish();

		if (ImportUpdateService.getInstance() != null) {
			ImportUpdateService.getInstance().setAsFinished();
		}

		if (intent != null) {
			int flags = intent.getFlags();
			flags |= Intent.FLAG_ACTIVITY_NO_ANIMATION;
			intent.setFlags(flags);
			overridePendingTransition(0, 0);
			startActivity(intent);
		}
	}
}
