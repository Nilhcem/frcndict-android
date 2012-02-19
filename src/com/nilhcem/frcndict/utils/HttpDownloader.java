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
	private final File output;
	private final URL url;

	public HttpDownloader(String urlStr, File outputFile) throws MalformedURLException {
		super();
		this.output = outputFile;
		this.url = new URL(urlStr);
	}

	@Override
	public void start() throws IOException {
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("GET");
		connection.setDoOutput(true);
		connection.connect();

		int totalSize = connection.getContentLength();
		FileOutputStream fos = new FileOutputStream(output);
		InputStream is = connection.getInputStream();
		byte[] buffer = new byte[1024];
		long curSize = 0;

		int read;
		while ((!cancelled && (read = is.read(buffer, 0, buffer.length)) != -1)) {
			fos.write(buffer, 0, read);

			// Notify percentage to observers
			if (this.countObservers() > 0) {
				curSize += read;
				updateProgress((int) ((curSize * 100) / totalSize));
			}
		}
		fos.close();
	}
}
