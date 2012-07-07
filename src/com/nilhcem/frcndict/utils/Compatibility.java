package com.nilhcem.frcndict.utils;

import java.lang.reflect.Field;

public final class Compatibility {
	private static int sCurSdkVersion = 0;
	private static final String TAG = "Compatibility";

	private Compatibility() {
		throw new UnsupportedOperationException();
	}

	public static int getApiLevel() {
		if (sCurSdkVersion > 0) {
			return sCurSdkVersion;
		}

		if (android.os.Build.VERSION.SDK.equalsIgnoreCase("3")) {
			sCurSdkVersion = 3;
		} else {
			Field sdkField;
			try {
				sdkField = android.os.Build.VERSION.class.getDeclaredField("SDK_INT");
				sCurSdkVersion = sdkField.getInt(null);
			} catch (Exception e) {
				Log.w(Compatibility.TAG, "Can't get API level: SDK_INT not supported");
				sCurSdkVersion = 0;
			}
		}
		return sCurSdkVersion;
	}

	/**
	 * Checks if current SDK is compatible with the desired API level.
	 * @param apiLevel the required API level.
	 * @return {@code true} if current OS is compatible.
	 */
	public static boolean isCompatible(int apiLevel) {
		return getApiLevel() >= apiLevel;
	}
}
