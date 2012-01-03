package com.nilhcem.frcndict.meaning;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.TextView;

import com.nilhcem.frcndict.R;
import com.nilhcem.frcndict.database.DatabaseHelper;
import com.nilhcem.frcndict.database.Tables;

public final class WordMeaningActivity extends Activity {
	public static String ID_INTENT = "id";

	private DatabaseHelper db = DatabaseHelper.getInstance();
	private TextView mSimplified;
	private TextView mPinyin;
	private TextView mDescription;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.word_meaning);
		initTextViews();
		loadData();
	}

	private void initTextViews() {
		mSimplified = (TextView) findViewById(R.id.wmChinese);
		mPinyin = (TextView) findViewById(R.id.wmPinyin);
		mDescription = (TextView) findViewById(R.id.wmDesc);
	}

	// Load data from database and fill views
	private void loadData() {
		// Get id from indent
		Intent intent = getIntent();
		int id = intent.getIntExtra(WordMeaningActivity.ID_INTENT, 0);

		if (id > 0) {
			Cursor c = db.findById(id);
			if (c.getCount() == 1 && c.moveToFirst()) {
				mSimplified.setText(c.getString(c.getColumnIndex(Tables.ENTRIES_KEY_SIMPLIFIED)));
				mPinyin.setText(c.getString(c.getColumnIndex(Tables.ENTRIES_KEY_PINYIN)));
				mDescription.setText(c.getString(c.getColumnIndex(Tables.ENTRIES_KEY_TRANSLATION))
						.replace("/", System.getProperty("line.separator")));
			} else {
				// TODO
			}
			c.close();
		} else {
			// TODO
		}
	}
}
