package com.nilhcem.frcndict.settings;

import android.content.SharedPreferences;

public final class OnPreferencesChangedListener implements SharedPreferences.OnSharedPreferenceChangeListener {
	private boolean themeHasChanged;
	private boolean resultListShouldBeUpdated;

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals(SettingsActivity.KEY_DARK_THEME)) {
			setThemeHasChanged(true);
		} else if (key.equals(SettingsActivity.KEY_CHINESE_CHARS)
				|| key.equals(SettingsActivity.KEY_PINYIN)
				|| key.equals(SettingsActivity.KEY_COLOR_HANZI)) {
			setResultListShouldBeUpdated(true);
		}
	}

	public boolean hasThemeChanged() {
		return themeHasChanged;
	}
	public void setThemeHasChanged(boolean themeHasChanged) {
		this.themeHasChanged = themeHasChanged;
	}

	public boolean shouldResultListBeUpdated() {
		return resultListShouldBeUpdated;
	}
	public void setResultListShouldBeUpdated(boolean resultListShouldBeUpdated) {
		this.resultListShouldBeUpdated = resultListShouldBeUpdated;
	}
}
