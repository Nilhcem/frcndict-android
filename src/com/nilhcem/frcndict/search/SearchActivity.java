package com.nilhcem.frcndict.search;

import java.util.Observable;
import java.util.Observer;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.nilhcem.frcndict.AboutActivity;
import com.nilhcem.frcndict.ApplicationController;
import com.nilhcem.frcndict.R;
import com.nilhcem.frcndict.core.ClearableEditText;
import com.nilhcem.frcndict.core.ClearableEditText.ClearableTextObservable;
import com.nilhcem.frcndict.core.DictActivity;
import com.nilhcem.frcndict.meaning.WordMeaningActivity;

public final class SearchActivity extends DictActivity implements Observer {
	private SearchService mService;
	private TextView mInputText;
	private ListView mResultList;
	private SearchAdapter mSearchAdapter;
	private EndlessScrollListener mEndlessScrollListener;
	private Button mLangButton;
	private Toast mPressBackTwiceToast = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!isFinishing()) {
			setContentView(R.layout.search_dict);

			initResultList();
			initLangButton();
			initService();
			initInputText();

			restore(savedInstanceState);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		changeButtonLangState();
		mPressBackTwiceToast = null;
		mService.setLastBackPressTime(0l);
	}

	@Override
	protected void onPause() {
		if (mPressBackTwiceToast != null) {
			mPressBackTwiceToast.cancel();
		}
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
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.main_menu_about) {
			Intent intent = new Intent(this, AboutActivity.class);
			startActivity(intent);
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putCharSequence("search", mInputText.getText());
		outState.putInt("cur-page", mEndlessScrollListener.getCurrentPage());
		outState.putBoolean("loading", mEndlessScrollListener.isLoading());
		outState.putInt("prev-total", mEndlessScrollListener.getPreviousTotal());
		outState.putInt("langbtn-visibility", mLangButton.getVisibility());
		outState.putCharSequence("langbtn-text", mLangButton.getText());
	}

	private void restore(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			mInputText.setText(savedInstanceState.getCharSequence("search"));
			mEndlessScrollListener.setCurrentPage(savedInstanceState.getInt("cur-page"));
			mEndlessScrollListener.setLoading(savedInstanceState.getBoolean("loading"));
			mEndlessScrollListener.setPreviousTotal(savedInstanceState.getInt("prev-total"));
			mLangButton.setVisibility(savedInstanceState.getInt("langbtn-visibility"));
			mLangButton.setText(savedInstanceState.getCharSequence("langbtn-text"));
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
			mService.runSearchThread((String) data, mInputText.getText().toString());
		} else if (observable instanceof ClearableTextObservable) {
			clearResults();
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
			mSearchAdapter = new SearchAdapter(this, R.layout.search_dict_list_item, getLayoutInflater());
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

	private void initLangButton() {
		mLangButton = (Button) findViewById(R.id.searchLangBtn);
		mLangButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Switch search type
				if (mService.getSearchType() == SearchService.SEARCH_FRENCH) {
					mService.setSearchType(SearchService.SEARCH_PINYIN);
				} else {
					mService.setSearchType(SearchService.SEARCH_FRENCH);
				}
				runNewSearch();
			}
		});
	}
	private void initService() {
		mService = ((ApplicationController) getApplication()).getSearchDictService();
		mService.setActivity(this);
		mService.setAdapter(mSearchAdapter);
	}

	private void initInputText() {
		ClearableEditText clearableText = (ClearableEditText) findViewById(R.id.searchInput);
		clearableText.addObserver(this);

		mInputText = (TextView) clearableText.getEditText();
		mInputText.setHint(R.string.search_hint_text);
		mInputText.setNextFocusDownId(mInputText.getId());
		mInputText.setOnKeyListener(new View.OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN) {
					if (keyCode == KeyEvent.KEYCODE_ENTER) { // TODO: Remove this condition later
						mService.setSearchType(SearchService.SEARCH_UNDEFINED);
						runNewSearch();
						return true;
					}
				}
				return false;
			}
		});
	}

	private void runNewSearch() {
		clearResults();
		mSearchAdapter.addLoading();
		mService.runSearchThread(null, mInputText.getText().toString());
	}

	private void clearResults() {
		hideLangButton();
		mSearchAdapter.clear();
		mEndlessScrollListener.reset();
		mService.stopPreviousThread();
	}

	public void changeButtonLangState() {
		if (mService.getLangBtnResId() != 0) {
			mLangButton.setText(mService.getLangBtnResId());
		}
		mLangButton.setVisibility(mService.getLangBtnVisibility());
	}

	private void hideLangButton() {
		mLangButton.setVisibility(View.GONE);
		mService.setLangBtnVisibility(View.GONE);
	}
}
