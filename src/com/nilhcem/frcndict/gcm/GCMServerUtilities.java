package com.nilhcem.frcndict.gcm;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import android.content.Context;
import android.util.Log;

import com.google.android.gcm.GCMRegistrar;
import com.nilhcem.frcndict.core.Config;

public final class GCMServerUtilities {
	private static final String TAG = "GCMServerUtilities";
	private static final String GCM_REGISTER_URL = Config.GCM_SERVER_URL + "register";
	private static final String GCM_UNREGISTER_URL = Config.GCM_SERVER_URL + "unregister";

	private static final int MAX_ATTEMPTS = 3;
	private static final int BACKOFF_MILLI_SECONDS = 2000;
	private static final Random random = new Random();

	private GCMServerUtilities() {
	}

	/**
	 * Register this account/device pair within the server.
	 * <p>
	 * Note: Must be called in a thread.<br />
	 * As the server might be down, we will retry it a couple times.
	 * </p>
	 *
	 * @return whether the registration succeeded or not.
	 */
	public static boolean register(final Context context, final String regId) {
		if (Config.LOG_INFO) Log.i(TAG, "registering device (regId = " + regId + ")");
		Map<String, String> params = new HashMap<String, String>();
		params.put("regId", regId);

		boolean success = false;
		long backoff = BACKOFF_MILLI_SECONDS + random.nextInt(1000);
		for (int i = 1; i <= MAX_ATTEMPTS; i++) {
			if (Config.LOG_DEBUG) Log.d(TAG, "Attempt #" + i + " to register");
			try {
				post(GCM_REGISTER_URL, params);
				GCMRegistrar.setRegisteredOnServer(context, true);
				success = true;
				break;
			} catch (IOException e) {
				if (Config.LOG_ERROR) Log.e(TAG, "Failed to register on attempt " + i, e);
				if (i != MAX_ATTEMPTS) {
					try {
						if (Config.LOG_DEBUG) Log.d(TAG, "Sleeping for " + backoff + " ms before retry");
						Thread.sleep(backoff);
					} catch (InterruptedException e1) {
						// Activity finished before we complete - exit.
						if (Config.LOG_DEBUG) Log.d(TAG, "Thread interrupted: abort remaining retries!");
						Thread.currentThread().interrupt();
						break;
					}
					// increase backoff exponentially
					backoff *= 2;
				}
			}
		}
        return success;
	}

	/**
	 * Unregister this account/device pair within the server.
	 * <p>
	 * Note: Must be called in a thread.
	 * </p>
	 */
	public static void unregister(final Context context, final String regId) {
		if (Config.LOG_INFO) Log.i(TAG, "unregistering device (regId = " + regId + ")");
        Map<String, String> params = new HashMap<String, String>();
        params.put("regId", regId);
        try {
            post(GCM_UNREGISTER_URL, params);
            GCMRegistrar.setRegisteredOnServer(context, false);
            if (Config.LOG_INFO) Log.i(TAG, "Unregistered");
        } catch (IOException e) {
            // At this point the device is unregistered from GCM, but still registered in the server.
            // We could try to unregister again, but it is not necessary:
            // if the server tries to send a message to the device, it will get
            // a "NotRegistered" error message and should unregister the device.
            if (Config.LOG_WARN) Log.w(TAG, "Failed to unregister");
        }
	}

	/**
	 * Issue a POST request to the server.
	 *
	 * @param endpoint POST address.
	 * @param params request parameters.
	 *
	 * @throws IOException propagated from POST.
	 */
	private static void post(String endpoint, Map<String, String> params) throws IOException {
		URL url;
		try {
			url = new URL(endpoint);
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("invalid url: " + endpoint);
		}

		// Constructs the POST body using the parameters
		StringBuilder bodyBuilder = new StringBuilder();
		Iterator<Entry<String, String>> iterator = params.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, String> param = iterator.next();
			bodyBuilder.append(param.getKey()).append('=').append(param.getValue());
			if (iterator.hasNext()) {
				bodyBuilder.append('&');
			}
		}
		String body = bodyBuilder.toString();

		if (Config.LOG_DEBUG) Log.d(TAG, "Posting '" + body + "' to " + url);
		byte[] bytes = body.getBytes();
		HttpURLConnection conn = null;
		try {
			conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setUseCaches(false);
			conn.setFixedLengthStreamingMode(bytes.length);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded;charset=UTF-8");

			// Post the request
			OutputStream out = conn.getOutputStream();
			out.write(bytes);
			out.close();

			// Handle the response
			int status = conn.getResponseCode();
			if (status != 200) {
				throw new IOException("Post failed with error code " + status);
			}
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
	}
}
