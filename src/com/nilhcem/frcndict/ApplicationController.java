package com.nilhcem.frcndict;

import java.io.File;

import android.app.Application;
import android.os.Environment;

import com.nilhcem.frcndict.database.DatabaseHelper;

public final class ApplicationController extends Application {
	private File rootDir;

	@Override
	public void onCreate() {
		super.onCreate();
		initRootDir();
		DatabaseHelper.getInstance().setDatabaseFolder(rootDir); //set sdcard as default
	}

	private void initRootDir() {
		rootDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
				+ "/Android/data/" + getClass().getPackage().getName());
		if (!rootDir.exists()) {
			rootDir.mkdirs();
		}
	}

	public File getRootDir() {
		return rootDir;
	}
}
