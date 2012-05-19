package com.nilhcem.frcndict.updatedb;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;

import com.nilhcem.frcndict.CheckDataActivity;
import com.nilhcem.frcndict.R;

public final class ImportActivity extends AbstractImportUpdateActivity {
	private AlertDialog mStorageDialog;

	public ImportActivity() {
		super();
		mImport = true;

		mStartServiceListener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
			    String state = Environment.getExternalStorageState();
			    if (Environment.MEDIA_MOUNTED.equals(state)) {
					mStorageDialog.show();
			    } else {
					startProcess(Boolean.FALSE);
			    }
			}
		};

		mCompletedListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				stopActivityAndStartIntent(new Intent(ImportActivity.this, CheckDataActivity.class));
			}
		};
	}

	@Override
	protected void initDialogs() {
		mStorageDialog = new AlertDialog.Builder(this)
			.setTitle(R.string.import_storage_dialog_title)
			.setMessage(R.string.import_storage_dialog_msg)
			.setIcon(R.drawable.ic_menu_help)
			.setPositiveButton(R.string.import_storage_dialog_sdcard, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					startProcess(Boolean.TRUE);
				}
			})
			.setNeutralButton(R.string.import_storage_dialog_device, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					startProcess(Boolean.FALSE);
				}
			})
			.create();
		super.initDialogs();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// No call for super(). Bug on API Level > 11
		if (mStorageDialog.isShowing()) {
			outState.putBoolean("select-storage", true);
			mStorageDialog.dismiss();
		}
	}

	@Override
	protected void restore(Bundle savedInstanceState) {
		super.restore(savedInstanceState);

		if (savedInstanceState != null && savedInstanceState.getBoolean("select-storage", false)) {
			mStorageDialog.show();
		}
	}
}
