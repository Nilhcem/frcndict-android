package com.nilhcem.frcndict.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import com.nilhcem.frcndict.ApplicationController;
import com.nilhcem.frcndict.R;

public final class SettingsActivity extends PreferenceActivity {
	// Shared preferences
	public static final String PREFS_NAME = "SharedPrefs";
	public static final String KEY_DB_PATH = "dbPath";

	public static final String KEY_CHINESE_CHARS = "chineseChars";
	public static final String VAL_CHINESE_CHARS_SIMP = "1";
	public static final String VAL_CHINESE_CHARS_TRAD = "2";
	public static final String VAL_CHINESE_CHARS_BOTH_ST = "3";
	public static final String VAL_CHINESE_CHARS_BOTH_TS = "4";

	public static final String KEY_PINYIN = "pinyin";
	public static final String VAL_PINYIN_NONE = "1";
	public static final String VAL_PINYIN_TONES = "2";
	public static final String VAL_PINYIN_NUMBER = "3";

	public static final String KEY_COLOR_HANZI = "hanziColoring";
	public static final String KEY_DARK_THEME = "darkTheme";
	public static final String KEY_DATABASE_UPDATES = "dbUpdates";

	// Other preferences
	public static final int NB_ENTRIES_PER_LIST = 20;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// Display night mode theme if set by user.
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		if (prefs.getBoolean(SettingsActivity.KEY_DARK_THEME, false)) {
			setTheme(R.style.DarkTheme);
		}

		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		prefs.registerOnSharedPreferenceChangeListener(((ApplicationController) getApplication())
				.getOnPreferencesChangedListener());
	}
}
