package com.nilhcem.frcndict.meaning;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

import com.nilhcem.frcndict.R;
import com.nilhcem.frcndict.database.DatabaseHelper;
import com.nilhcem.frcndict.database.Tables;
import com.nilhcem.frcndict.utils.WordsFormater;

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
				String simplified = c.getString(c.getColumnIndex(Tables.ENTRIES_KEY_SIMPLIFIED));
				String pinyin = c.getString(c.getColumnIndex(Tables.ENTRIES_KEY_PINYIN));
				String desc = c.getString(c.getColumnIndex(Tables.ENTRIES_KEY_TRANSLATION));

				mSimplified.setText(Html.fromHtml(WordsFormater.addColorToHanzi(simplified, pinyin)));
				mPinyin.setText(WordsFormater.pinyinNbToTones(pinyin));
				mDescription.setText(desc.replace("/", System.getProperty("line.separator")));
			} else {
				// TODO
			}
			c.close();
		} else {
			// TODO
		}
	}
}
