// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
// WARNING: Do not modify this file directly (unless you know what you are doing).
// Please change 'ant.properties' values instead.
// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
package com.nilhcem.frcndict.core;

public final class Config {
	/** Whether or not to include logging statements in the application. */
	public final static boolean LOGGING = @CONFIG.LOGGING@;

	/** Dictionary URL */
	public static final String DICT_URL = "@DICT.URL@";
}
