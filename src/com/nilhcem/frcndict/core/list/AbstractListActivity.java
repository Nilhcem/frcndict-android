package com.nilhcem.frcndict.core.list;

import java.util.Observer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.nilhcem.frcndict.R;
import com.nilhcem.frcndict.core.AbstractDictActivity;
import com.nilhcem.frcndict.meaning.WordMeaningActivity;

public abstract class AbstractListActivity extends AbstractDictActivity implements Observer {

	protected AbstractSearchService mService;
	protected ListView mResultList;
	protected ListAdapter mListAdapter;
	protected EndlessScrollListener mEndlessScrollListener;

	protected abstract int getLayoutResId();
	protected abstract int getListResId();
	protected abstract Context getPackageContext();
	protected abstract AbstractSearchService getService();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(getLayoutResId());
		initResultList();
		initService();
		initBeforeRestore();
		restore(savedInstanceState);
	}

	// initializes some layouts before calling the restore method, nothing by default
	protected abstract void initBeforeRestore();

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// No call for super(). Bug on API Level > 11
		outState.putInt("cur-page", mEndlessScrollListener.getCurrentPage());
		outState.putBoolean("loading", mEndlessScrollListener.isLoading());
		outState.putInt("prev-total", mEndlessScrollListener.getPreviousTotal());
	}

	protected void restore(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			mEndlessScrollListener.setCurrentPage(savedInstanceState.getInt("cur-page"));
			mEndlessScrollListener.setLoading(savedInstanceState.getBoolean("loading"));
			mEndlessScrollListener.setPreviousTotal(savedInstanceState.getInt("prev-total"));
		}
	}

	// TODO: Deprecated
	// Saves the search adapter to keep results when application state change
	@Override
	public Object onRetainNonConfigurationInstance() {
		return (mListAdapter == null) ? super.onRetainNonConfigurationInstance() : mListAdapter;
	}

	protected void initResultList() {
		mEndlessScrollListener = new EndlessScrollListener();
		mEndlessScrollListener.addObserver(this);

		// TODO deprecated
		// Get the instance of the object that was stored if one exists
		if (getLastNonConfigurationInstance() == null) {
			mListAdapter = new ListAdapter(getPackageContext(), R.layout.search_dict_list_item, getLayoutInflater(), mPrefs);
		} else {
			mListAdapter = (ListAdapter) getLastNonConfigurationInstance();
		}
		mListAdapter.setTextSizesFromParent(this);

		mResultList = (ListView) findViewById(getListResId());
		mResultList.setAdapter(mListAdapter);
		mResultList.setOnScrollListener(mEndlessScrollListener);
		mResultList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (view.getId() > 0) { // not loading
					Intent intent = new Intent(getPackageContext(), WordMeaningActivity.class);
					intent.putExtra(WordMeaningActivity.ID_INTENT, view.getId());
					startActivity(intent);
				}
			}
		});
	}

	private void initService() {
		mService = getService();
		mService.setAdapter(mListAdapter);
	}
}
