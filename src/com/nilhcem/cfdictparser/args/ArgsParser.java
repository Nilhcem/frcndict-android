package com.nilhcem.cfdictparser.args;

/**
 * Arguments parser.
 *
 * @author Nilhcem
 * @since 1.0
 */
public final class ArgsParser {
	private String inputFile = null;

	/**
	 * Checks arguments.
	 *
	 * @param args program arguments.
	 * @throws DisplayUsageException if the arguments doesn't match the specifications.
	 */
	public void checkArgs(String[] args) throws DisplayUsageException {
		if (args.length != 1) {
			throw new DisplayUsageException();
		}
		inputFile = args[0];
	}

	/**
	 * Returns the name of the input file.
	 * @return the name of the input file.
	 */
	public String getInputFile() {
		return inputFile;
	}
}
