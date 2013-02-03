package com.nilhcem.cfdictparser;

import java.io.File;

import com.nilhcem.cfdictparser.core.Configuration;
import com.nilhcem.cfdictparser.core.Zipper;

/**
 * Creates the output folder and places every needed files inside.
 *
 * @author Nilhcem
 * @since 1.0
 */
public final class OutputMaker {
	private static final Configuration CONFIG = Configuration.getInstance();
	private static final String DB_NAME = CONFIG.get("db.name");
	private static final String OUTPUT_DIR = CONFIG.get("output.dir");
	private static final String OUTPUT_FILE = OUTPUT_DIR + File.separator + CONFIG.get("zip.file");

	/**
	 * Creates the output folder, and puts every needed files inside.
	 */
	public void create() {
		createOutputFolders();
		zipDatabase();
		rmTempDatabase();
	}

	/**
	 * Creates the output directories.
	 */
	private void createOutputFolders() {
		File output = new File(OUTPUT_DIR);
		output.mkdirs();
	}

	/**
	 * Compresses the database in a .zip file.
	 */
	private void zipDatabase() {
		Zipper zipper = new Zipper(OUTPUT_FILE, new String[] { DB_NAME });
		zipper.start();
	}

	/**
	 * Deletes the non-zipped database.
	 */
	private void rmTempDatabase() {
		File database = new File(DB_NAME);
		database.delete();
	}
}
