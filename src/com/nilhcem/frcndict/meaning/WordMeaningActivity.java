package com.nilhcem.frcndict.meaning;

import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.nilhcem.frcndict.R;
import com.nilhcem.frcndict.core.AbstractDictActivity;
import com.nilhcem.frcndict.core.layout.ClickableHanzi;
import com.nilhcem.frcndict.core.layout.StarButton;
import com.nilhcem.frcndict.database.Tables;
import com.nilhcem.frcndict.settings.SettingsActivity;
import com.nilhcem.frcndict.utils.ChineseCharsHandler;

public final class WordMeaningActivity extends AbstractDictActivity {
	public static final String ID_INTENT = "id";
	public static final String HANZI_INTENT = "hanzi";

	private int mId;
	private ClickableHanzi mHanzi;
	private TextView mPinyin;
	private TextView mMeaning;
	private TextView mMeaningTitle;
	private StarButton mStarButton;

	private View mLoadingLayout;
	private View mMeaningLayout;
	private View mNoResultLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!isFinishing()) {
			setContentView(R.layout.word_meaning);
			new LoadingAsync().execute();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.word_meaning_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean stopProcessing;

		if (item.getItemId() == R.id.starred_menu_back) {
			finish();
			stopProcessing = true;
		} else {
			stopProcessing = super.onOptionsItemSelected(item);
		}
		return stopProcessing;
	}

	// Get id from intent
	private void initId() {
		Intent intent = getIntent();
		mId = intent.getIntExtra(WordMeaningActivity.ID_INTENT, 0);

		// Check if we sent an hanzi instead of an ID
		if (mId == 0) {
			String hanzi = intent.getStringExtra(WordMeaningActivity.HANZI_INTENT);
			if (hanzi != null) {
				mDb.open();
				mId = mDb.getIdByHanzi(hanzi);
				mDb.close();
			}
		}
	}

	private void initLayouts() {
		mLoadingLayout = findViewById(R.id.wmLoadingLayout);
		mMeaningLayout = findViewById(R.id.wmMeaningLayout);
		mNoResultLayout = findViewById(R.id.wmNoResultLayout);
	}

	private void initTextViews() {
		mHanzi = (ClickableHanzi) findViewById(R.id.wmChinese);
		mPinyin = (TextView) findViewById(R.id.wmPinyin);
		mMeaning = (TextView) findViewById(R.id.wmMeaning);
		mMeaningTitle = (TextView) findViewById(R.id.wmMeaningTitle);
	}

	private void initStarButton() {
		mStarButton = (StarButton) findViewById(R.id.wmStarButton);
		mStarButton.init(mId, mDb, this);
	}

	private void fillViews(Cursor c) {
		if (c != null && c.getCount() == 1 && c.moveToFirst()) {
			String simplified = c.getString(c.getColumnIndex(Tables.ENTRIES_KEY_SIMPLIFIED));
			String traditional = c.getString(c.getColumnIndex(Tables.ENTRIES_KEY_TRADITIONAL));
			String pinyin = c.getString(c.getColumnIndex(Tables.ENTRIES_KEY_PINYIN));
			String desc = c.getString(c.getColumnIndex(Tables.ENTRIES_KEY_TRANSLATION));
			String starredDate = c.getString(c.getColumnIndex(Tables.ENTRIES_KEY_STARRED_DATE));

			ChineseCharsHandler chineseCharsHandler = ChineseCharsHandler.getInstance();
			if (pinyin.length() > 0) {
				mPinyin.setText(chineseCharsHandler.formatPinyin(pinyin, mPrefs));
			} else {
				mPinyin.setVisibility(View.GONE); // hide pinyin if empty
			}

			mHanzi.setText(simplified, traditional, pinyin, mPrefs);
			mHanzi.display(this);
			mMeaning.setText(getFormattedMeaning(desc));
			mStarButton.setStarredDate(starredDate);
		}
	}

	private void initFontSizes() {
		int arrayIdx = SettingsActivity.getArrayIdxFontSizes(mPrefs);
		String[] sizes = getResources().getStringArray(R.array.wordMeaningSizes);
		float otherSize = Float.parseFloat(sizes[6 + arrayIdx]);

		mPinyin.setTextSize(TypedValue.COMPLEX_UNIT_SP, otherSize);
		mMeaning.setTextSize(TypedValue.COMPLEX_UNIT_SP, otherSize);
		mMeaningTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, otherSize);
		mStarButton.getStarredText().setTextSize(TypedValue.COMPLEX_UNIT_SP, otherSize);
	}

	private String getFormattedMeaning(String meaning) {
		StringBuilder sb = new StringBuilder();
		String[] meanings = meaning.split("/");

		String space = " ";
		String bullet = getString(R.string.meaning_title_bullet);
		String separator = System.getProperty("line.separator");
		boolean addSeparator = false;

		for (String curMeaning : meanings) {
			if (addSeparator) {
				sb.append(separator);
			} else {
				addSeparator = true;
			}
			sb.append(bullet).append(space).append(curMeaning);
		}

		// If there are many meanings, put the title in plural form (instead of singular form)
		if (meanings.length > 1) {
			mMeaningTitle.setText(R.string.meaning_title_plural);
		}
		return sb.toString();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mHanzi.display(this);
	}

	private final class LoadingAsync extends AsyncTask<Void, Void, Cursor> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			initLayouts();
			displayLayout(mLoadingLayout);
			mDb.open();
		}

		@Override
		protected Cursor doInBackground(Void... params) {
			Cursor c = null;

			initId();
			initTextViews();
			initStarButton();

			if (mId > 0) {
				c = mDb.findById(mId);
			}
			return c;
		}

		@Override
		protected void onPostExecute(Cursor result) {
			super.onPostExecute(result);
			if (result != null) {
				fillViews(result);
				initFontSizes();
				result.close();
			}
			mDb.close();
			if (result == null) {
				displayLayout(mNoResultLayout);
			} else {
				displayLayout(mMeaningLayout);
			}
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			displayLayout(mNoResultLayout);
		}

		private void displayLayout(View layout) {
			mLoadingLayout.setVisibility(View.GONE);
			mMeaningLayout.setVisibility(View.GONE);
			mNoResultLayout.setVisibility(View.GONE);
			layout.setVisibility(View.VISIBLE);
		}
	}
}
