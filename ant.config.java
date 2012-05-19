// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
// WARNING: Do not modify this file directly (unless you know what you are doing).
// Please refer to 'ant.properties' instead.
// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
package com.nilhcem.frcndict.core;

public final class Config {
	/** Dictionary URL */
	public static final String DICT_URL = "@DICT.URL@";

	/** Logging level */
	private static final int LOGLEVEL = @LOGGING.LEVEL@;
	public static final boolean LOG_ERROR = LOGLEVEL > 0;
	public static final boolean LOG_WARN = LOGLEVEL > 1;
	public static final boolean LOG_INFO = LOGLEVEL > 2;
	public static final boolean LOG_DEBUG = LOGLEVEL > 3;

	/** Checks for updates every @UPDATES.INTERVAL@ days */
	public static final int CHECK_FOR_UPDATES_INTERVAL = @UPDATES.INTERVAL@;
}
