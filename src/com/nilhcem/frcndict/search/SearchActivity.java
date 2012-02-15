package com.nilhcem.frcndict.search;

import java.util.Observable;
import java.util.Observer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.nilhcem.frcndict.ApplicationController;
import com.nilhcem.frcndict.R;
import com.nilhcem.frcndict.core.AbstractMenuActivity;
import com.nilhcem.frcndict.core.ClearableEditText;
import com.nilhcem.frcndict.core.ClearableEditText.ClearableTextObservable;
import com.nilhcem.frcndict.meaning.WordMeaningActivity;
import com.nilhcem.frcndict.settings.OnPreferencesChangedListener;
import com.nilhcem.frcndict.settings.SettingsActivity;

public final class SearchActivity extends AbstractMenuActivity implements Observer {
	private SearchService mService;
	private TextView mIntroText;
	private TextView mInputText;
	private ListView mResultList;
	private Button mSearchButton;
	private SearchAdapter mSearchAdapter;
	private EndlessScrollListener mEndlessScrollListener;
	private Toast mPressBackTwiceToast = null;
	private Toast mSearchEmptyToast = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!isFinishing()) {
			setContentView(R.layout.search_dict);

			initResultList();
			initSearchButton();
			initService();
			initInputText();
			initIntroText();

			restore(savedInstanceState);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		// Display intro text only if search has not been started
		if (!mSearchAdapter.isEmpty()) {
			showHideIntroText(false);
		}
		changeSearchButtonBackground();
		mPressBackTwiceToast = null;
		mService.setLastBackPressTime(0l);

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
		if (mService.isBackBtnPressedForTheFirstTime()) {
			mPressBackTwiceToast = Toast.makeText(this, R.string.search_press_back_twice_exit, SearchService.BACK_TO_EXIT_TIMER);
			mPressBackTwiceToast.show();
			mService.setLastBackPressTime(System.currentTimeMillis());
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
		outState.putInt("cur-page", mEndlessScrollListener.getCurrentPage());
		outState.putBoolean("loading", mEndlessScrollListener.isLoading());
		outState.putInt("prev-total", mEndlessScrollListener.getPreviousTotal());
	}

	private void restore(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			mInputText.setText(savedInstanceState.getCharSequence("search"));
			mEndlessScrollListener.setCurrentPage(savedInstanceState.getInt("cur-page"));
			mEndlessScrollListener.setLoading(savedInstanceState.getBoolean("loading"));
			mEndlessScrollListener.setPreviousTotal(savedInstanceState.getInt("prev-total"));
		} else {
			mService.setSearchType(SearchService.SEARCH_UNDEFINED);
		}
	}

	// TODO: Deprecated
	// Saves the search adapter to keep results when application state change
	@Override
	public Object onRetainNonConfigurationInstance() {
		if (mSearchAdapter != null) {
			return mSearchAdapter;
		}
		return super.onRetainNonConfigurationInstance();
	}

	@Override
	public void update(Observable observable, Object data) {
		if (observable instanceof EndlessScrollListener) {
			mService.runSearchThread((String) data, mInputText.getText().toString(), this);
		} else if (observable instanceof ClearableTextObservable) {
			clearResults(true);
			changeSearchButtonBackground();
		}
	}

	private void initResultList() {
		mEndlessScrollListener = new EndlessScrollListener();
		mEndlessScrollListener.addObserver(this);

		// TODO deprecated
		// Get the instance of the object that was stored if one exists
		if (getLastNonConfigurationInstance() != null) {
			mSearchAdapter = (SearchAdapter) getLastNonConfigurationInstance();
		} else {
			mSearchAdapter = new SearchAdapter(this, R.layout.search_dict_list_item, getLayoutInflater(), prefs);
		}

		mResultList = (ListView) findViewById(R.id.searchList);
		mResultList.setAdapter(mSearchAdapter);
		mResultList.setOnScrollListener(mEndlessScrollListener);
		mResultList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (view.getId() > 0) { // not loading
					Intent intent = new Intent(SearchActivity.this, WordMeaningActivity.class);
					intent.putExtra(WordMeaningActivity.ID_INTENT, view.getId());
					startActivity(intent);
				}
			}
		});
	}

	private void initSearchButton() {
		mSearchButton = (Button) findViewById(R.id.searchBtn);
		mSearchButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Switch search type
				if (mService.getSearchType() == SearchService.SEARCH_FRENCH) {
					mService.setSearchType(SearchService.SEARCH_PINYIN);
				} else if (mService.getSearchType() == SearchService.SEARCH_PINYIN) {
					mService.setSearchType(SearchService.SEARCH_FRENCH);
				} else {
					mService.setSearchType(SearchService.SEARCH_UNDEFINED);
				}
				runNewSearch(false);
			}
		});
	}

	private void initService() {
		mService = ((ApplicationController) getApplication()).getSearchDictService();
		mService.setAdapter(mSearchAdapter);
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

	private void runNewSearch(boolean clearSearchType) {
		String text = mInputText.getText().toString();
		if (text.trim().length() == 0) {
			cancelToastIfNotNull(mSearchEmptyToast);
			mSearchEmptyToast = Toast.makeText(SearchActivity.this, R.string.search_empty_text, Toast.LENGTH_SHORT);
			mSearchEmptyToast.show();
		} else {
			clearResults(clearSearchType);
			showHideIntroText(false);
			mSearchAdapter.addLoading();
			mService.runSearchThread(null, mInputText.getText().toString(), this);
		}
	}

	private void clearResults(boolean clearSearchType) {
		if (clearSearchType) {
			mService.setSearchType(SearchService.SEARCH_UNDEFINED);
		}
		mSearchAdapter.clear();
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

		if (searchType == SearchService.SEARCH_UNDEFINED) {
			res = R.drawable.magnifier_selector;
		} else if (searchType == SearchService.SEARCH_FRENCH) {
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
