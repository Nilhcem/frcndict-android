package com.nilhcem.frcndict.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;

import com.nilhcem.frcndict.ApplicationController;
import com.nilhcem.frcndict.R;
import com.nilhcem.frcndict.core.AbstractDictActivity;
import com.nilhcem.frcndict.utils.FileHandler;

public final class SettingsActivity extends PreferenceActivity {
	// Shared preferences
	public static final String PREFS_NAME = "SharedPrefs";
	public static final String KEY_DB_PATH_OLD = "dbPath";
	public static final String KEY_INSTALLED = "installed";

	public static final String KEY_CHINESE_CHARS = "chineseChars";
	public static final String VAL_CHINESE_CHARS_SIMP = "1";
	public static final String VAL_CHINESE_CHARS_TRAD = "2";
	public static final String VAL_CHINESE_CHARS_BOTH_ST = "3";
	public static final String VAL_CHINESE_CHARS_BOTH_TS = "4";

	public static final String KEY_PINYIN = "pinyin";
	public static final String VAL_PINYIN_NONE = "1";
	public static final String VAL_PINYIN_TONES = "2";
	public static final String VAL_PINYIN_NUMBER = "3";
	public static final String VAL_PINYIN_ZHUYIN = "4";

	public static final String KEY_COLOR_HANZI = "hanziColoring";
	public static final String KEY_DARK_THEME = "darkTheme";

	public static final String KEY_TEXT_SIZE = "textSize";
	public static final String VAL_TEXT_SIZE_SMALL = "1";
	public static final String VAL_TEXT_SIZE_MEDIUM = "2";
	public static final String VAL_TEXT_SIZE_BIG = "3";

	// Other preferences
	public static final int NB_ENTRIES_PER_LIST = 20;

	private static final String KEY_ADVANCED = "advanced";
	private static final String KEY_INSTALL_TTS = "installTTS";
	private static final String KEY_IMPORT_EXPORT = "importExport";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		AbstractDictActivity.checkForNightModeTheme(this, prefs);

		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		prefs.registerOnSharedPreferenceChangeListener(((ApplicationController) getApplication())
				.getOnPreferencesChangedListener());
		removeImportExportIfNoSdCard();
		removeInstallTtsIfNoFreeSpaceOnExternalStoreOrAlreadyInstalled();
	}

	// returns the array index depending on the user's prefs font sizes (small: 0, medium: 1, big: 2)
	public static int getArrayIdxFontSizes(SharedPreferences prefs) {
		int index;

		String sizePref = prefs.getString(SettingsActivity.KEY_TEXT_SIZE, SettingsActivity.VAL_TEXT_SIZE_MEDIUM);
		if (sizePref.equals(SettingsActivity.VAL_TEXT_SIZE_SMALL)) {
			index = 0;
		} else if (sizePref.equals(SettingsActivity.VAL_TEXT_SIZE_MEDIUM)) {
			index = 1;
		} else { // VAL_TEXT_SIZE_BIG
			index = 2;
		}
		return index;
	}

	private void removeImportExportIfNoSdCard() {
		PreferenceCategory advanced = (PreferenceCategory) findPreference(SettingsActivity.KEY_ADVANCED);
		Preference importExport = findPreference(SettingsActivity.KEY_IMPORT_EXPORT);
		if (advanced != null && importExport != null && !FileHandler.isSdCardMounted()) {
			advanced.removePreference(importExport);
		}
	}

	/**
	 * Remove option if voices are already installed or if available space on external storage < 100MB.
	 */
	private void removeInstallTtsIfNoFreeSpaceOnExternalStoreOrAlreadyInstalled() {
		PreferenceCategory advanced = (PreferenceCategory) findPreference(SettingsActivity.KEY_ADVANCED);
		Preference installTts = findPreference(SettingsActivity.KEY_INSTALL_TTS);

		if (advanced != null && installTts != null
				&& (FileHandler.getFreeSpaceInExternalStorage() < 100
						|| FileHandler.areVoicesInstalled())) {
			advanced.removePreference(installTts);
		}
	}
}
