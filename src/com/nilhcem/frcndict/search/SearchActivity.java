package com.nilhcem.frcndict.search;

import java.util.Observable;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.nilhcem.frcndict.ApplicationController;
import com.nilhcem.frcndict.R;
import com.nilhcem.frcndict.about.AboutDialog;
import com.nilhcem.frcndict.core.ClearableEditText;
import com.nilhcem.frcndict.core.ClearableEditText.ClearableTextObservable;
import com.nilhcem.frcndict.core.list.AbstractListActivity;
import com.nilhcem.frcndict.core.list.AbstractSearchService;
import com.nilhcem.frcndict.core.list.EndlessScrollListener;
import com.nilhcem.frcndict.settings.OnPreferencesChangedListener;
import com.nilhcem.frcndict.settings.SettingsActivity;
import com.nilhcem.frcndict.starred.StarredActivity;

public final class SearchActivity extends AbstractListActivity {
	private TextView mIntroText;
	private TextView mInputText;
	private Button mSearchButton;
	private Toast mPressBackTwiceToast = null;
	private Toast mSearchEmptyToast = null;
	private Dialog mAboutDialog = null;

	@Override
	protected int getLayoutResId() {
		return R.layout.search_dict;
	}

	@Override
	protected int getListResId() {
		return R.id.searchList;
	}

	@Override
	protected Context getPackageContext() {
		return SearchActivity.this;
	}

	@Override
	protected AbstractSearchService getService() {
		return ((ApplicationController) getApplication()).getSearchDictService();
	}

