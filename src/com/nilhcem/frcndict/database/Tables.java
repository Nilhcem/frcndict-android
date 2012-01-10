package com.nilhcem.frcndict.database;

/**
 * Contains tables and columns names.
 *
 * @author Nilhcem
 * @since 1.0
 */
public interface Tables {
	// Entries table
	public static final String ENTRIES_TABLE_NAME = "entries";
	public static final String ENTRIES_KEY_ROWID = "_id";
	public static final String ENTRIES_KEY_SIMPLIFIED = "simplified";
	public static final String ENTRIES_KEY_TRADITIONAL = "traditional";
	public static final String ENTRIES_KEY_PINYIN = "pinyin";
	public static final String ENTRIES_KEY_PINYIN2 = "pinyin2";
	public static final String ENTRIES_KEY_TRANSLATION = "translation";

	// Metadata table
	public static final String METADATA_TABLE_NAME = "dict_metadata";
	public static final String METADATA_KEY_VERSION = "version";
}
