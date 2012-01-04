package com.nilhcem.frcndict.search;

import java.util.List;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.nilhcem.frcndict.R;
import com.nilhcem.frcndict.database.Entry;
import com.nilhcem.frcndict.utils.WordsConverter;

/* package-private */
final class SearchAdapter extends ArrayAdapter<Entry> {
	private boolean searchIsOver; // true if no more result to avoid checking database
	private Entry loading;
	private LayoutInflater inflater;

	SearchAdapter(Context context, int textViewResourceId, LayoutInflater inflater) {
		super(context, textViewResourceId);
		this.searchIsOver = false;
		this.inflater = inflater;
	}

	// TODO: Check if convertView != null ?
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Entry entry = getItem(position);

		int resId;
		if (entry == loading) {
			resId = R.layout.search_dict_loading;
		} else {
			resId = R.layout.search_dict_list_item;
		}

		View view = inflater.inflate(resId, parent, false);

		if (entry == loading) {
			view.setId(0);
		} else {
			// Get references
			TextView chinese = (TextView) view.findViewById(R.id.slChinese);
			TextView pinyin = (TextView) view.findViewById(R.id.slPinyin);
			TextView desc = (TextView) view.findViewById(R.id.slDesc);

			view.setId(entry.getId());
			chinese.setText(Html.fromHtml(WordsConverter.addColorToHanzi(entry.getSimplified(), entry.getPinyin())));
			pinyin.setText(WordsConverter.pinyinNbToTones(entry.getPinyin()));
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
		for (Entry entry : entries) {
			add(entry);
		}
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
