package com.nilhcem.frcndict.updatedb.xml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;

import com.nilhcem.frcndict.core.AbstractProgressObservable;
import org.xmlpull.v1.XmlSerializer;

import android.content.Context;
import android.database.Cursor;
import android.util.Xml;

import com.nilhcem.frcndict.R;
import com.nilhcem.frcndict.core.Log;
import com.nilhcem.frcndict.database.StarredDbHelper;

public final class BackupXmlWriter extends AbstractProgressObservable {

	public static final String XML_SUB_TAG = "entry";
	private static final String XML_ENCODING = "UTF-8";
	private static final String XML_MAIN_TAG = "starred";
	private static final String XML_HEADER = "Backup file containing starred words for %s (https://play.google.com/store/apps/details?id=%s)";

	private final StarredDbHelper mDb;
	private String mHeaders = null;
	private FileOutputStream mOutputStream;

	public BackupXmlWriter(StarredDbHelper dbHelper, File xmlFile) {
		super();
		mDb = dbHelper;

		// Create FileOuputStream for xmlFile
		try {
			mOutputStream = new FileOutputStream(xmlFile);
		} catch (FileNotFoundException ex) {
			Log.e(BackupXmlWriter.class.getSimpleName(), ex, "Can't get output stream");
		}
	}

	@Override
	public void start() throws IOException {
		long totalEntries = mDb.getNbStarred();

		if (totalEntries > 0) {
			XmlSerializer serializer = Xml.newSerializer();

			Cursor c = mDb.getAllStarredCursor();
			if (c.moveToFirst()) {
				serializer.setOutput(mOutputStream, BackupXmlWriter.XML_ENCODING);
				serializer.startDocument(BackupXmlWriter.XML_ENCODING, Boolean.TRUE);
				serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);

				// Insert headers if any
				if (mHeaders != null) {
					serializer.comment(mHeaders);
				}

				serializer.startTag(null, BackupXmlWriter.XML_MAIN_TAG);

				HashMap<String, Integer> columnsIndexCache = new HashMap<String, Integer>();
				long curEntry = 0;
				do {
					// Save in XML
					fillColumnsIndexCache(columnsIndexCache, c);
					String simplified = c.getString(columnsIndexCache.get(StarredDbHelper.STARRED_KEY_SIMPLIFIED));
					String date = c.getString(columnsIndexCache.get(StarredDbHelper.STARRED_KEY_DATE));
					serializer.startTag(null, BackupXmlWriter.XML_SUB_TAG);
					serializer.attribute(null, StarredDbHelper.STARRED_KEY_SIMPLIFIED, simplified);
					serializer.attribute(null, StarredDbHelper.STARRED_KEY_DATE, date);
					serializer.endTag(null, BackupXmlWriter.XML_SUB_TAG);

					// Notify percentage to observers
					if (countObservers() > 0) {
						updateProgress((int) ((++curEntry * 100) / totalEntries));
					}
				} while (c.moveToNext());

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

	public void insertHeader(Context app) {
		mHeaders = String.format(Locale.US, XML_HEADER,
				app.getString(R.string.app_name),
				app.getClass().getPackage().getName());
	}

	private void fillColumnsIndexCache(HashMap<String, Integer> cache, Cursor c) {
		if (cache.isEmpty()) {
			cache.put(StarredDbHelper.STARRED_KEY_SIMPLIFIED, c.getColumnIndex(StarredDbHelper.STARRED_KEY_SIMPLIFIED));
			cache.put(StarredDbHelper.STARRED_KEY_DATE, c.getColumnIndex(StarredDbHelper.STARRED_KEY_DATE));
		}
	}
}
