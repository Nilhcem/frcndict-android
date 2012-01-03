package com.nilhcem.frcndict.search;

import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.nilhcem.frcndict.R;
import com.nilhcem.frcndict.database.DatabaseHelper;

public final class SearchDictActivity extends Activity implements Observer {
	private DatabaseHelper db = DatabaseHelper.getInstance();
	private TextView mInputText;
	private ListView mResultList;
	private SearchAdapter mSearchAdapter;
	private EndlessScrollListener mEndlessScrollListener;
	private SearchAsync mLastTask = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_dict);

		initResultList();
		initInputText();

		restore(savedInstanceState);
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onStart() {
		super.onStart();
		db.open();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		db.close();
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
			String curPage = (String) data;
			startSearchTask(curPage);
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
	}

	private void initInputText() {
		mInputText = (TextView) findViewById(R.id.searchInput);

		mInputText.setOnKeyListener(new View.OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
						(keyCode == KeyEvent.KEYCODE_ENTER)) { // TODO: Remove this condition later
					mEndlessScrollListener.reset();
					mSearchAdapter.clear();
					mSearchAdapter.addLoading();
					startSearchTask(null);
					return true;
				}
				return false;
			}
		});
	}

	private void startSearchTask(String curPage) { // curPage should be null if first page
		if (mLastTask != null) {
			mLastTask.cancel(true);
		}

		mLastTask = new SearchAsync(mSearchAdapter);
		mLastTask.execute(curPage, mInputText.getText().toString());
	}
}
