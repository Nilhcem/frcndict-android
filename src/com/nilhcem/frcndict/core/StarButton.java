package com.nilhcem.frcndict.core;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nilhcem.frcndict.R;
import com.nilhcem.frcndict.database.DatabaseHelper;

public final class StarButton extends RelativeLayout {
	private int mId;
	private Context mParent;
	private DatabaseHelper mDb;
	private SimpleDateFormat mDateFormat;

	private TextView mStarredText;
	private ImageButton mStarredBtn;
	private View.OnClickListener mOnClickListener;

	public StarButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.core_star_button, this, true);

		mStarredBtn = (ImageButton) findViewById(R.id.starButton);
		mStarredText = (TextView) findViewById(R.id.starText);

		initOnClickListener();
		mStarredBtn.setOnClickListener(mOnClickListener);
		layout.setOnClickListener(mOnClickListener);
	}

	public void init(int id, DatabaseHelper db, Context parent) {
		mId = id;
		mDb = db;
		mParent = parent;
		mDateFormat = new SimpleDateFormat(mParent.getString(R.string.meaning_starred_date_format));
	}

	public void setStarredDate(String starredDate) {
		if (starredDate != null) {
			String parsedDate;
			mStarredBtn.setSelected(true);
			try {
				parsedDate = mDateFormat.format(DatabaseHelper.DATE_FORMAT.parse(starredDate));
			} catch (ParseException e) {
				parsedDate = starredDate;
			}
			mStarredText.setText(String.format(mParent.getString(R.string.meaning_starred_on), parsedDate));
		}
	}

	private void initOnClickListener() {
		mOnClickListener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final Date now;

				if (mStarredBtn.isSelected()){
					now = null;
					mStarredBtn.setSelected(false);
					mStarredText.setText(mParent.getString(R.string.meaning_not_starred));
				} else {
					now = new Date();
					mStarredBtn.setSelected(true);
					mStarredText.setText(String.format(mParent.getString(R.string.meaning_starred_on), mDateFormat.format(now)));
				}

				// Run "starred" database modification on a special thread
				new Thread() {
					@Override
					public void run() {
						super.run();
						mDb.setStarredDate(mId, now);
					}
				}.start();
			}
		};
	}
}
