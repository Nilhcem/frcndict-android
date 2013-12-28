package com.nilhcem.frcndict.core;

import java.util.IllegalFormatException;
import java.util.Locale;

public final class Log {

	private static final boolean LOG_ERROR = Config.LOGLEVEL > 0;
	private static final boolean LOG_WARN = Config.LOGLEVEL > 1;
	private static final boolean LOG_INFO = Config.LOGLEVEL > 2;
	private static final boolean LOG_DEBUG = Config.LOGLEVEL > 3;

	private Log() {
		throw new UnsupportedOperationException();
	}

	public static void d(String tag, String msgFormat, Object... args) {
		if (LOG_DEBUG) {
			try {
				android.util.Log.d(tag, String.format(Locale.US, msgFormat, args));
			} catch (NullPointerException e) {
				// Do nothing
			} catch (IllegalFormatException e) {
				android.util.Log.d(tag, msgFormat);
			}
		}
	}

	public static void w(String tag, String msgFormat, Object... args) {
		if (LOG_WARN) {
			try {
				android.util.Log.w(tag, String.format(Locale.US, msgFormat, args));
			} catch (NullPointerException e) {
				// Do nothing
			} catch (IllegalFormatException e) {
				android.util.Log.w(tag, msgFormat);
			}
		}
	}

	public static void i(String tag, String msgFormat, Object... args) {
		if (LOG_INFO) {
			try {
				android.util.Log.i(tag, String.format(Locale.US, msgFormat, args));
			} catch (NullPointerException e) {
				// Do nothing
			} catch (IllegalFormatException e) {
				android.util.Log.i(tag, msgFormat);
			}
		}
	}

	public static void e(String tag, Throwable t) {
		if (LOG_ERROR) {
			android.util.Log.e(tag, "", t);
		}
	}

	public static void e(String tag, String msgFormat, Object... args) {
		if (LOG_ERROR) {
			try {
				android.util.Log.e(tag, String.format(Locale.US, msgFormat, args));
			} catch (NullPointerException e) {
				// Do nothing
			} catch (IllegalFormatException e) {
				android.util.Log.e(tag, msgFormat);
			}
		}
	}

	public static void e(String tag, Throwable t, String msgFormat, Object... args) {
		if (LOG_ERROR) {
			try {
				android.util.Log.e(tag, String.format(Locale.US, msgFormat, args), t);
			} catch (NullPointerException e) {
				// Do nothing
			} catch (IllegalFormatException e) {
				android.util.Log.e(tag, msgFormat, t);
			}
		}
	}
}
