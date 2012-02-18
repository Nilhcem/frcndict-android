package com.nilhcem.frcndict.core.list;

import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.nilhcem.frcndict.R;
import com.nilhcem.frcndict.database.Entry;
import com.nilhcem.frcndict.utils.ChineseCharsHandler;

public final class ListAdapter extends ArrayAdapter<Entry> {
	private boolean searchIsOver; // true if no more result to avoid checking database
	private Entry loading;
	private Entry noResults;
	private LayoutInflater inflater;
	private SharedPreferences prefs;

	public ListAdapter(Context context, int textViewResourceId, LayoutInflater inflater, SharedPreferences prefs) {
		super(context, textViewResourceId);
		this.searchIsOver = false;
		this.inflater = inflater;
		this.loading = new Entry();
		this.noResults = new Entry();
		this.prefs = prefs;
	}

	// TODO: Check if convertView != null ?
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Entry entry = getItem(position);

		int resId;
		if (entry == loading) {
			resId = R.layout.search_dict_loading;
		} else if (entry == noResults) {
			resId = R.layout.search_dict_no_results;
		} else {
			resId = R.layout.search_dict_list_item;
		}

		View view = inflater.inflate(resId, parent, false);

		if (entry == loading || entry == noResults) {
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

			ChineseCharsHandler chineseCharsHandler = ChineseCharsHandler.getInstance();
			view.setId(entry.getId());
			chinese.setText(Html.fromHtml(chineseCharsHandler.formatHanzi(entry.getSimplified(),
					entry.getTraditional(), entry.getPinyin() , prefs)));
			String pinyinStr = chineseCharsHandler.formatPinyin(entry.getPinyin(), prefs);
			if (pinyinStr.length() > 0) {
				pinyin.setText(pinyinStr);
			} else {
				pinyin.setVisibility(View.GONE); // hide pinyin if empty
			}
			desc.setText(entry.getDesc());
		}
		return view;
	}

	// clear and reinit search
	@Override
	public void clear() {
		super.clear();
		searchIsOver = false;
	}

	public void addLoading() {
		add(loading);
	}
	public void removeLoading() {
		this.remove(loading);
	}

	public void add(List<Entry> entries, boolean stillLeft) {
		// Check if any result found
		if (entries.isEmpty() && getCount() == 0) {
			add(noResults);
		} else {
			for (Entry entry : entries) {
				add(entry);
			}
		}

		// Determine if search is over or not
		if (stillLeft) { // add loading if still some entries left
			addLoading();
		} else {
			searchIsOver = true;
		}
		notifyDataSetChanged();
	}

	public boolean searchIsOver() {
		return searchIsOver;
	}
}