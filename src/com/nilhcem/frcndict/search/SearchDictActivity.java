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
	private SearchAsync mLastTask = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_dict);

		initResultList();
		initInputText();
	}

	@Override
	protected void onPause() {
		super.onPause();
//		db.close();
	}

	@Override
	protected void onResume() {
		super.onResume();
//		db.open();
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
	public void update(Observable observable, Object data) {
		if (observable instanceof EndlessScrollListener) {
			String curPage = (String) data;
			startSearchTask(curPage);
		}
	}

	private void initResultList() {
		EndlessScrollListener listener = new EndlessScrollListener();
		listener.addObserver(this);

		mSearchAdapter = new SearchAdapter(this, R.layout.search_dict_list_item, getLayoutInflater());
		mResultList = (ListView) findViewById(R.id.searchList);
		mResultList.setAdapter(mSearchAdapter);
		mResultList.setOnScrollListener(listener);
	}

	private void initInputText() {
		mInputText = (TextView) findViewById(R.id.searchInput);

		mInputText.setOnKeyListener(new View.OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
						(keyCode == KeyEvent.KEYCODE_ENTER)) { // TODO: Remove this condition later
					mSearchAdapter.clear();
					mSearchAdapter.addLoading();
					startSearchTask(null);
					return true;
				}
				return false;
			}
		});
	}

	private void startSearchTask(String curPage) { // curPage is null if first page
		if (mLastTask != null) {
			mLastTask.cancel(true);
		}

		mLastTask = new SearchAsync(mSearchAdapter);
		mLastTask.execute(curPage, mInputText.getText().toString());
	}
}
