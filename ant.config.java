// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
// WARNING: Do not modify this file directly (unless you know what you are doing).
// Please refer to 'ant.properties' instead.
// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
package com.nilhcem.frcndict.core;

import android.text.TextUtils;

import com.nilhcem.frcndict.core.Config;
import com.nilhcem.frcndict.utils.Compatibility;

public final class Config {
	/** Dictionary URL */
	public static final String DICT_URL = "@DICT.URL@";

	/** Google Cloud Messaging */
	public static final String GCM_SENDER_ID = "@GCM.SENDER.ID@";
	public static final String GCM_SERVER_URL = "@GCM.SERVER.URL@";

	/** Logging level: 0:none, 1:error, 2:warn, 3:info, 4:debug */
	public static final int LOGLEVEL = @LOGGING.LEVEL@;
	public static final boolean LOG_ERROR = LOGLEVEL > 0; // TODO: REMOVE
	public static final boolean LOG_WARN = LOGLEVEL > 1; // TODO: REMOVE
	public static final boolean LOG_INFO = LOGLEVEL > 2; // TODO: REMOVE
	public static final boolean LOG_DEBUG = LOGLEVEL > 3; // TODO: REMOVE

	public static boolean isGcmEnabled() {
		return (Compatibility.isCompatible(8)
			&& !TextUtils.isEmpty(Config.GCM_SENDER_ID)
			&& !TextUtils.isEmpty(Config.GCM_SERVER_URL));
	}
}
