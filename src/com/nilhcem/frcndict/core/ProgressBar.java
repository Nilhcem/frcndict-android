package com.nilhcem.frcndict.core;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nilhcem.frcndict.R;

public final class ProgressBar extends RelativeLayout {
	private final TextView mTitle;
	private final TextView mPercent;
	private final android.widget.ProgressBar mProgressBar;

	private static final String PERCENT_CHAR = "%";
	private static final Integer MAX_PROGRESS = 100;

	public ProgressBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.core_progress_bar, this, true);

		mTitle = (TextView) findViewById(R.id.progressTitle);
		mPercent = (TextView) findViewById(R.id.progressPercent);
		mProgressBar = (android.widget.ProgressBar) findViewById(R.id.progressBar);
		mProgressBar.setMax(ProgressBar.MAX_PROGRESS);
	}

	public void setTitle(int resId) {
		mTitle.setText(resId);
	}

	public void setProgress(Integer progress) {
		// Do not display 0%
		if (progress != 0) {
			mProgressBar.setProgress(progress);

			if (progress.equals(ProgressBar.MAX_PROGRESS)) {
				mPercent.setText(R.string.import_done);
			} else {
				mPercent.setText(Integer.toString(progress) + ProgressBar.PERCENT_CHAR);
			}
		}
	}
}
