package com.nilhcem.frcndict.starred;

import java.util.Observable;

import android.content.Context;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.nilhcem.frcndict.ApplicationController;
import com.nilhcem.frcndict.R;
import com.nilhcem.frcndict.core.list.AbstractListActivity;
import com.nilhcem.frcndict.core.list.AbstractSearchService;
import com.nilhcem.frcndict.core.list.EndlessScrollListener;

public final class StarredActivity extends AbstractListActivity {
	@Override
	protected int getLayoutResId() {
		return R.layout.starred_words;
	}

	@Override
	protected int getListResId() {
		return R.id.starredList;
	}

	@Override
	protected Context getPackageContext() {
		return StarredActivity.this;
	}

	@Override
	protected AbstractSearchService getService() {
		return ((ApplicationController) getApplication()).getStarredService();
	}

	@Override
	protected void onResume() {
		super.onResume();

		// Run a new search only if no search is running right now
		if (mListAdapter.isEmpty()) {
			refreshSearch(false);
		}
	}

	@Override
	protected void onDestroy() {
		if (mService != null) {
			mService.stopPreviousThread();
		}
		super.onDestroy();
	}

	@Override
	public void update(Observable observable, Object data) {
		if (observable instanceof EndlessScrollListener) {
			mService.runSearchThread(null, (String) data, null);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.starred_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean stopProcessing = true;

		if (item.getItemId() == R.id.starred_menu_refresh) {
			refreshSearch(true);
		} else if (item.getItemId() == R.id.starred_menu_back) {
			finish();
		} else {
			stopProcessing = super.onOptionsItemSelected(item);
		}
		return stopProcessing;
	}

	private void refreshSearch(boolean clearResults) {
		if (clearResults) {
			mListAdapter.clear();
			mEndlessScrollListener.reset();
			mService.stopPreviousThread();
		}
		mListAdapter.addLoading();
		mService.runSearchThread(null, null, null);
	}

	@Override
	protected void initBeforeRestore() {
		// Do nothing
	}
}
