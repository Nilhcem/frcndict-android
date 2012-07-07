package com.nilhcem.frcndict.settings;

import android.content.SharedPreferences;

import com.nilhcem.frcndict.utils.Log;

public final class OnPreferencesChangedListener implements SharedPreferences.OnSharedPreferenceChangeListener {
	private boolean mThemeHasChanged;
	private boolean mResultListShouldBeUpdated;

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals(SettingsActivity.KEY_DARK_THEME)) {
			Log.d(OnPreferencesChangedListener.class.getSimpleName(), "[Has changed] Theme");
			setThemeHasChanged(true);
		} else if (key.equals(SettingsActivity.KEY_CHINESE_CHARS)
				|| key.equals(SettingsActivity.KEY_PINYIN)
				|| key.equals(SettingsActivity.KEY_COLOR_HANZI)
				|| key.equals(SettingsActivity.KEY_TEXT_SIZE)) {
			Log.d(OnPreferencesChangedListener.class.getSimpleName(), "[Has changed] Results appearance");
			setResultListShouldBeUpdated(true);
		}
	}

	public boolean hasThemeChanged() {
		return mThemeHasChanged;
	}
	public void setThemeHasChanged(boolean themeHasChanged) {
		mThemeHasChanged = themeHasChanged;
	}

	public boolean shouldResultListBeUpdated() {
		return mResultListShouldBeUpdated;
	}
	public void setResultListShouldBeUpdated(boolean resultListShouldBeUpdated) {
		mResultListShouldBeUpdated = resultListShouldBeUpdated;
	}
}
