package com.nilhcem.frcndict;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.nilhcem.frcndict.core.DictActivity;

public final class AboutActivity extends DictActivity {
	private static final String TAG = "AboutActivity";
	private static final String VERSION_SEPARATOR = "-";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!isFinishing()) {
			setContentView(R.layout.about);
			fillVersions();
		}
	}

	private void fillVersions() {
		// Get app version
		PackageInfo pInfo = null;
		try {
			pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
		} catch (NameNotFoundException e) {
			Log.e(TAG, "fillVersions() exception", e);
		}
		String appVersion = pInfo.versionName;

		// Get database version
		String dbVersion = convertDbVersionToFormattedDateVersion(db.getDbVersion());

		// Display versions
		String aboutStr = getString(R.string.about_app_version, appVersion);
		String poweredbyStr = getString(R.string.about_db_version, dbVersion);

		((TextView) findViewById(R.id.aboutAppVersion)).setText(aboutStr);
		((TextView) findViewById(R.id.aboutDbVersion)).setText(poweredbyStr);
	}

	private String convertDbVersionToFormattedDateVersion(String version) {
		if (version.length() > 8) {
			return new StringBuilder()
				.append(version.substring(0, 4))
				.append(AboutActivity.VERSION_SEPARATOR)
				.append(version.substring(4, 6))
				.append(AboutActivity.VERSION_SEPARATOR)
				.append(version.substring(6, 8))
				.toString();
		}
		return "";
	}
}
