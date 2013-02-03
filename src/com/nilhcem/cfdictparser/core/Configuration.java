package com.nilhcem.cfdictparser.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Contains and returns some project-specific configuration variables.
 *
 * @author Nilhcem
 * @since 1.0
 */
public final class Configuration {
	private static final Configuration INSTANCE = new Configuration();
	private static final String CONFIG_FILE = "/configuration.properties";
	private final Properties config = new Properties();

	/**
	 * Returns the unique instance of the class.
	 *
	 * @return the unique instance.
	 */
	public static Configuration getInstance() {
		return INSTANCE;
	}

	/**
	 * Opens the "{@code configuration.properties}" file and maps data.
	 */
	private Configuration() {
		InputStream in = getClass().getResourceAsStream(CONFIG_FILE);
		try {
			config.load(in);
			in.close();
		} catch (IOException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Returns the value of a specific entry from the "{@code configuration.properties}" file.
	 *
	 * @param key a string representing the key from a key/value couple.
	 * @return the value of the key, or an empty string if the key was not found.
	 */
	public String get(String key) {
		if (config != null && config.containsKey(key)) {
			return config.getProperty(key);
		}
		return "";
	}
}
