package com.nilhcem.cfdictparser.sqlite;

/**
 * Contains tables and columns names.
 *
 * @author Nilhcem
 * @since 1.0
 */
public interface Tables {
	// Entries table
	String ENTRIES_TABLE_NAME = "entries";
	String ENTRIES_KEY_ROWID = "_id";
	String ENTRIES_KEY_SIMPLIFIED = "simplified";
	String ENTRIES_KEY_TRADITIONAL = "traditional";
	String ENTRIES_KEY_PINYIN = "pinyin";
	String ENTRIES_KEY_PINYIN2 = "pinyin2";
	String ENTRIES_KEY_TRANSLATION = "translation";
	String ENTRIES_KEY_TRANS_NO_ACCENT = "trans_no_accent";
	String ENTRIES_KEY_TRANS_AVG_LENGTH = "trans_avg_length"; // see DbHandler.getAverageLengthForEachWord
}
