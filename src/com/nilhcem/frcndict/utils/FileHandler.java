package com.nilhcem.frcndict.utils;

import java.io.File;
import java.util.Locale;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;

public final class FileHandler {

	public static final String SD_BACKUP_RESTORE_FILE = "cfdict.xml";
	private static final String SD_PATH = "/Android/data/";
	private static final String INTERNAL_PATH = "/data/";
	private static final String VOICES_DIR = "/chinese-tts-data";
	private static final String STROKES_DIR = "/chinese-stroke-data";
	private static final String STROKES_EXT = ".gif";
	private static final int BYTES_IN_A_MB = 1048576; // 1048576 = Nb of bytes in a MB: 1 * 1024 (kb) * 1024 (mb)

	private FileHandler() {
		throw new UnsupportedOperationException();
	}

	public static String getDbStorageDirectory(Context context) {
		File external = FileHandler.getAppRootDir(context.getApplicationContext(), true);
		if (external != null && external.exists() && external.canRead() && external.canWrite()) {
			return external.getAbsolutePath();
		} else {
			external = FileHandler.getAppRootDir(context.getApplicationContext(), false);
			if (external != null && external.exists() && external.canRead() && external.canWrite()) {
				return external.getAbsolutePath();
			}
		}
		return null;
	}

	private static File getAppRootDir(Context appContext, boolean sdcard) {
		File rootDir = null;

		if (sdcard) {
			File externalDir = Environment.getExternalStorageDirectory();
			if (externalDir == null) {
				sdcard = false;
			} else {
				rootDir = new File(externalDir.getAbsolutePath()
						+ FileHandler.SD_PATH + appContext.getClass().getPackage().getName());
			}
		}
		if (!sdcard) {
			rootDir = new File(Environment.getDataDirectory().getAbsolutePath()
				+ FileHandler.INTERNAL_PATH + appContext.getClass().getPackage().getName());
		}

		if (!rootDir.exists()) {
			rootDir.mkdirs();
		}
		return rootDir;
	}

	public static boolean isSdCardMounted() {
		return android.os.Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED);
	}

	public static File getBackupRestoreFile() {
		return new File(Environment.getExternalStorageDirectory().getAbsolutePath()
				+ "/" + FileHandler.SD_BACKUP_RESTORE_FILE);
	}

	public static File getVoicesDir() {
		File file = null;

		if (isSdCardMounted()) {
			file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
				+ FileHandler.VOICES_DIR);
		}
		return file;
	}

	/**
	 * @return free space in external storage (in MB).
	 */
	public static double getFreeSpaceInExternalStorage() {
		double freeSpace = 0;

		if (isSdCardMounted()) {
			StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
			double sdAvailSize = (double)stat.getAvailableBlocks() * (double)stat.getBlockSize();
			freeSpace = sdAvailSize / FileHandler.BYTES_IN_A_MB;
		}
		return freeSpace;
	}

	public static File getStrokesDir() {
		File file = null;

		if (isSdCardMounted()) {
			file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
				+ FileHandler.STROKES_DIR);
		}
		return file;
	}

	public static File getStrokesFile(String hanzi) {
		File strokeFile = null;

		File strokeDir = FileHandler.getStrokesDir();
		if (strokeDir != null) {
			strokeFile = new File(strokeDir, String.format(Locale.US, "%s%s", hanzi, FileHandler.STROKES_EXT));
		}
		return strokeFile;
	}

	public static boolean areVoicesInstalled() {
		boolean installed = false;

		File voices = FileHandler.getVoicesDir();
		if (voices != null) {
			// Check if it contains some data
			File mp3 = new File(voices, "zhang1.mp3");
			if (mp3.isFile()) {
				installed = true;
			}
		}
		return installed;
	}
}
