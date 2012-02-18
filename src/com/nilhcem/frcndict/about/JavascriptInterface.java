package com.nilhcem.frcndict.about;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.preference.PreferenceManager;
import android.util.Log;

import com.nilhcem.frcndict.R;
import com.nilhcem.frcndict.database.DatabaseHelper;
import com.nilhcem.frcndict.settings.SettingsActivity;

/* package-private */
final class JavascriptInterface {
	private static final String TAG = "JavascriptInterface";
	private static final String VERSION_SEPARATOR = "-";
	private static final String THEME_DEFAULT = "./theme-default.css";
	private static final String THEME_DARK = "./theme-dark.css";

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
		DatabaseHelper db = DatabaseHelper.getInstance();
		db.open();
		String version = db.getDbVersion();
		db.close();
		return convertDbVersionToFormattedDateVersion(version);
	}

	public void closeDialog() {
		dialog.dismiss();
	}

	public String getTheme() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(parentContext);
		if (prefs.getBoolean(SettingsActivity.KEY_DARK_THEME, false)) {
			return JavascriptInterface.THEME_DARK;
		}
		return JavascriptInterface.THEME_DEFAULT;
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
}
