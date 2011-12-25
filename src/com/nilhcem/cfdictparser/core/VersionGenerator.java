package com.nilhcem.cfdictparser.core;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Generates a version number for the database.
 * @author Nilhcem
 * @since 1.0
 */
public final class VersionGenerator {
	private static String version = null;

	/**
	 * Returns a version number for the database.
	 * <p>
	 * If the version number was not computed yet, generates a new one.
	 * </p>
	 *
	 * @return the version number of the database.
	 */
	public static String getVersion() {
		if (version == null) {
			version = new SimpleDateFormat(Configuration.getInstance().get("version.format")).format(new Date());
		}
		return version;
	}
}
