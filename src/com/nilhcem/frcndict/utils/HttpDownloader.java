package com.nilhcem.frcndict.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.nilhcem.frcndict.core.AbstractCancellableObservable;

public final class HttpDownloader extends AbstractCancellableObservable {
	private static final int GENERIC_SIZE = 6000000;
	private final File mOutput;
	private final URL mUrl;

	public HttpDownloader(String urlStr, File outputFile) throws MalformedURLException {
		super();
		mOutput = outputFile;
		mUrl = new URL(urlStr);
	}

	@Override
	public void start() throws IOException {
		HttpURLConnection connection = (HttpURLConnection) mUrl.openConnection();
		connection.setRequestMethod("GET");
		connection.setDoOutput(true);
		connection.connect();

		int totalSize = connection.getContentLength();
		if (totalSize == -1) { // Small hack if this field is not set
			totalSize = HttpDownloader.GENERIC_SIZE;
		}
		FileOutputStream fos = new FileOutputStream(mOutput);
		InputStream is = connection.getInputStream();
		byte[] buffer = new byte[1024];
		long curSize = 0;

		int read;
		while ((!mCancelled && (read = is.read(buffer, 0, buffer.length)) != -1)) {
			fos.write(buffer, 0, read);

			// Notify percentage to observers
			if (countObservers() > 0) {
				curSize += read;
				updateProgress((int) ((curSize * 100) / totalSize));
			}
		}
		fos.close();
	}
}
