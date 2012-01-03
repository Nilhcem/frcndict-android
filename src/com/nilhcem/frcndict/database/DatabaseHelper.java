package com.nilhcem.frcndict.database;

import java.io.File;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.nilhcem.frcndict.ApplicationController;

public final class DatabaseHelper {
	public static final String DATABASE_NAME = "dictionary.db";
	private static final DatabaseHelper instance = new DatabaseHelper();

	private File dbPath;
	private SQLiteDatabase mDb;

	private DatabaseHelper() {
    }

	public static DatabaseHelper getInstance() {
		return instance;
    }

	public void setDatabasePath(File dbPath) {
		this.dbPath = dbPath;
	}

	public void open() {
		if (mDb == null || !mDb.isOpen()) {
			mDb = SQLiteDatabase.openDatabase(dbPath.getAbsolutePath(), null, SQLiteDatabase.OPEN_READWRITE);
		}
	}

    public void close() {
		mDb.close();
    }

	// returns true if database exists
	public boolean isInitialized() {
		boolean initialized = false;

		if (dbPath.exists()) {
			mDb = SQLiteDatabase.openDatabase(dbPath.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY);
			try {
				mDb.query(Tables.ENTRIES_TABLE_NAME, new String[] {
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

	// TODO: Just to try
	public Cursor searchDesc(String search, Integer curPage) {
		return mDb.query(Tables.ENTRIES_TABLE_NAME, null,
				String.format("%s LIKE '%s%%'", Tables.ENTRIES_KEY_TRANSLATION, search),
				null, null, null, String.format("%s ASC", Tables.ENTRIES_KEY_ROWID),
				String.format("%d,%d", curPage * ApplicationController.NB_ENTRIES_PER_LIST,
						ApplicationController.NB_ENTRIES_PER_LIST + 1)); // last one is not display but just to know if there are still some elements after
    }
}
