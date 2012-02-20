package com.nilhcem.frcndict.about;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.preference.PreferenceManager;
import android.util.Log;

import com.nilhcem.frcndict.R;
import com.nilhcem.frcndict.core.Config;
import com.nilhcem.frcndict.database.DatabaseHelper;
import com.nilhcem.frcndict.settings.SettingsActivity;

/* package-private */
final class JavascriptInterface {
	private static final String TAG = "JavascriptInterface";
	private static final String VERSION_SEPARATOR = "-";
	private static final String THEME_DEFAULT = "./theme-default.css";
	private static final String THEME_DARK = "./theme-dark.css";

	private final Context mParentContext;
	private final Dialog mDialog;

	JavascriptInterface(Context parent, Dialog dialog) {
		mParentContext = parent;
		mDialog = dialog;
	}

	public String getAppName() {
		return mParentContext.getString(R.string.app_name);
	}

	public String getAppVersion() {
		String version;
		try {
			PackageInfo pInfo = mParentContext.getPackageManager().getPackageInfo(mParentContext.getPackageName(), 0);
			version = pInfo.versionName;
		} catch (NameNotFoundException e) {
			if (Config.LOGGING) {
				Log.e(TAG, "fillVersions() exception", e);
			}
			version = "";
		}
		return version;
	}

	public String getDbVersion() {
		DatabaseHelper db = DatabaseHelper.getInstance();
		db.open();
		String version = db.getDbVersion();
		db.close();
		return convertDbVersionToFormattedDateVersion(version);
	}

	public void closeDialog() {
		mDialog.dismiss();
	}

	public String getTheme() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mParentContext);
		return (prefs.getBoolean(SettingsActivity.KEY_DARK_THEME, false))
			? JavascriptInterface.THEME_DARK : JavascriptInterface.THEME_DEFAULT;
	}

	private String convertDbVersionToFormattedDateVersion(String version) {
		return (version.length() > 8)
			? new StringBuilder()
			.append(version.substring(0, 4))
			.append(JavascriptInterface.VERSION_SEPARATOR)
			.append(version.substring(4, 6))
			.append(JavascriptInterface.VERSION_SEPARATOR)
			.append(version.substring(6, 8))
			.toString()
			: "";
	}
}
