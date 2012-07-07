package com.nilhcem.frcndict.core.list;

import java.util.Observable;

import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

import com.nilhcem.frcndict.utils.Log;

public final class EndlessScrollListener extends Observable implements OnScrollListener {
	private int mCurrentPage; // Current loaded "page" of data
	private int mPreviousTotal; // Total nb of items in the dataset.
	private boolean mLoading; // True if we are still waiting for the last set of data to load.

	public EndlessScrollListener() {
		super();
		reset();
	}

	// called every time the list is scrolled to check if the latest element of the list is visible, to run a special operation
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		if (mLoading && (totalItemCount > mPreviousTotal)) {
			// If the dataset count has changed, it has finished loading
			mLoading = false;
			mPreviousTotal = totalItemCount;
			mCurrentPage++;
		}
		// If it isn't currently loading, we check to see if we need to reload more data.
		if (!mLoading && totalItemCount > 0 && ((firstVisibleItem + visibleItemCount) == totalItemCount)) {
			Log.d(EndlessScrollListener.class.getSimpleName(), "[End of scroll] Load more data");
			mLoading = true;
			setChanged();
			notifyObservers(Integer.toString(mCurrentPage));
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// Do nothing
	}

	public int getCurrentPage() {
		return mCurrentPage;
	}
	public void setCurrentPage(int currentPage) {
		mCurrentPage = currentPage;
	}

	public int getPreviousTotal() {
		return mPreviousTotal;
	}
	public void setPreviousTotal(int previousTotal) {
		mPreviousTotal = previousTotal;
	}

	public boolean isLoading() {
		return mLoading;
	}
	public void setLoading(boolean loading) {
		mLoading = loading;
	}

	public void reset() {
		mCurrentPage = 0;
		mPreviousTotal = 1; // Starts at 1 because of the "loading..." item
		mLoading = true;
	}
}
