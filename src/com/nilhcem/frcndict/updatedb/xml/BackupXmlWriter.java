package com.nilhcem.frcndict.updatedb.xml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

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

	private final DatabaseHelper db;
	private FileOutputStream outputStream;

	public BackupXmlWriter(DatabaseHelper db, File xmlFile) {
		super();
		this.db = db;

		// Create FileOuputStream for xmlFile
		try {
			outputStream = new FileOutputStream(xmlFile);
		} catch (FileNotFoundException e) {
			Log.e(BackupXmlWriter.TAG, e.getMessage());
		}
	}

	@Override
	public void start() throws IOException {
		db.open();
		long totalEntries = db.getNbStarred();

		if (totalEntries > 0 && !cancelled) {
			XmlSerializer serializer = Xml.newSerializer();

			try {
				Cursor c = db.getAllStarred();
				if (c.moveToFirst()) {
					serializer.setOutput(outputStream, BackupXmlWriter.XML_ENCODING);
					serializer.startDocument(BackupXmlWriter.XML_ENCODING, Boolean.TRUE);
					serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
					serializer.startTag(null, BackupXmlWriter.XML_MAIN_TAG);

					long curEntry = 0;
					do {
						// Save in XML
						String simplified = c.getString(c.getColumnIndex(Tables.ENTRIES_KEY_SIMPLIFIED));
						String date = c.getString(c.getColumnIndex(Tables.ENTRIES_KEY_STARRED_DATE));
						serializer.startTag(null, BackupXmlWriter.XML_SUB_TAG);
						serializer.attribute(null, Tables.ENTRIES_KEY_SIMPLIFIED, simplified);
						serializer.attribute(null, Tables.ENTRIES_KEY_STARRED_DATE, date);
						serializer.endTag(null, BackupXmlWriter.XML_SUB_TAG);

						// Notify percentage to observers
						if (this.countObservers() > 0) {
							updateProgress((int) ((++curEntry * 100) / totalEntries));
						}
					} while (c.moveToNext() && !cancelled);

					c.close();
			        serializer.endTag(null, BackupXmlWriter.XML_MAIN_TAG);
			        serializer.endDocument();
			        serializer.flush();
				}
			} catch (Exception e) {
				Log.e(BackupXmlWriter.TAG, e.getMessage());
				updateProgress(100);
			}
		} else {
			// Notify that it is finished
			updateProgress(100);
		}
		db.close();
        outputStream.close();
	}
}
