package com.nilhcem.cfdictparser.sqlite;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import com.nilhcem.cfdictparser.core.Configuration;
import com.nilhcem.cfdictparser.core.VersionGenerator;

/**
 * Provides methods to communicate with an SQLite database.
 *
 * @author Nilhcem
 * @since 1.0
 */
public final class DbHandler {
	private static final Configuration CONFIG = Configuration.getInstance();
	private static final String DB_NAME = CONFIG.get("db.name");
	private Connection conn;
	private PreparedStatement prep;

	/**
	 * Creates the SQLite database.
	 */
	public DbHandler() {
		try {
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection("jdbc:sqlite:" + DbHandler.DB_NAME);
			createTables();
			prep = conn.prepareStatement("INSERT INTO " + Tables.ENTRIES_TABLE_NAME + "("
					+ Tables.ENTRIES_KEY_SIMPLIFIED + ", "
					+ Tables.ENTRIES_KEY_TRADITIONAL + ", "
					+ Tables.ENTRIES_KEY_PINYIN + ", "
					+ Tables.ENTRIES_KEY_PINYIN2 + ", "
					+ Tables.ENTRIES_KEY_TRANSLATION + ", "
					+ Tables.ENTRIES_KEY_TRANS_AVG_LENGTH + ") VALUES (?, ?, ?, ?, ?, ?);");
		} catch (ClassNotFoundException | SQLException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Closes the database if it is not closed yet (should already be closed).
	 */
	@Override
	protected void finalize() throws Throwable {
		close();
		super.finalize();
	}

	/**
	 * Creates some tables.
	 *
	 * @throws SQLException if an SQL error happened.
	 */
	private void createTables() throws SQLException {
		Statement stat = conn.createStatement();

		// android_metadata
		stat.executeUpdate("DROP TABLE IF EXISTS android_metadata;");
		stat.executeUpdate("CREATE TABLE android_metadata (locale TEXT);");
		stat.executeUpdate("INSERT INTO android_metadata VALUES ('en_US');");

		// ENTRIES_TABLE_NAME
		stat.executeUpdate("DROP TABLE IF EXISTS " + Tables.ENTRIES_TABLE_NAME + ";");
		stat.executeUpdate("CREATE TABLE " + Tables.ENTRIES_TABLE_NAME + " ("
				+ Tables.ENTRIES_KEY_ROWID + " integer primary key autoincrement, "
				+ Tables.ENTRIES_KEY_SIMPLIFIED + " text not null, "
				+ Tables.ENTRIES_KEY_TRADITIONAL + " text, "
				+ Tables.ENTRIES_KEY_PINYIN + " text, "
				+ Tables.ENTRIES_KEY_PINYIN2 + " text, "
				+ Tables.ENTRIES_KEY_TRANSLATION + " text not null,"
				+ Tables.ENTRIES_KEY_TRANS_AVG_LENGTH + " integer,"
				+ Tables.ENTRIES_KEY_STARRED_DATE + " text);");

		// METADATA_TABLE_NAME
		stat.executeUpdate("DROP TABLE IF EXISTS " + Tables.METADATA_TABLE_NAME + ";");
		stat.executeUpdate("CREATE TABLE " + Tables.METADATA_TABLE_NAME + " ("
				+ Tables.METADATA_KEY_VERSION + " long not null);");
		stat.executeUpdate("INSERT INTO " + Tables.METADATA_TABLE_NAME + " VALUES ('" + VersionGenerator.getVersion() + "');");
	}

	/**
	 * Closes the database.
	 */
	public void close() {
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				System.err.println(e.getMessage());
				e.printStackTrace();
			} finally {
				conn = null;
			}
		}
	}

	/**
	 * Add an entry in the database.
	 *
	 * @param simplified Simplified Chinese characters.
	 * @param traditional Traditional Chinese characters.
	 * @param pinyin Pinyin.
	 * @param translation Translation of the Chinese characters.
	 */
	public void add(String simplified, String traditional, String pinyin, String translation) {
		try {
			prep.setString(1, simplified);
			prep.setString(2, traditional);
			prep.setString(3, pinyin);
			prep.setString(4, pinyin.replaceAll("[^a-zA-Z]", "").toLowerCase()); // pinyin lower case without tones nor space
			prep.setString(5, translation);
			prep.setInt(6, getAverageLengthForEachWord(translation));
			prep.addBatch();
		} catch (SQLException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Commit all the pending inserted entries.
	 */
	public void flush() {
	    try {
			conn.setAutoCommit(false);
		    prep.executeBatch();
		    conn.setAutoCommit(true);
	    } catch (SQLException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Get a representation of the average length (*100) of each words of the translation.
	 * <p>
	 * This is a trick used to get more accurate results, we order each results by their length.<br />
	 * However, the translation contains many words on one column (yeah... this is not good...) and this can't work.<br />
	 * We can fix this by creating another table which contains each word separately but it costs much more
	 * physical and logical memory.
	 * </p>
	 *
	 * @param translation the translation containing many words separated by the / character.
	 * @return the average number of characters for each word in the translation.
	 */
	public int getAverageLengthForEachWord(String translation) {
		String[] translations = translation.split("/");

		int total = 0;
		int nbWords = translations.length;
		for (String curTr : translations) {
			total += curTr.length();
		}

		if (nbWords > 0) {
			total = (int)((double)total * 100 / nbWords);
		}
		return total;
	}
}
