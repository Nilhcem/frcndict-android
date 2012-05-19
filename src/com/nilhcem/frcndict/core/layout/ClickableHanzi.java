package com.nilhcem.frcndict.core.layout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.ClipboardManager;
import android.text.Html;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nilhcem.frcndict.R;
import com.nilhcem.frcndict.meaning.WordMeaningActivity;
import com.nilhcem.frcndict.settings.SettingsActivity;
import com.nilhcem.frcndict.utils.ChineseCharsHandler;

@SuppressWarnings("deprecation")
public final class ClickableHanzi extends LinearLayout {
	private static final String END_FONT_TAG = "</font>";

	private static final Integer ACTION_OPEN_CHAR = 0;
	private static final Integer ACTION_COPY_CHAR = 1;
	private static final Integer ACTION_COPY_SIMP = 2;
	private static final Integer ACTION_COPY_TRAD = 3;

	private String mSimplified;
	private String mTraditional;
	private SharedPreferences mPrefs;
	private List<TextView> mTextViews = new ArrayList<TextView>();

	public ClickableHanzi(Context context, AttributeSet attrs) {
		super(context, attrs);

		LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		this.setLayoutParams(params);
		this.setOrientation(LinearLayout.VERTICAL);
	}

	private List<String> splitHanzi(String input) {
		List<String> splitted = new ArrayList<String>();

		if (input.contains(END_FONT_TAG)) { // HTML
			String[] splittedArray = input.split(END_FONT_TAG);
			for (String curStr : splittedArray) {
				splitted.add(curStr + END_FONT_TAG);
			}
		} else {
			String[] splittedArray = input.split("");
			for (String curStr : splittedArray) {
				if (curStr.length() > 0) {
					splitted.add(curStr);
				}
			}
		}
		return splitted;
	}

	private float getHanziSize(SharedPreferences prefs) {
		float hanziSize;
		int arrayIdx = SettingsActivity.getArrayIdxFontSizes(prefs);
		String[] sizes = getResources().getStringArray(R.array.wordMeaningSizes);

		if (mSimplified.length() > 3) {
			hanziSize = Float.parseFloat(sizes[arrayIdx]);
		} else {
			// if Hanzi < 3 characters, increase size
			hanziSize = Float.parseFloat(sizes[3 + arrayIdx]);
		}
		return hanziSize;
	}

