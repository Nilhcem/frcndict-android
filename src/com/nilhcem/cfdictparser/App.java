package com.nilhcem.cfdictparser;

import java.io.FileNotFoundException;

import com.nilhcem.cfdictparser.args.ArgsParser;
import com.nilhcem.cfdictparser.args.DisplayUsageException;

/**
 * Entry point of the application.
 * <p>
 * Checks arguments, parses file and creates output data.
 * </p>
 *
 * @author Nilhcem
 * @since 1.0
 */
public final class App {
	/**
	 * Launches application.
	 *
	 * @param args an Array of String which should contain only one parameter: the UTF-8 text file dictionary.
	 */
	public static void main(final String[] args) {
		try {
			System.out.println("Processing, please wait...");

			// Check parameters
			ArgsParser params = new ArgsParser();
			params.checkArgs(args);

			// Parse file
			FileParser parser = new FileParser(params.getInputFile());
			parser.parseFile();

			// Create output data
			OutputMaker output = new OutputMaker();
			output.create();

			System.out.println("Done.");
		} catch (DisplayUsageException e) {
			System.err.println(e.getMessage());
		} catch (FileNotFoundException e) {
			System.err.println(String.format("File not found: %s. Exit.", e.getMessage()));
		}
	}
}
