package com.nilhcem.frcndict.meaning;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.text.Html;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.nilhcem.frcndict.R;
import com.nilhcem.frcndict.core.AbstractDictActivity;
import com.nilhcem.frcndict.core.layout.StarButton;
import com.nilhcem.frcndict.database.Tables;
import com.nilhcem.frcndict.settings.SettingsActivity;
import com.nilhcem.frcndict.utils.ChineseCharsHandler;

public final class WordMeaningActivity extends AbstractDictActivity {
	public static final String ID_INTENT = "id";

	private int mId;
	private TextView mHanzi;
	private TextView mPinyin;
	private TextView mMeaning;
	private TextView mMeaningTitle;
	private StarButton mStarButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!isFinishing()) {
			setContentView(R.layout.word_meaning);
			initId();
			initTextViews();
			initStarButton();
			loadData();
			initFontSizes();
			initCopyDialog();
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

		if (item.getItemId() == R.id.starred_menu_search) {
			finish();
			stopProcessing = true;
		} else {
			stopProcessing = super.onOptionsItemSelected(item);
		}
		return stopProcessing;
	}

	// Get id from indent
	private void initId() {
		Intent intent = getIntent();
		mId = intent.getIntExtra(WordMeaningActivity.ID_INTENT, 0);
	}

	private void initTextViews() {
		mHanzi = (TextView) findViewById(R.id.wmChinese);
		mPinyin = (TextView) findViewById(R.id.wmPinyin);
		mMeaning = (TextView) findViewById(R.id.wmMeaning);
		mMeaningTitle = (TextView) findViewById(R.id.wmMeaningTitle);
	}

	private void initStarButton() {
		mStarButton = (StarButton) findViewById(R.id.wmStarButton);
		mStarButton.init(mId, mDb, this);
	}

	// Load data from database and fill views
	private void loadData() {
		if (mId > 0) {
			mDb.open();
			Cursor c = mDb.findById(mId);
			if (c.getCount() == 1 && c.moveToFirst()) {
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

				mHanzi.setText(Html.fromHtml(chineseCharsHandler.formatHanzi(simplified, traditional, pinyin, mPrefs)));
				mMeaning.setText(getFormattedMeaning(desc));
				mStarButton.setStarredDate(starredDate);
			}
			c.close();
			mDb.close();
		}
	}

	private void initFontSizes() {
		int arrayIdx = SettingsActivity.getArrayIdxFontSizes(mPrefs);
		String[] sizes = getResources().getStringArray(R.array.wordMeaningSizes);

		float hanziSize;
		if (mHanzi.getText().length() > 3) {
			hanziSize = Float.parseFloat(sizes[arrayIdx]);
		} else {
			// if Hanzi < 3 characters, increase size
			hanziSize = Float.parseFloat(sizes[3 + arrayIdx]);
		}
		float otherSize = Float.parseFloat(sizes[6 + arrayIdx]);

		mHanzi.setTextSize(TypedValue.COMPLEX_UNIT_SP, hanziSize);
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

	private void initCopyDialog() {
		mHanzi.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final String text = android.text.Html.fromHtml(mHanzi.getText().toString()).toString();
				final CharSequence[] items = {String.format(getString(R.string.meaning_copy_text), text)};
				AlertDialog.Builder builder = new AlertDialog.Builder(WordMeaningActivity.this);

				builder.setTitle(R.string.meaning_copy_title);
				builder.setItems(items, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// Copy to clipboard
						ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
						clipboard.setText(text);
						Toast.makeText(WordMeaningActivity.this, R.string.meaning_copied, Toast.LENGTH_SHORT).show();
					}
				});
				AlertDialog alert = builder.create();
				alert.show();
			}
		});
	}
}
