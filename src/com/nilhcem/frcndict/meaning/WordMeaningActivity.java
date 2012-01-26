package com.nilhcem.frcndict.meaning;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.nilhcem.frcndict.R;
import com.nilhcem.frcndict.core.AbstractDictActivity;
import com.nilhcem.frcndict.database.Tables;
import com.nilhcem.frcndict.utils.ChineseCharsHandler;

public final class WordMeaningActivity extends AbstractDictActivity {
	public static String ID_INTENT = "id";

	private TextView mSimplified;
	private TextView mPinyin;
	private TextView mMeaning;
	private TextView mMeaningTitle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!isFinishing()) {
			setContentView(R.layout.word_meaning);
			initTextViews();
			loadData();
		}
	}

	private void initTextViews() {
		mSimplified = (TextView) findViewById(R.id.wmChinese);
		mPinyin = (TextView) findViewById(R.id.wmPinyin);
		mMeaning = (TextView) findViewById(R.id.wmMeaning);
		mMeaningTitle = (TextView) findViewById(R.id.wmMeaningTitle);
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

				if (pinyin.length() > 0) {
					mPinyin.setText(ChineseCharsHandler.formatPinyin(pinyin, prefs));
				} else {
					mPinyin.setVisibility(View.GONE); // hide pinyin if empty
				}

				mSimplified.setText(Html.fromHtml(ChineseCharsHandler.addColorToHanzi(simplified, pinyin)));
				mMeaning.setText(getFormattedMeaning(desc));
			} else {
				// TODO
			}
			c.close();
		} else {
			// TODO
		}
	}

	private String getFormattedMeaning(String meaning) {
		StringBuilder sb = new StringBuilder();
		String[] meanings = meaning.split("/");

		String space = " ";
		String bullet = getString(R.string.meaning_title_bullet);
		String separator = System.getProperty("line.separator");

		for (String curMeaning : meanings) {
			sb.append(bullet).append(space).append(curMeaning).append(separator);
		}

		// If there are many meanings, put the title in plural form (instead of singular form)
		if (meanings.length > 1) {
			mMeaningTitle.setText(R.string.meaning_title_plural);
		}
		return sb.toString();
	}
}