	@Override
	protected void initBeforeRestore() {
		initSearchButton();
		initInputText();
		initIntroText();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			if (savedInstanceState.getBoolean("about-displayed", false)) {
				createAboutDialog().show();
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		// Display intro text only if search has not been started
		if (!mListAdapter.isEmpty()) {
			showHideIntroText(false);
		}
		changeSearchButtonBackground();
		mPressBackTwiceToast = null;
		((SearchService)mService).setLastBackPressTime(0l);

		OnPreferencesChangedListener listener = ((ApplicationController) getApplication())
				.getOnPreferencesChangedListener();
		// If theme was changed, restart activity.
		if (listener.hasThemeChanged()) {
			listener.setThemeHasChanged(false);
			Intent intent = getIntent();
			finish();
			startActivity(intent);
		} else if (listener.shouldResultListBeUpdated()) {
			// refresh views after back from settings
			listener.setResultListShouldBeUpdated(false);
			mResultList.invalidateViews();
		}
	}

	@Override
	protected void onPause() {
		cancelToastIfNotNull(mPressBackTwiceToast);
		cancelToastIfNotNull(mSearchEmptyToast);
		super.onPause();
	}

	@Override
	public void onBackPressed() {
		SearchService searchService = (SearchService) mService;

		if (searchService.isBackBtnPressedForTheFirstTime()) {
			mPressBackTwiceToast = Toast.makeText(this, R.string.search_press_back_twice_exit, SearchService.BACK_TO_EXIT_TIMER);
			mPressBackTwiceToast.show();
			searchService.setLastBackPressTime(System.currentTimeMillis());
		} else {
			// It is a real exit, close DB
			mService.stopPreviousThread();
			db.close();
			super.onBackPressed();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putCharSequence("search", mInputText.getText());

		if (mAboutDialog != null && mAboutDialog.isShowing()) {
			outState.putBoolean("about-displayed", true);
			mAboutDialog.dismiss();
		}
	}

	@Override
	protected void restore(Bundle savedInstanceState) {
		super.restore(savedInstanceState);

		if (savedInstanceState != null) {
			mInputText.setText(savedInstanceState.getCharSequence("search"));
		} else {
			mService.setSearchType(AbstractSearchService.SEARCH_UNDEFINED);
		}
	}

	@Override
	public void update(Observable observable, Object data) {
		if (observable instanceof EndlessScrollListener) {
			mService.runSearchThread(this, (String) data, mInputText.getText().toString());
		} else if (observable instanceof ClearableTextObservable) {
			clearResults(true);
			changeSearchButtonBackground();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.main_menu_about) {
			createAboutDialog().show();
			return true;
		} else if (item.getItemId() == R.id.main_menu_settings) {
			startActivity(new Intent(this, SettingsActivity.class));
			return true;
		} else if (item.getItemId() == R.id.main_menu_starred) {
			startActivity(new Intent(this, StarredActivity.class));
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	private void initSearchButton() {
		mSearchButton = (Button) findViewById(R.id.searchBtn);
		mSearchButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Switch search type
				if (mService.getSearchType() == AbstractSearchService.SEARCH_FRENCH) {
					mService.setSearchType(AbstractSearchService.SEARCH_PINYIN);
				} else if (mService.getSearchType() == AbstractSearchService.SEARCH_PINYIN) {
					mService.setSearchType(AbstractSearchService.SEARCH_FRENCH);
				} else {
					mService.setSearchType(AbstractSearchService.SEARCH_UNDEFINED);
				}
				runNewSearch(false);
			}
		});
	}

	private void initInputText() {
		ClearableEditText clearableText = (ClearableEditText) findViewById(R.id.searchInput);
		clearableText.addObserver(this);

		mInputText = (TextView) clearableText.getEditText();
		mInputText.setHint(R.string.search_hint_text);
		mInputText.setNextFocusDownId(mInputText.getId());
		mInputText.setImeOptions(EditorInfo.IME_ACTION_SEARCH); // set search icon as the keyboard return key
		mInputText.setOnKeyListener(new View.OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN) {
					if (keyCode == KeyEvent.KEYCODE_ENTER) {
						runNewSearch(true);
						// Hide keyboard
		                InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		                in.hideSoftInputFromWindow(mInputText.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
						return true;
					}
				}
				return false;
			}
		});
	}

	private void initIntroText() {
		mIntroText = (TextView) findViewById(R.id.searchIntro);
		mIntroText.setText(Html.fromHtml(
			String.format("<font color=\"%s\"><b>%s</b></font><br />%s",
				getResources().getStringArray(R.array.introTitleColors)[prefs.getBoolean(SettingsActivity.KEY_DARK_THEME, false) ? 1 : 0],
				getString(R.string.app_name), getString(R.string.search_intro))));
	}

	private Dialog createAboutDialog() {
		mAboutDialog = new AboutDialog(this, R.style.AboutDialog);
		return mAboutDialog;
	}

	private void runNewSearch(boolean clearSearchType) {
		String text = mInputText.getText().toString();
		if (text.trim().length() == 0) {
			cancelToastIfNotNull(mSearchEmptyToast);
			mSearchEmptyToast = Toast.makeText(SearchActivity.this, R.string.search_empty_text, Toast.LENGTH_SHORT);
			mSearchEmptyToast.show();
		} else {
			clearResults(clearSearchType);
			showHideIntroText(false);
			mListAdapter.addLoading();
			mService.runSearchThread(this, null, mInputText.getText().toString());
		}
	}

	private void clearResults(boolean clearSearchType) {
		if (clearSearchType) {
			mService.setSearchType(AbstractSearchService.SEARCH_UNDEFINED);
		}
		mListAdapter.clear();
		mEndlessScrollListener.reset();
		mService.stopPreviousThread();
		showHideIntroText(true);
	}

	private void showHideIntroText(boolean show) {
		if (show) {
			mResultList.setVisibility(View.GONE);
			mIntroText.setVisibility(View.VISIBLE);
		} else {
			mIntroText.setVisibility(View.GONE);
			mResultList.setVisibility(View.VISIBLE);
		}
	}

	public void changeSearchButtonBackground() {
		int res = 0;
		int searchType = mService.getSearchType();

		if (searchType == AbstractSearchService.SEARCH_UNDEFINED) {
			res = R.drawable.magnifier_selector;
		} else if (searchType == AbstractSearchService.SEARCH_FRENCH) {
			res = R.drawable.magnifier_fr_selector;
		} else { // hanzi - pinyin
			res = R.drawable.magnifier_cn_selector;
		}
		mSearchButton.setBackgroundResource(res);
	}

	private void cancelToastIfNotNull(Toast toast) {
		if (toast != null) {
			toast.cancel();
		}
	}
}
