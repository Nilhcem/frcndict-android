package com.nilhcem.frcndict.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

import android.app.Application;
import android.os.Environment;
import android.os.StatFs;

import com.nilhcem.frcndict.database.DatabaseHelper;

public final class FileHandler {
	public static final String SD_BACKUP_RESTORE_FILE = "cfdict.xml";
	private static final String SD_PATH = "/Android/data/";
	private static final String INTERNAL_PATH = "/data/";
	private static final String VOICES_DIR = "/chinese-tts-data";
	private static final int BYTES_IN_A_MB = 1048576; // 1048576 = Nb of bytes in a MB: 1 * 1024 (kb) * 1024 (mb)

	private FileHandler() {
	}

	public static String readFile(File file) throws IOException {
		FileInputStream stream = new FileInputStream(file);
		try {
			FileChannel fc = stream.getChannel();
			MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
			return Charset.defaultCharset().decode(bb).toString();
		} finally {
			stream.close();
		}
	}

	public static File getAppRootDir(Application app, boolean sdcard) {
		File rootDir;

		if (sdcard) {
			// API Level 8: Use getExternalFilesDir(null).getAbsolutePath() instead
			rootDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
				+ FileHandler.SD_PATH + app.getClass().getPackage().getName());
		} else {
			rootDir = new File(Environment.getDataDirectory().getAbsolutePath()
				+ FileHandler.INTERNAL_PATH + app.getClass().getPackage().getName());
		}

		if (!rootDir.exists()) {
			rootDir.mkdirs();
		}
		return rootDir;
	}

	public static boolean isDatabaseInstalledOnSDcard() {
		File dbPath = DatabaseHelper.getInstance().getDatabasePath();

		if (dbPath != null && dbPath.getAbsolutePath().startsWith(
				Environment.getExternalStorageDirectory().getAbsolutePath() + FileHandler.SD_PATH)) {
			return true;
		}
		return false;
	}

	public static boolean isSdCardMounted() {
		return android.os.Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED);
	}

	public static File getBackupRestoreFile() {
		File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
				+ "/" + FileHandler.SD_BACKUP_RESTORE_FILE);
		return file;
	}

	public static File getVoicesDir() {
		File file = null;

		if (isSdCardMounted()) {
			file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
				+ FileHandler.VOICES_DIR);
		}
		return file;
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
}
