package com.nilhcem.cfdictparser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.nilhcem.cfdictparser.sqlite.DbHandler;

/**
 * Provides methods to parse the UTF-8 text file dictionary, to fill the database.
 *
 * @author Nilhcem
 * @since 1.0
 */
public final class FileParser {
	private File inputFile;
	private static final Pattern PATTERN = Pattern.compile("^\\s*(.+)\\s+\\[(.*)\\]\\s+/(.+)/\\s*$");

	/**
	 * @param pathname a String containing the UTF-8 text file dictionary path.
	 * @throws FileNotFoundException if the dictionary was not found.
	 */
	public FileParser(String pathname) throws FileNotFoundException {
		inputFile = new File(pathname);
		if (!inputFile.exists()) {
			throw new FileNotFoundException(pathname);
		}
	}

	/**
	 * Parses the text file and fills the database.
	 */
	public void parseFile() {
		try {
			String line;
			DbHandler db = new DbHandler();
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile.getAbsolutePath()), "UTF8"));

			while ((line = in.readLine()) != null) {
				parseLine(line.trim(), db);
			}
			db.flush();
			in.close();
			db.close();
		} catch (IOException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Parses the String in parameter.
	 *
	 * @param line the String which should be parsed.
	 * @param db the {@code DbHandler} object. Will be used to insert data in database.
	 */
	private void parseLine(String line, DbHandler db) {
		// Check if line is a comment
		if (line.startsWith("#")) {
			return;
		}

		// Parse line
		Matcher matcher = PATTERN.matcher(line);
		if (matcher.find()) {
			String[] chinese = matcher.group(1).split(" ");
			String simplified;
			String traditional;

			if (chinese.length == 2 && chinese[0].length() == chinese[1].length()) {
				traditional = chinese[0];
				simplified = chinese[1];
			} else {
				simplified = matcher.group(1);
				traditional = "";
			}
			String pinyin = matcher.group(2);
			String translation = matcher.group(3);

			db.add(simplified, traditional, pinyin, translation);
		}
	}
}
