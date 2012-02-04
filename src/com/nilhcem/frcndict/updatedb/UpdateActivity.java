package com.nilhcem.frcndict.updatedb;

import android.content.DialogInterface;
import android.view.View;

import com.nilhcem.frcndict.R;
import com.nilhcem.frcndict.core.ProgressBar;

public final class UpdateActivity extends AbstrImportUpdateActivity {
	private ProgressBar mBackupProgress;
	private ProgressBar mRestoreProgress;

	public UpdateActivity() {
		mImport = false;

		mStartServiceListener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startProcess(null);
			}
		};

		mCompletedListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				stopActivityAndStartIntent(null);
			}
		};
	}

	@Override
	public void onBackPressed() {
		if (ImportUpdateService.getInstance() != null
				&& ImportUpdateService.getInstance().getStatus() != ImportUpdateService.STATUS_UNSTARTED) {
			moveTaskToBack(true); // act like home button (because we can't use the application when it is updating dictionary)
		} else {
			super.onBackPressed();
		}
	}

	@Override
	public void updateProgressData(int progressId, Integer progress) {
		if (progressId == ImportUpdateService.PROGRESS_BAR_BACKUP) {
			mBackupProgress.setProgress(progress);
		} else if (progressId == ImportUpdateService.PROGRESS_BAR_RESTORE) {
			mRestoreProgress.setProgress(progress);
		} else {
			super.updateProgressData(progressId, progress);
		}
	}

	@Override
	protected void initProgressData() {
		super.initProgressData();

		mBackupProgress = (ProgressBar) findViewById(R.id.importBackupProgress);
		mBackupProgress.setTitle(R.string.update_backup_text);
		mBackupProgress.setVisibility(View.VISIBLE);

		mRestoreProgress = (ProgressBar) findViewById(R.id.importRestoreProgress);
		mRestoreProgress.setTitle(R.string.update_restore_text);
		mRestoreProgress.setVisibility(View.VISIBLE);
	}

	@Override
	protected void refreshProgresses() {
		ImportUpdateService service = ImportUpdateService.getInstance();

		if (service != null) {
			mBackupProgress.setProgress(service.getPercent(ImportUpdateService.PROGRESS_BAR_BACKUP));
			mRestoreProgress.setProgress(service.getPercent(ImportUpdateService.PROGRESS_BAR_RESTORE));
		}
		super.refreshProgresses();
	}

	@Override
	protected void onCancelProgressButtonClicked() {
		super.onCancelProgressButtonClicked();
		CheckForUpdatesService.displayUpdateNotification(this);
	}
}
