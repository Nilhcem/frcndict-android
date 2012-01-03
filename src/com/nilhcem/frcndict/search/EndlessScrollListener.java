package com.nilhcem.frcndict.search;

import java.util.Observable;

import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

/* package-private */
final class EndlessScrollListener extends Observable implements OnScrollListener {
	private int currentPage; // Current loaded "page" of data
	private int previousTotal; // Total nb of items in the dataset.
	private boolean loading; // True if we are still waiting for the last set of data to load.

	EndlessScrollListener() {
		reset();
	}

	// called every time the list is scrolled to check if the latest element of the list is visible, to run a special operation
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		if (loading) {
			// If the dataset count has changed, it has finished loading
			if (totalItemCount > previousTotal) {
				loading = false;
				previousTotal = totalItemCount;
				currentPage++;
			}
		}
		// If it isn't currently loading, we check to see if we need to reload more data.
		if (!loading && totalItemCount > 0 && ((firstVisibleItem + visibleItemCount) == totalItemCount)) {
			loading = true;
			setChanged();
			notifyObservers(Integer.toString(currentPage));
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
	}

	public int getCurrentPage() {
		return currentPage;
	}
	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}

	public int getPreviousTotal() {
		return previousTotal;
	}
	public void setPreviousTotal(int previousTotal) {
		this.previousTotal = previousTotal;
	}

	public boolean isLoading() {
		return loading;
	}
	public void setLoading(boolean loading) {
		this.loading = loading;
	}

	public void reset() {
		currentPage = 0;
		previousTotal = 1; // Starts at 1 because of the "loading..." item
		loading = true;
	}
}
