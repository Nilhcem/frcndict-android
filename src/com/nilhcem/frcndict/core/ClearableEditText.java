package com.nilhcem.frcndict.core;

import java.util.Observable;
import java.util.Observer;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.nilhcem.frcndict.R;

/**
 * @see http://arunbadole1209.wordpress.com/2011/12/16/how-to-create-edittext-with-crossx-button-at-end-of-it/
 */
public final class ClearableEditText extends RelativeLayout {
	private EditText mEditText;
	private Button mBtnClear;
	private ClearableTextObservable mObservable;

	public ClearableEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.clearable_edit_text, this, true);

		mEditText = (EditText) findViewById(R.id.clearable_edit);
		mBtnClear = (Button) findViewById(R.id.clearable_button_clear);
		mBtnClear.setVisibility(View.INVISIBLE);
		mObservable = new ClearableTextObservable();
		clearText();
		showHideClearButton();
	}

	public final class ClearableTextObservable extends Observable {
		@Override
		public void notifyObservers() {
			setChanged();
			super.notifyObservers();
		}
	}

	public EditText getEditText() {
		return mEditText;
	}

	private void clearText() {
		mBtnClear.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mEditText.setText("");
				mObservable.notifyObservers();
			}
		});
	}

	private void showHideClearButton() {
		mEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (s.length() > 0) {
					mBtnClear.setVisibility(View.VISIBLE);
				} else {
					mBtnClear.setVisibility(View.INVISIBLE);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});
	}

	public void addObserver(Observer observer) {
		mObservable.addObserver(observer);
	}
}
