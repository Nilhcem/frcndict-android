package com.nilhcem.frcndict.updatedb.xml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import org.xmlpull.v1.XmlSerializer;

import android.database.Cursor;
import android.util.Log;
import android.util.Xml;

import com.nilhcem.frcndict.core.AbstractCancellableObservable;
import com.nilhcem.frcndict.database.DatabaseHelper;
import com.nilhcem.frcndict.database.Tables;

public final class BackupXmlWriter extends AbstractCancellableObservable {
	public static final String XML_SUB_TAG = "entry";
	public static final String XML_ENCODING = "UTF-8";
	private static final String XML_MAIN_TAG = "starred";
	private static final String TAG = "BackupXmlWriter";

	private final DatabaseHelper mDb;
	private FileOutputStream mOutputStream;

	public BackupXmlWriter(DatabaseHelper db, File xmlFile) {
		super();
		mDb = db;

		// Create FileOuputStream for xmlFile
		try {
			mOutputStream = new FileOutputStream(xmlFile);
		} catch (FileNotFoundException e) {
			Log.e(BackupXmlWriter.TAG, e.getMessage());
		}
	}

	@Override
	public void start() throws IOException {
		mDb.open();
		long totalEntries = mDb.getNbStarred();

		if (totalEntries > 0 && !mCancelled) {
			XmlSerializer serializer = Xml.newSerializer();

			Cursor c = mDb.getAllStarred();
			if (c.moveToFirst()) {
				serializer.setOutput(mOutputStream, BackupXmlWriter.XML_ENCODING);
				serializer.startDocument(BackupXmlWriter.XML_ENCODING, Boolean.TRUE);
				serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
				serializer.startTag(null, BackupXmlWriter.XML_MAIN_TAG);

				HashMap<String, Integer> columnsIndexCache = new HashMap<String, Integer>();
				long curEntry = 0;
				do {
					// Save in XML
					fillColumnsIndexCache(columnsIndexCache, c);
					String simplified = c.getString(columnsIndexCache.get(Tables.ENTRIES_KEY_SIMPLIFIED));
					String date = c.getString(columnsIndexCache.get(Tables.ENTRIES_KEY_STARRED_DATE));
					serializer.startTag(null, BackupXmlWriter.XML_SUB_TAG);
					serializer.attribute(null, Tables.ENTRIES_KEY_SIMPLIFIED, simplified);
					serializer.attribute(null, Tables.ENTRIES_KEY_STARRED_DATE, date);
					serializer.endTag(null, BackupXmlWriter.XML_SUB_TAG);

					// Notify percentage to observers
					if (countObservers() > 0) {
						updateProgress((int) ((++curEntry * 100) / totalEntries));
					}
				} while (c.moveToNext() && !mCancelled);

				c.close();
		        serializer.endTag(null, BackupXmlWriter.XML_MAIN_TAG);
		        serializer.endDocument();
		        serializer.flush();
			}
		} else {
			// Notify that it is finished
			updateProgress(100);
		}
		mDb.close();
        mOutputStream.close();
	}

	private void fillColumnsIndexCache(HashMap<String, Integer> cache, Cursor c) {
		if (cache.isEmpty()) {
			cache.put(Tables.ENTRIES_KEY_SIMPLIFIED, c.getColumnIndex(Tables.ENTRIES_KEY_SIMPLIFIED));
			cache.put(Tables.ENTRIES_KEY_STARRED_DATE, c.getColumnIndex(Tables.ENTRIES_KEY_STARRED_DATE));
		}
	}
}
