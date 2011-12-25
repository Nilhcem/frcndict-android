package com.nilhcem.cfdictparser.args;

/**
 * Thrown if the program arguments were invalid.
 *
 * @author Nilhcem
 * @since 1.0
 */
public final class DisplayUsageException extends Exception {
	private static final long serialVersionUID = 7329560806636980938L;

	/**
	 * Displays the program usage.
	 *
	 * @return the program usage.
	 */
	@Override
	public String getMessage() {
		return "usage: java -jar CFDICT-parser.jar /path/to/cfdict.u8";
	}
}
