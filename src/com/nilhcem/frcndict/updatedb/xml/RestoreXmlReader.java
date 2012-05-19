package com.nilhcem.frcndict.updatedb.xml;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.nilhcem.frcndict.core.AbstractCancellableObservable;
import com.nilhcem.frcndict.core.Config;
import com.nilhcem.frcndict.database.DatabaseHelper;
import com.nilhcem.frcndict.database.Tables;

public final class RestoreXmlReader extends AbstractCancellableObservable {
	private final File mXmlFile;
	private final DatabaseHelper mDb;

	public RestoreXmlReader(DatabaseHelper db, File xmlFile) {
		super();
		mDb = db;
		mXmlFile = xmlFile;
	}

	@Override
	public void start() throws IOException {
		mDb.open();
		try {
			mDb.beginTransaction();
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

			try {
				DocumentBuilder builder = factory.newDocumentBuilder();
				Document dom = builder.parse(mXmlFile);
				Element root = dom.getDocumentElement();
				NodeList items = root.getElementsByTagName(BackupXmlWriter.XML_SUB_TAG);

				int totalEntries = items.getLength();
				for (int i = 0; i < totalEntries; i++) {
					Node item = items.item(i);
					NamedNodeMap attributes = item.getAttributes();

					Node simplifiedNode = attributes.getNamedItem(Tables.ENTRIES_KEY_SIMPLIFIED);
					Node starredNode = attributes.getNamedItem(Tables.ENTRIES_KEY_STARRED_DATE);

					if (simplifiedNode != null && starredNode != null) {
						String simplified = simplifiedNode.getNodeValue();
						String starredDate = starredNode.getNodeValue();

						if (simplified != null && starredDate != null) {
							mDb.setStarredDate(simplified, starredDate);
						}
					}

					// Notify percentage to observers
					if (countObservers() > 0) {
						updateProgress(((i + 1) * 100) / totalEntries);
					}
				}
				mDb.setTransactionSuccessfull();
			} catch (ParserConfigurationException ex) {
				if (Config.LOG_ERROR) Log.e(RestoreXmlReader.class.getSimpleName(), "Failed to get DocumentBuilder factory", ex);
				// Do nothing
			} catch (SAXException ex) {
				if (Config.LOG_ERROR) Log.e(RestoreXmlReader.class.getSimpleName(), "Error parsing xml file", ex);
				// Do nothing
			} finally {
				mDb.endTransaction();
			}
			updateProgress(100); // Notify that it is finished (even if 0 elements to restore)
		} catch (SQLiteException ex) {
			if (Config.LOG_ERROR) Log.e(RestoreXmlReader.class.getSimpleName(), "SQLite exception", ex);
			// Do nothing
		}
		mDb.close();
	}
}
