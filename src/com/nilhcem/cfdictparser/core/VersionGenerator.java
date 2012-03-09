package com.nilhcem.cfdictparser.core;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.nilhcem.cfdictparser.sqlite.Tables;

/**
 * Generates a version number for the database.
 * @author Nilhcem
 * @since 1.0
 */
public final class VersionGenerator {
	private static String version = null;
	private static final String VERSION_FORMAT = "%s-%d";

	/**
	 * Returns a version number for the database.
	 * <p>
	 * The format is the following:<br />
	 * [database version number]-[android app minimum version code].<br />
	 * It is important to specify the minimum version code of the android application
	 * to maintain the compatibility: ie if the parser has changed (table is renamed) and the
	 * android application is not updated on user side, the android app will download the new
	 * database but will not be able to deal with it since it still has a code compatible with the previous parser.<br /><br />
	 * If the version number was not computed yet, generates a new one.
	 * </p>
	 *
	 * @return the version number of the database.
	 */
	public static String getVersion() {
		if (version == null) {
			Configuration conf = Configuration.getInstance();
			version = String.format(VersionGenerator.VERSION_FORMAT,
					new SimpleDateFormat(conf.get("version.format")).format(new Date()),
					Tables.DATABASE_VERSION);
		}
		return version;
	}
}
