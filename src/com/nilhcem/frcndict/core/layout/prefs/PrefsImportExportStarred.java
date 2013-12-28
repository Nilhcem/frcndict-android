package com.nilhcem.frcndict.core.layout.prefs;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.preference.Preference;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.Toast;

import com.nilhcem.frcndict.R;
import com.nilhcem.frcndict.core.Log;
import com.nilhcem.frcndict.database.StarredDbHelper;
import com.nilhcem.frcndict.updatedb.xml.BackupXmlWriter;
import com.nilhcem.frcndict.updatedb.xml.RestoreXmlReader;
import com.nilhcem.frcndict.utils.FileHandler;

final class PrefsImportExportStarred extends Preference {

	private WeakReference<Context> mContext;

	public PrefsImportExportStarred(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	public PrefsImportExportStarred(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView(context);
	}

	private void initView(Context context) {
		mContext = new WeakReference<Context>(context);
		setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				final CharSequence[] items = {
					getContext().getString(R.string.backup_restore_backup),
					getContext().getString(R.string.backup_restore_restore)
				};

				AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
				builder.setTitle(R.string.settings_db_importexport);
				builder.setItems(items, new DialogInterface.OnClickListener() {
					@Override
					public synchronized void onClick(DialogInterface dialog, int which) {
						if (which == 0) {
							new BackupAsync().execute();
						} else {
							new RestoreAsync().execute();
						}
					}
				});
				AlertDialog alert = builder.create();
				alert.show();
				return false;
			}
		});
	}

	private final class BackupAsync extends AsyncTask<Void, Void, String> {
		private final ProgressDialog mDialog;

		public BackupAsync() {
			mDialog = new ProgressDialog(getContext());
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			if (FileHandler.isSdCardMounted()) {
				mDialog.setCancelable(false);
				mDialog.setMessage(getContext().getString(R.string.backup_restore_processing));
				mDialog.show();
			} else {
				Toast.makeText(getContext(), R.string.backup_restore_nosdcard,
						Toast.LENGTH_LONG).show();
				cancel(true);
			}
		}

		@Override
		protected String doInBackground(Void... params) {
			String path = null;

			if (!isCancelled()) {
				// Write into file
				File xmlFile = FileHandler.getBackupRestoreFile();

				StarredDbHelper helper = null;
				Context context = null;
				if (mContext != null) {
					context = mContext.get();
				}

				try {
					if (context != null) {
						helper = new StarredDbHelper(mContext.get());
					}
					BackupXmlWriter xmlWriter = new BackupXmlWriter(helper, xmlFile);
					xmlWriter.insertHeader(getContext().getApplicationContext());
					xmlWriter.start();
				} catch (IOException ex) {
					Log.e(BackupAsync.class.getSimpleName(), ex, "Failed backing up starred words");
				} finally {
					if (helper != null) {
						helper.close();
					}
				}
				path = xmlFile.getAbsolutePath();
			}
			return path;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (!isCancelled() && !TextUtils.isEmpty(result)) {
				Context c = getContext();
				String str = String.format(Locale.US, c.getString(R.string.backup_restore_saved), result);
				Toast.makeText(c, str, Toast.LENGTH_LONG).show();
			}
			dismissDialog();
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			dismissDialog();
		}

		private void dismissDialog() {
			if (mDialog.isShowing()) {
				mDialog.dismiss();
			}
		}
	}

	private final class RestoreAsync extends AsyncTask<Void, Void, Boolean> {
		private final ProgressDialog mDialog;
		private File mXmlFile;

		public RestoreAsync() {
			mDialog = new ProgressDialog(getContext());
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			String error = null;
			Context c = getContext();
			if (FileHandler.isSdCardMounted()) {
				// Check if file exists
				mXmlFile = FileHandler.getBackupRestoreFile();
				if (!mXmlFile.exists()) {
					error = String.format(Locale.US,
							c.getString(R.string.backup_restore_not_found),
							FileHandler.SD_BACKUP_RESTORE_FILE);
				}
			} else {
				error = c.getString(R.string.backup_restore_nosdcard);
			}

			if (error == null) {
				mDialog.setCancelable(false);
				mDialog.setMessage(c.getString(R.string.backup_restore_processing));
				mDialog.show();
			} else {
				Toast.makeText(c, error, Toast.LENGTH_LONG).show();
				cancel(true);
			}
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			Boolean result = null;

			if (!isCancelled()) {
				StarredDbHelper helper = null;
				Context context = null;
				if (mContext != null) {
					context = mContext.get();
				}

				// Restore starred words from the XML file
				try {
					helper = new StarredDbHelper(context);
					RestoreXmlReader xmlReader = new RestoreXmlReader(helper, mXmlFile);
					xmlReader.start();
					result = Boolean.TRUE;
				} catch (IOException ex) {
					Log.e(RestoreAsync.class.getSimpleName(), ex, "Failed restoring starred words");
					result = Boolean.FALSE;
				} finally {
					if (helper != null) {
						helper.close();
					}
				}
			}
			return result;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (!isCancelled()) {
				int resId;
				if (result == null || result == Boolean.FALSE) {
					resId = R.string.backup_restore_restore_ko;
				} else {
					resId = R.string.backup_restore_restore_ok;
				}
				Toast.makeText(getContext(), resId, Toast.LENGTH_LONG).show();
			}
			dismissDialog();
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			dismissDialog();
		}

		private void dismissDialog() {
			if (mDialog.isShowing()) {
				mDialog.dismiss();
			}
		}
	}
}
