package com.nilhcem.cfdictparser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
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
	private static final String MEANINGS_SEPARATOR = "/";
	private static final Pattern PATTERN = Pattern.compile("^\\s*(.+)\\s+\\[(.*)\\]\\s+/(.+)/\\s*$");

	// For pinyin formatting
	private Pattern formatPinyinPattern = Pattern.compile("([a-z:]+\\d)([a-z]+)");
	private Pattern formatPinyinPattern2 = Pattern.compile("([a-zA-Z0-9.,_-]+).*");

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
			db.createIndexes();
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
			String translation = formatTranslation(matcher.group(3));

			pinyin = formatPinyin(pinyin, simplified);
			if (pinyin != null) {
				db.add(simplified.trim(), traditional.trim(), pinyin.trim(), translation.trim());
			}
		}
	}

	/**
	 * Format the pinyin entry properly, since all dictionary data are not parsed the same way.
	 *
	 * Pretty ugly but seems to work.
	 * @param pinyin the pinyin to check.
	 * @param simplified the simplified chinese character, as a reference to check the pinyin.
	 * @return the newly formatted pinyin, or null if the entry is broken and should not be added.
	 */
	private String formatPinyin(String pinyin, String simplified) {
		if (!pinyin.isEmpty()) {
			int nbHanzi = simplified.length();

			// Problem if the number of character in the simplified chinese is not equal to the number of separated pinyin
			if (nbHanzi != pinyin.split(" ").length) {
				// it may be a pinyin problem (ie "jia1na1 da1" instead of "jia1 na1 da1"). Split pinyin properly
				Matcher matcher = formatPinyinPattern.matcher(pinyin);
				pinyin = matcher.replaceAll("$1 $2");

				if (nbHanzi == pinyin.split(" ").length) { // problem should be solved
					return pinyin;
				} else { // Still not good, it may be because a hanzi contains A-Za-z characters, also in pinyin
					Matcher matcher2 = formatPinyinPattern2.matcher(simplified);
					if (matcher2.matches()) {
						String matchStr = matcher2.group(1);
						if (pinyin.contains(matchStr)) {
							pinyin = pinyin.replaceFirst(matchStr, matchStr + " ");
							nbHanzi -= (matchStr.length() - 1);
						}
					}
					if (nbHanzi == pinyin.split(" ").length) { // problem should be solved
						return pinyin;
					} else {
						return null; //Problem in the pinyin, do not insert entry in the dictionary
					}
				}
			}
		}
		return pinyin;
	}

	/**
	 * Remove duplicates and trim every translation.
	 *
	 * @param meaning original translations
	 * @return a duplicated-free and trimmed translation list
	 */
	private String formatTranslation(String translation) {
		String result = null;

		if (translation != null) {
			boolean addSeparator = false;
			StringBuilder sb = new StringBuilder();
			List<String> alreadyInserted = new ArrayList<>();

			String[] meanings = translation.split(FileParser.MEANINGS_SEPARATOR);
			for (String meaning : meanings) {
				meaning = meaning.trim();
				if (!meaning.isEmpty()
					&& !alreadyInserted.contains(meaning)) {
					if (addSeparator) {
						sb.append(FileParser.MEANINGS_SEPARATOR);
					} else {
						addSeparator = true;
					}
					sb.append(meaning);
					alreadyInserted.add(meaning);
				}
			}
			result = sb.toString();
		}
		return result;
	}
}
