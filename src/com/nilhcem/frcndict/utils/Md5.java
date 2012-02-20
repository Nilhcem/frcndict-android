package com.nilhcem.frcndict.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.nilhcem.frcndict.core.Config;

import android.util.Log;

/**
 * Provides classes to compute md5 checksum.
 *
 * @author Nilhcem
 * @since 1.0
 */
public final class Md5 {
	private static final String TAG = "Md5";

	/**
	 * Returns the MD5 checksum of the file passed in parameters.
	 *
	 * @param file the file from which we should calculate the checksum.
	 * @return the MD5 checksum of the file passed in parameters.
	 * @throws FileNotFoundException if the file specified in parameters was not found.
	 */
	public static String getMd5Sum(File file) throws FileNotFoundException {
		// Get an MD5 implementation of MessageDigest
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			if (Config.LOGGING) {
				Log.e(Md5.TAG, e.getMessage());
			}
			return null;
		}

		// Open file and read contents
		InputStream is = new FileInputStream(file);
		byte[] buffer = new byte[8192];
		int read = 0;
		try {
			while ((read = is.read(buffer)) != -1) {
				// pass data read from file to digest for processing
				digest.update(buffer, 0, read);
			}
			is.close();
		} catch (IOException e) {
			if (Config.LOGGING) {
				Log.e(Md5.TAG, e.getMessage());
			}
			return null;
		}

		// Get the MD5 sum
		byte[] md5sum = digest.digest();
		return new BigInteger(1, md5sum).toString(16);
	}
}
