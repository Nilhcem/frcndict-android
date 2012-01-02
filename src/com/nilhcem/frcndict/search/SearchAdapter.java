package com.nilhcem.frcndict.search;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.nilhcem.frcndict.R;
import com.nilhcem.frcndict.database.Entry;

/* package-private */ final class SearchAdapter extends ArrayAdapter<Entry> {
	private Entry loading;
	private LayoutInflater inflater;

	public SearchAdapter(Context context, int textViewResourceId, LayoutInflater inflater) {
		super(context, textViewResourceId);
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

		if (entry != loading) {
			// Get references
			TextView chinese = (TextView) view.findViewById(R.id.slChinese);
			TextView pinyin = (TextView) view.findViewById(R.id.slPinyin);
			TextView desc = (TextView) view.findViewById(R.id.slDesc);

			chinese.setText(entry.getSimplified());
			pinyin.setText(entry.getPinyin());
			desc.setText(entry.getDesc());
		}
		return view;
	}

	public void addLoading() {
		add(loading);
	}

	public void removeLoading() {
		this.remove(loading);
	}

	public void add(List<Entry> entries, boolean addLoading) {
		for (Entry entry : entries) {
			add(entry);
		}
		if (addLoading) {
			addLoading();
		}
		notifyDataSetChanged();
	}
}
