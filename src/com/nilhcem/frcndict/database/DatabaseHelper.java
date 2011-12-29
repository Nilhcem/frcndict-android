package com.nilhcem.frcndict.database;

import java.io.File;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

public final class DatabaseHelper {
	public static final String DATABASE_NAME = "dictionary.db";
	private static final DatabaseHelper instance = new DatabaseHelper();

	private File dbPath;
	private SQLiteDatabase database;

	private DatabaseHelper() {
    }

	public static DatabaseHelper getInstance() {
		return instance;
    }

	public void setDatabasePath(File dbPath) {
		this.dbPath = dbPath;
	}

//	public void open() {
//		database = SQLiteDatabase.openDatabase(dbPath.getAbsolutePath(), null, SQLiteDatabase.OPEN_READWRITE);
//	}

    public void close() {
		database.close();
    }

	// returns true if database exists
	public boolean isInitialized() {
		boolean initialized = false;

		if (dbPath.exists()) {
			database = SQLiteDatabase.openDatabase(dbPath.getAbsolutePath(), null, SQLiteDatabase.OPEN_READWRITE);
			try {
				database.query(Tables.ENTRIES_TABLE_NAME, new String[] {
						Tables.ENTRIES_KEY_ROWID }, null, null, null, null, null);
				initialized = true;
			} catch (SQLiteException exc) {
				//initialized = false;
			} finally {
				close();
			}
		}
		return initialized;
	}
}
