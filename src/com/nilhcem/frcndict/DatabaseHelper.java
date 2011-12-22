package com.nilhcem.frcndict;

import java.io.File;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Environment;

public final class DatabaseHelper {
	private static final DatabaseHelper instance = new DatabaseHelper();
	public static /* final */ String DATABASE_PATH;
	public static final String ENTRIES_TABLE_NAME = "entries";
	public static final String ENTRIES_KEY_ROWID = "_id";
	public static final String ENTRIES_KEY_SIMPLIFIED = "simplified";
	public static final String ENTRIES_KEY_TRADITIONAL = "traditional";
	public static final String ENTRIES_KEY_PINYIN = "pinyin";
	public static final String ENTRIES_KEY_TRANSLATION = "translation";
	private static final String ENTRIES_TABLE_CREATE = "CREATE TABLE " + ENTRIES_TABLE_NAME + " ("
			+ ENTRIES_KEY_ROWID + " integer primary key autoincrement, "
			+ ENTRIES_KEY_SIMPLIFIED + " text not null, "
			+ ENTRIES_KEY_TRADITIONAL + " text, "
			+ ENTRIES_KEY_PINYIN + " text, "
			+ ENTRIES_KEY_TRANSLATION + " text not null);";

	private SQLiteDatabase database;

	private DatabaseHelper() {
		File dbFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/" + getClass().getPackage().getName());
		dbFolder.mkdirs();
		DATABASE_PATH = dbFolder.getAbsolutePath() + "/dictionary.db";
		database = SQLiteDatabase.openDatabase(DATABASE_PATH, null, SQLiteDatabase.CREATE_IF_NECESSARY);
    }

	public static DatabaseHelper getInstance() {
		return instance;
    }

	public void createTable() {
		// TODO : transaction
		database.execSQL(ENTRIES_TABLE_CREATE);
    }

    public void close() {
		database.close();
    }

	// return true if database exists
	public boolean isInitialized() {
		try {
			database.query(ENTRIES_TABLE_NAME, new String[] { ENTRIES_KEY_ROWID }, null, null, null, null, null);
		} catch (SQLiteException exc) {
			return false;
		}
		return true;
	}
}
