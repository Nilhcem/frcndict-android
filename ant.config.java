// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
// WARNING: Do not modify this file directly (unless you know what you are doing).
// Please refer to 'ant.properties' instead.
// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
package com.nilhcem.frcndict.core;

public final class Config {
	private Config() {
		throw new UnsupportedOperationException();
	}

	/** Logging level: 0:none, 1:error, 2:warn, 3:info, 4:debug */
	public static final int LOGLEVEL = @LOGGING.LEVEL@;

	/** Database version (will be displayed in the About dialog) */
	public static final String DATABASE_VERSION = "@DATABASE.VERSION@";
}
