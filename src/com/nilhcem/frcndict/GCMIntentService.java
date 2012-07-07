package com.nilhcem.frcndict;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;
import com.nilhcem.frcndict.core.Config;
import com.nilhcem.frcndict.gcm.GCMServerUtilities;
import com.nilhcem.frcndict.settings.SettingsActivity;
import com.nilhcem.frcndict.utils.Log;

public final class GCMIntentService extends GCMBaseIntentService {
	private static final String TAG = "GCMIntentService";

	public GCMIntentService() {
		super(Config.GCM_SENDER_ID);
	}

	@Override
	protected void onRegistered(Context context, String registrationId) {
		Log.i(TAG, "Device registered: regId = %s", registrationId);
		GCMServerUtilities.register(context, registrationId);
	}

	@Override
	protected void onUnregistered(Context context, String registrationId) {
		Log.i(TAG, "Device unregistered");
		if (GCMRegistrar.isRegisteredOnServer(context)) {
			GCMServerUtilities.unregister(context, registrationId);
		} else {
			Log.i(TAG, "Ignoring unregister callback");
		}
	}

	@Override
	protected void onMessage(Context context, Intent intent) {
		Log.i(TAG, "Received message");

		// Check if push is enabled
		Bundle extras = intent.getExtras();
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		boolean checkForUpdates = preferences.getBoolean(SettingsActivity.KEY_DATABASE_UPDATES, false);
		if (checkForUpdates) {
			if (extras != null) {
				try {
					String type = URLDecoder.decode((String) extras.get("type"), "UTF-8");
					if (type.equals("updateAvailable")) {
						// Save push to display notification when app will start again
						String version = URLDecoder.decode((String) extras.get("version"), "UTF-8");
						savePushInPrefs(version);
					}
				} catch (IllegalArgumentException e) {
					Log.e(TAG, e, "Error while decoding string");
				} catch (UnsupportedEncodingException e) { // Should not happen
					Log.e(TAG, e, "UTF-8 encoding is not supported");
				}
			}
		} else {
			GCMRegistrar.unregister(context);
		}
	}

	@Override
	public void onError(Context context, String errorId) {
		Log.e(TAG, "Received error: %s", errorId);
	}

	/**
	 * Save the push in the preferences in order to display that an update is available next time the app is started.
	 * <p>
	 * Next time the application starts, it will check if a push was received.
	 * If a push was received, display a notification if an update is available.<br />
	 * This way, users won't all download updates at the same time
	 * and won't be disturbed if they don't want to use the application.
	 * </p>
	 */
	private void savePushInPrefs(String version) {
		SharedPreferences prefs = getSharedPreferences(SettingsActivity.PREFS_NAME, MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(SettingsActivity.KEY_DATABASE_UPDATES, version);
		editor.commit();
	}
}
