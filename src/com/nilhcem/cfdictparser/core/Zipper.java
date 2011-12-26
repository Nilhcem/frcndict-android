package com.nilhcem.cfdictparser.core;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Compresses files into a .zip file.
 *
 * @author Nilhcem
 * @since 1.0
 */
public final class Zipper {
	private final String[] filesToCompress;
	private final String outputFile;

	/**
	 * @param outputFile the .zip file path.
	 * @param filesToCompress an array containing files path names which should be compressed.
	 */
	public Zipper(String outputFile, String[] filesToCompress) {
		this.outputFile = outputFile;
		this.filesToCompress = filesToCompress.clone();
	}

	/**
	 * Compresses files into a .zip file.
	 */
	public void start() {
		byte[] buffer = new byte[1024];

		try {
			// Create the ZIP file
			ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outputFile));

			// Compress the files
			for (String fileToCompress : filesToCompress) {
				FileInputStream fis = new FileInputStream(fileToCompress);
				BufferedInputStream in = new BufferedInputStream(fis, buffer.length);

				ZipEntry entry = new ZipEntry(fileToCompress);
				entry.setSize(new File(fileToCompress).length());
				out.putNextEntry(entry);

				// Transfer bytes from the file to the ZIP file
				int size = 0;
				while ((size = in.read(buffer, 0, buffer.length)) != -1) {
					out.write(buffer, 0, size);
				}

		        // Complete the entry
				out.flush();
		        out.closeEntry();
		        in.close();
			}

		    // Complete the ZIP file
		    out.close();
		} catch (IOException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}
}
