package com.nilhcem.frcndict.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Observable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public final class Unzip extends Observable {
	private File location;
	private File zipFile;

	public Unzip(File zipFile, File location) {
		this.zipFile = zipFile;
		this.location = location;
		dirChecker("");
	}

	private void dirChecker(String dir) {
		File curDir = new File(location.getAbsolutePath() + File.separator + dir);
		if (!curDir.isDirectory()) {
			curDir.mkdirs();
		}
	}

	private long getTotalSize() throws IOException {
		long totalSize = 0l;

		ZipFile zf = new ZipFile(zipFile);
		Enumeration<? extends ZipEntry> e = zf.entries();
		while (e.hasMoreElements()) {
			ZipEntry ze = (ZipEntry) e.nextElement();
			if (ze.getSize() > 0) {
				totalSize += ze.getSize();
			}
		}

		return totalSize;
	}

	public void start() throws IOException {
		long totalSize = 0l;

		if (countObservers() > 0) {
			totalSize = getTotalSize();
		}

		ZipInputStream zip = new ZipInputStream(new FileInputStream(zipFile));
		BufferedInputStream in = new BufferedInputStream(zip);

		long curSize = 0;
		byte[] buffer = new byte[1024];

		ZipEntry entry;
		while ((entry = zip.getNextEntry()) != null) {
			if (entry.isDirectory()) {
				dirChecker(entry.getName());
			} else {
				FileOutputStream fos = new FileOutputStream(location.getAbsolutePath() + File.separator + entry.getName());
				BufferedOutputStream out = new BufferedOutputStream(fos, buffer.length);

				int read;
				while ((read = in.read(buffer, 0, buffer.length)) != -1) {
					out.write(buffer, 0, read);
					if (totalSize != 0) {
						curSize += read;
						setChanged();
						notifyObservers((int) ((curSize * 100) / totalSize));
					}
				}
				out.flush();
				out.close();
				fos.close();
			}
			zip.closeEntry();
		}
		zip.close();
		in.close();
	}
}
