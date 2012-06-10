package com.nilhcem.frcndict.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

import android.app.Application;
import android.os.Environment;

import com.nilhcem.frcndict.database.DatabaseHelper;

public final class FileHandler {
	public static final String SD_BACKUP_RESTORE_FILE = "cfdict.xml";
	private static final String SD_PATH = "/Android/data/";
	private static final String INTERNAL_PATH = "/data/";

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
}
