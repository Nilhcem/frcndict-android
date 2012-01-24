package com.nilhcem.frcndict.settings;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.nilhcem.frcndict.R;

public final class SettingsActivity extends PreferenceActivity {
	// Shared preferences
	public static final String PREFS_NAME = "SharedPrefs";
	public static final String KEY_DB_PATH = "dbPath";
	public static final String KEY_CHINESE_CHARS = "chineseChars";
	public static final String KEY_PINYIN = "pinyin";
	public static final String KEY_COLOR_HANZI = "hanziColoring";
	public static final String KEY_DARK_THEME = "darkTheme";
	public static final String KEY_DATABASE_UPDATES = "dbUpdates";

	// Other preferences
	public static final int NB_ENTRIES_PER_LIST = 20;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}
}
