package com.nilhcem.frcndict.search;

import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.nilhcem.frcndict.R;
import com.nilhcem.frcndict.database.DatabaseHelper;
import com.nilhcem.frcndict.meaning.WordMeaningActivity;

public final class SearchDictActivity extends Activity implements Observer {
	private DatabaseHelper db = DatabaseHelper.getInstance();
	private TextView mInputText;
	private ListView mResultList;
	private SearchAdapter mSearchAdapter;
	private EndlessScrollListener mEndlessScrollListener;
	private SearchAsync mLastTask = null;
	private Toast mPressBackTwiceToast = null;
	private long lastBackPressTime = 0;

	private static final int BACK_TO_EXIT_TIMER = 4000;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_dict);

		initResultList();
		initInputText();

		restore(savedInstanceState);
	}

	@Override
	protected void onStart() {
		super.onStart();
		db.openIfNotAlready();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mPressBackTwiceToast = null;
		lastBackPressTime = 0;
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
		if (lastBackPressTime < System.currentTimeMillis() - SearchDictActivity.BACK_TO_EXIT_TIMER) {
			mPressBackTwiceToast = Toast.makeText(this, R.string.search_press_back_twice_exit, SearchDictActivity.BACK_TO_EXIT_TIMER);
			mPressBackTwiceToast.show();
			lastBackPressTime = System.currentTimeMillis();
		} else {
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

		mResultList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (view.getId() > 0) { // not loading
					Intent intent = new Intent(SearchDictActivity.this, WordMeaningActivity.class);
					intent.putExtra(WordMeaningActivity.ID_INTENT, view.getId());
					startActivity(intent);
				}
			}
		});
	}

	private void initInputText() {
		mInputText = (TextView) findViewById(R.id.searchInput);

		mInputText.setOnKeyListener(new View.OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN) {
					if (keyCode == KeyEvent.KEYCODE_ENTER) { // TODO: Remove this condition later
						mEndlessScrollListener.reset();
						mSearchAdapter.clear();
						mSearchAdapter.addLoading();
						startSearchTask(null);
						return true;
					}
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
