package com.nilhcem.frcndict.about;

import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

import com.nilhcem.frcndict.R;
import com.nilhcem.frcndict.database.DatabaseHelper;

/* package-private */
final class JavascriptInterface {
	private static final String TAG = "JavascriptInterface";
	private static final String VERSION_SEPARATOR = "-";

	private Context parentContext;
	private Dialog dialog;

	JavascriptInterface(Context parent, Dialog dialog) {
		this.parentContext = parent;
		this.dialog = dialog;
	}

	public String getAppName() {
		return parentContext.getString(R.string.app_name);
	}

	public String getAppVersion() {
		PackageInfo pInfo = null;
		try {
			pInfo = parentContext.getPackageManager().getPackageInfo(parentContext.getPackageName(), 0);
		} catch (NameNotFoundException e) {
			Log.e(TAG, "fillVersions() exception", e);
		}
		return pInfo.versionName;
	}

	public String getDbVersion() {
		return convertDbVersionToFormattedDateVersion(DatabaseHelper.getInstance().getDbVersion());
	}

	private String convertDbVersionToFormattedDateVersion(String version) {
		if (version.length() > 8) {
			return new StringBuilder()
				.append(version.substring(0, 4))
				.append(JavascriptInterface.VERSION_SEPARATOR)
				.append(version.substring(4, 6))
				.append(JavascriptInterface.VERSION_SEPARATOR)
				.append(version.substring(6, 8))
				.toString();
		}
		return "";
	}

	public void closeDialog() {
		dialog.dismiss();
	}
}
