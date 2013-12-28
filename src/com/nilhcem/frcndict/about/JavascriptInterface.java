package com.nilhcem.frcndict.about;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.preference.PreferenceManager;

import com.nilhcem.frcndict.R;
import com.nilhcem.frcndict.core.Config;
import com.nilhcem.frcndict.core.Log;
import com.nilhcem.frcndict.settings.SettingsActivity;

/**
 * Note: if some changes are made to this class, please modify proguard.cfg
 */
/* package-private */ final class JavascriptInterface {

	private static final String THEME_DEFAULT = "./res/theme-default.css";
	private static final String THEME_DARK = "./res/theme-dark.css";

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
		} catch (NameNotFoundException ex) {
			Log.e(JavascriptInterface.class.getSimpleName(), ex, "Failed to get version");
			version = "";
		}
		return version;
	}

	public String getDbVersion() {
		return Config.DATABASE_VERSION;
	}

	public void closeDialog() {
		mDialog.dismiss();
	}

	public String getTheme() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mParentContext);
		return (prefs.getBoolean(SettingsActivity.KEY_DARK_THEME, false))
			? JavascriptInterface.THEME_DARK : JavascriptInterface.THEME_DEFAULT;
	}
}