	public void setText(String simplified, String traditional, String pinyin, SharedPreferences prefs) {
		String formattedHanzi = ChineseCharsHandler.getInstance().formatHanzi(simplified, traditional, pinyin, prefs);

		mSimplified = simplified;
		mTraditional = traditional;
		mPrefs = prefs;

		float hanziSize = getHanziSize(prefs);
		List<String> splitted = splitHanzi(formattedHanzi);

		int id = 0;
		for (String str : splitted) {
			TextView tv = new TextView(getContext());
			tv.setId(++id);
			tv.setText(Html.fromHtml(str));
			tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, hanziSize);
			tv.setOnClickListener(onClickListener);
			mTextViews.add(tv);
		}
	}

	public void display(Activity activity) {
		displayElements(getMaxWidth(activity));
	}

	private void displayElements(int maxWidth) {
		int widthSoFar = 0;

		LinearLayout newLine = getNewLine();
		for (TextView tv : mTextViews) {
			// Remove views if any
			LinearLayout parent = (LinearLayout) tv.getParent();
			if (parent != null) {
				parent.removeAllViews();
			}

			tv.measure(0, 0);
			widthSoFar += tv.getMeasuredWidth();
			if (widthSoFar >= maxWidth) {
				this.addView(newLine);
				newLine = getNewLine();
				newLine.addView(tv);
				widthSoFar = tv.getMeasuredWidth();
			} else {
				newLine.addView(tv);
			}
		}
		this.addView(newLine);
	}

	private int getMaxWidth(Activity activity) {
		Display display = activity.getWindowManager().getDefaultDisplay();
		return display.getWidth() - 15;
	}

	private LinearLayout getNewLine() {
		LinearLayout newLine = new LinearLayout(getContext());
		newLine.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		newLine.setOrientation(LinearLayout.HORIZONTAL);

		return newLine;
	}

	/**
	 * Display some options:
	 * <ul>
	 * 	<li>See CHAR meaning [only if size > 1 && CHAR is a Chinese character]</li>
	 * 	<li>Copy CHAR [only if size > 1 && CHAR is a Chinese character]</li>
	 * 	<li>Copy Simplified [only if simplified selected in settings]</li>
	 * 	<li>Copy Traditional [only if traditional selected in settings and Trad != Simp]</li>
	 * </ul>
	 */
	private View.OnClickListener onClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			int actionId = 0;
			final Map<Integer, Integer> actionsMap = new HashMap<Integer, Integer>();

			final String selectedChar = Html.fromHtml(((TextView) v).getText().toString()).toString();
			List<CharSequence> options = new ArrayList<CharSequence>();
			if (mSimplified.length() > 1 && ChineseCharsHandler.getInstance().charIsChinese(selectedChar.charAt(0))) {
				options.add(String.format(getContext().getString(R.string.meaning_see_text), selectedChar));
				options.add(String.format(getContext().getString(R.string.meaning_copy_text), selectedChar));
				actionsMap.put(actionId++, ACTION_OPEN_CHAR);
				actionsMap.put(actionId++, ACTION_COPY_CHAR);
			}

			String prefHanzi = mPrefs.getString(SettingsActivity.KEY_CHINESE_CHARS, SettingsActivity.VAL_CHINESE_CHARS_SIMP);
			if (prefHanzi.equals(SettingsActivity.VAL_CHINESE_CHARS_TRAD)) {
				options.add(String.format(getContext().getString(R.string.meaning_copy_text), mTraditional));
				actionsMap.put(actionId++, ACTION_COPY_TRAD);
			} else {
				if ((prefHanzi.equals(SettingsActivity.VAL_CHINESE_CHARS_SIMP)) || mSimplified.equals(mTraditional)) {
					options.add(String.format(getContext().getString(R.string.meaning_copy_text), mSimplified));
					actionsMap.put(actionId++, ACTION_COPY_SIMP);
				} else {
					if (prefHanzi.equals(SettingsActivity.VAL_CHINESE_CHARS_BOTH_ST)) {
						options.add(String.format(getContext().getString(R.string.meaning_copy_text), mSimplified));
						options.add(String.format(getContext().getString(R.string.meaning_copy_text), mTraditional));
						actionsMap.put(actionId++, ACTION_COPY_SIMP);
						actionsMap.put(actionId++, ACTION_COPY_TRAD);
					} else {
						options.add(String.format(getContext().getString(R.string.meaning_copy_text), mTraditional));
						options.add(String.format(getContext().getString(R.string.meaning_copy_text), mSimplified));
						actionsMap.put(actionId++, ACTION_COPY_TRAD);
						actionsMap.put(actionId++, ACTION_COPY_SIMP);
					}
				}
			}
			final CharSequence items[] = (CharSequence[]) options.toArray(new CharSequence[options.size()]);

			AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
			builder.setTitle(R.string.meaning_copy_title);
			builder.setItems(items, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Integer action = actionsMap.get(which);
					if (action.equals(ACTION_OPEN_CHAR)) {
						Intent intent = new Intent(getContext(), WordMeaningActivity.class);
						intent.putExtra(WordMeaningActivity.HANZI_INTENT, selectedChar);
						getContext().startActivity(intent);
					} else { // Copy to clipboard
						String toCopy;
						if (action.equals(ACTION_COPY_SIMP)) {
							toCopy = mSimplified;
						} else if (action.equals(ACTION_COPY_TRAD)) {
							toCopy = mTraditional;
						} else { // Char
							toCopy = selectedChar;
						}

						ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Service.CLIPBOARD_SERVICE);
						clipboard.setText(toCopy);
						Toast.makeText(getContext(), R.string.meaning_copied, Toast.LENGTH_SHORT).show();
					}
				}
			});
			AlertDialog alert = builder.create();
			alert.show();
		}
	};
}
