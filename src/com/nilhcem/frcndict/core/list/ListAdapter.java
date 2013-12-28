package com.nilhcem.frcndict.core.list;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.Html;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.nilhcem.frcndict.R;
import com.nilhcem.frcndict.database.Entry;
import com.nilhcem.frcndict.settings.SettingsActivity;
import com.nilhcem.frcndict.utils.ChineseCharsHandler;

public final class ListAdapter extends ArrayAdapter<Entry> {

	private boolean mSearchIsOver; // true if no more result to avoid checking database
	private float[] mListItemSizes;
	private final Entry mLoading;
	private final Entry mNoResults;
	private final LayoutInflater mInflater;
	private final SharedPreferences mPrefs;

	public ListAdapter(Context context, int textViewResourceId, LayoutInflater inflater, SharedPreferences prefs) {
		super(context, textViewResourceId);
		mSearchIsOver = false;
		mInflater = inflater;
		mLoading = new Entry();
		mNoResults = new Entry();
		mPrefs = prefs;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Entry entry = getItem(position);

		int resId;
		if (entry.equals(mLoading)) {
			resId = R.layout.search_dict_loading;
		} else if (entry.equals(mNoResults)) {
			resId = R.layout.search_dict_no_results;
		} else {
			resId = R.layout.search_dict_list_item;
		}

		View view = mInflater.inflate(resId, parent, false);

		if (entry.equals(mLoading) || entry.equals(mNoResults)) {
			// Make entry unclickable
			view.setId(0);
			view.setClickable(false);
			view.setOnTouchListener(null);
			view.setOnLongClickListener(null);
		} else {
			// Get references
			TextView chinese = (TextView) view.findViewById(R.id.slChinese);
			TextView pinyin = (TextView) view.findViewById(R.id.slPinyin);
			TextView desc = (TextView) view.findViewById(R.id.slDesc);

			// Set sizes
			int arrayIdx = SettingsActivity.getArrayIdxFontSizes(mPrefs);
			chinese.setTextSize(TypedValue.COMPLEX_UNIT_SP, mListItemSizes[arrayIdx]);
			pinyin.setTextSize(TypedValue.COMPLEX_UNIT_SP, mListItemSizes[3 + arrayIdx]);
			desc.setTextSize(TypedValue.COMPLEX_UNIT_SP, mListItemSizes[6 + arrayIdx]);

			ChineseCharsHandler charsHandler = ChineseCharsHandler.getInstance();
			view.setId(entry.getId());
			chinese.setText(Html.fromHtml(charsHandler.formatHanzi(entry.getSimplified(),
					entry.getTraditional(), entry.getPinyin() , mPrefs)));
			String pinyinStr = charsHandler.formatPinyin(entry.getPinyin(), mPrefs);
			if (TextUtils.isEmpty(pinyinStr)) {
				pinyin.setVisibility(View.GONE); // hide pinyin if empty
			} else {
				pinyin.setText(pinyinStr);
			}
			desc.setText(entry.getDesc());
		}
		return view;
	}

	// clear and reinit search
	@Override
	public void clear() {
		super.clear();
		mSearchIsOver = false;
	}

	public void addLoading() {
		add(mLoading);
	}
	public void removeLoading() {
		remove(mLoading);
	}

	public void add(List<Entry> entries, boolean stillLeft) {
		// Check if any result found
		if (entries.isEmpty() && getCount() == 0) {
			add(mNoResults);
		} else {
			for (Entry entry : entries) {
				add(entry);
			}
		}

		// Determine if search is over or not
		if (stillLeft) { // add loading if still some entries left
			addLoading();
		} else {
			mSearchIsOver = true;
		}
		notifyDataSetChanged();
	}

	public boolean isSearchOver() {
		return mSearchIsOver;
	}

	public void setTextSizesFromParent(Activity parent) {
		String[] listItemSizesStr = parent.getResources().getStringArray(R.array.listItemSizes);
		int length = listItemSizesStr.length;

		mListItemSizes = new float[length];
		for (int i = 0; i < length; i++) {
			mListItemSizes[i] = Float.parseFloat(listItemSizesStr[i]);
		}
	}
}
