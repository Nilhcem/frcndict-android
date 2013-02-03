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

import com.nilhcem.frcndict.core.AbstractCancellableObservable;
import com.nilhcem.frcndict.core.Log;
import com.nilhcem.frcndict.database.StarredDbHelper;

public final class RestoreXmlReader extends AbstractCancellableObservable {
	private final File mXmlFile;
	private final StarredDbHelper mDb;

	public RestoreXmlReader(StarredDbHelper dbHelper, File xmlFile) {
		super();
		mDb = dbHelper;
		mXmlFile = xmlFile;
	}

	@Override
	public void start() throws IOException {
		try {
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

					Node simplifiedNode = attributes.getNamedItem(StarredDbHelper.STARRED_KEY_SIMPLIFIED);
					Node starredNode = attributes.getNamedItem(StarredDbHelper.STARRED_KEY_DATE);

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
			} catch (ParserConfigurationException ex) {
				Log.e(RestoreXmlReader.class.getSimpleName(), ex, "Failed to get DocumentBuilder factory");
				// Do nothing
			} catch (SAXException ex) {
				Log.e(RestoreXmlReader.class.getSimpleName(), ex, "Error parsing xml file");
				// Do nothing
			}
			updateProgress(100); // Notify that it is finished (even if 0 elements to restore)
		} catch (SQLiteException ex) {
			Log.e(RestoreXmlReader.class.getSimpleName(), ex, "SQLite exception");
			// Do nothing
		}
		mDb.close();
	}
}
