package com.nilhcem.cfdictparser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import com.nilhcem.cfdictparser.core.Configuration;
import com.nilhcem.cfdictparser.core.Md5;
import com.nilhcem.cfdictparser.core.VersionGenerator;
import com.nilhcem.cfdictparser.core.Zipper;

/**
 * Creates the output folder and places every needed files inside.
 *
 * @author Nilhcem
 * @since 1.0
 */
public final class OutputMaker {
	private static final Configuration CONFIG = Configuration.getInstance();
	private static final String OUTPUT_FILE = CONFIG.get("output.dir") + File.separator + CONFIG.get("output.zip");

	/**
	 * Creates the output folder, and puts every needed files inside.
	 */
	public void create() {
		createOutputFolder();
		zipDatabase();
		createMd5File();
		createVersion();
		rmTempDatabase();
	}

	/**
	 * Creates the output directory.
	 */
	private void createOutputFolder() {
		File output = new File(CONFIG.get("output.dir"));
		output.mkdir();
	}

	/**
	 * Compresses the database in a .zip file.
	 */
	private void zipDatabase() {
		Zipper zipper = new Zipper(OUTPUT_FILE, new String[] {CONFIG.get("db.name")});
		zipper.start();
	}

	/**
	 * Deletes the non-zipped database.
	 */
	private void rmTempDatabase() {
		File database = new File(CONFIG.get("db.name"));
		database.delete();
	}

	/**
	 * Generates an MD5 checksum of the zipped database, and writes it into a file.
	 */
	private void createMd5File() {
		try {
			writeToFile(Md5.getMd5Sum(OUTPUT_FILE), CONFIG.get("output.dir") + File.separator + CONFIG.get("output.md5"));
		} catch (FileNotFoundException e) {
			e.getMessage();
			e.printStackTrace();
		}

	}

	/**
	 * Creates a file containing the database version number.
	 */
	private void createVersion() {
		writeToFile(VersionGenerator.getVersion(), CONFIG.get("output.dir")
				+ File.separator + CONFIG.get("output.version"));
	}

	/**
	 * Writes the content of a String passed in parameter into a file.
	 *
	 * @param content the content of the new file which will be created.
	 * @param filePath the file path of the new file which will be created.
	 */
	private void writeToFile(String content, String filePath) {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(filePath));
			out.write(content);
			out.close();
		} catch (IOException e) {
			e.getMessage();
			e.printStackTrace();
		}
	}
}
