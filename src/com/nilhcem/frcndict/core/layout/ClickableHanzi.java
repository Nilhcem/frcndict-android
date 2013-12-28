package com.nilhcem.frcndict.core.layout;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.ClipboardManager;
import android.text.Html;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nilhcem.frcndict.R;
import com.nilhcem.frcndict.meaning.HanziListener;
import com.nilhcem.frcndict.meaning.StrokesOrderDisplayer;
import com.nilhcem.frcndict.meaning.WordMeaningActivity;
import com.nilhcem.frcndict.settings.SettingsActivity;
import com.nilhcem.frcndict.utils.ChineseCharsHandler;
import com.nilhcem.frcndict.utils.FileHandler;

@SuppressWarnings("deprecation")
public final class ClickableHanzi extends LinearLayout {

	private static final String START_FONT_TAG = "<font";
	private static final String END_FONT_TAG = "</font>";

	private static final Integer ACTION_OPEN_CHAR = 0;
	private static final Integer ACTION_COPY_CHAR = 1;
	private static final Integer ACTION_COPY_SIMP = 2;
	private static final Integer ACTION_COPY_TRAD = 3;
	private static final Integer ACTION_LISTEN_HANZI = 4;
	private static final Integer ACTION_STROKE_ORDER = 5;

	private String mSimplified;
	private String mPinyin;
	private String mTraditional;
	private final boolean mAddListenFeature;
	private SharedPreferences mPrefs;
	private final List<TextView> mTextViews = new ArrayList<TextView>();

	public ClickableHanzi(Context context, AttributeSet attrs) {
		super(context, attrs);

		LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		this.setLayoutParams(params);
		this.setOrientation(LinearLayout.VERTICAL);
		mAddListenFeature = FileHandler.areVoicesInstalled();
	}

	private List<String> splitHanzi(String input) {
		List<String> splitted = new ArrayList<String>();

		if (input.contains(END_FONT_TAG)) { // HTML
			String[] splittedArray = input.split(END_FONT_TAG);
			int i = 0;
			for (String curStr : splittedArray) {
				// Handle case where String doesn't start by a hanzi, ie: "DNA<font...>hanzi</font>"
				if (i++ == 0) {
					if (!curStr.startsWith(START_FONT_TAG)) {
						String[] splittedFont = curStr.split(START_FONT_TAG);
						if (splittedFont.length > 1) {
							splitted.add(splittedFont[0]);
							curStr = String.format(Locale.US, "%s%s", START_FONT_TAG, splittedFont[1]);
						}
					}
				}
				splitted.add(curStr + END_FONT_TAG);
			}
		} else {
			String[] splittedArray = input.split("");
			for (String curStr : splittedArray) {
				if (!TextUtils.isEmpty(curStr)) {
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
		mPinyin = pinyin;
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
	private final View.OnClickListener onClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			Context c = getContext();
			final List<Integer> actions = new ArrayList<Integer>();
			final String selectedChar = Html.fromHtml(((TextView) v).getText().toString()).toString();
			List<CharSequence> options = new ArrayList<CharSequence>();

			// Check if stroke order file exists
			final File strokeOrderFile = FileHandler.getStrokesFile(selectedChar);
			boolean strokeFileExists = (strokeOrderFile != null && strokeOrderFile.isFile());
			addStrokeOrderOption(actions, options, c, selectedChar, strokeFileExists);

			// Fill options
			if (mSimplified.length() > 1 && ChineseCharsHandler.getInstance().charIsChinese(selectedChar.charAt(0))) {
				addOpenCharOption(actions, options, c, selectedChar);
				addCopyCharOption(actions, options, c, selectedChar, ACTION_COPY_CHAR);
			}

			String prefHanzi = mPrefs.getString(SettingsActivity.KEY_CHINESE_CHARS, SettingsActivity.VAL_CHINESE_CHARS_SIMP);
			if (prefHanzi.equals(SettingsActivity.VAL_CHINESE_CHARS_TRAD)) {
				addCopyCharOption(actions, options, c, mTraditional, ACTION_COPY_TRAD);
				addListenOption(actions, options, c, mTraditional);
			} else {
				if ((prefHanzi.equals(SettingsActivity.VAL_CHINESE_CHARS_SIMP)) || mSimplified.equals(mTraditional)) {
					addCopyCharOption(actions, options, c, mSimplified, ACTION_COPY_SIMP);
					addListenOption(actions, options, c, mSimplified);
				} else {
					if (prefHanzi.equals(SettingsActivity.VAL_CHINESE_CHARS_BOTH_ST)) {
						addCopyCharOption(actions, options, c, mSimplified, ACTION_COPY_SIMP);
						addCopyCharOption(actions, options, c, mTraditional, ACTION_COPY_TRAD);
						addListenOption(actions, options, c, mSimplified);
					} else {
						addCopyCharOption(actions, options, c, mTraditional, ACTION_COPY_TRAD);
						addCopyCharOption(actions, options, c, mSimplified, ACTION_COPY_SIMP);
						addListenOption(actions, options, c, mTraditional);
					}
				}
			}

			final CharSequence items[] = options.toArray(new CharSequence[options.size()]);
			AlertDialog.Builder builder = new AlertDialog.Builder(c);
			builder.setTitle(R.string.meaning_copy_title);
			builder.setItems(items, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Integer action = actions.get(which);
					if (action.equals(ACTION_OPEN_CHAR)) {
						Intent intent = new Intent(getContext(), WordMeaningActivity.class);
						intent.putExtra(WordMeaningActivity.HANZI_INTENT, selectedChar);
						getContext().startActivity(intent);
					} else if (action.equals(ACTION_LISTEN_HANZI)) {
						new HanziListener().play(mPinyin);
					} else if (action.equals(ACTION_STROKE_ORDER)) {
						new StrokesOrderDisplayer().display(getContext(), strokeOrderFile);
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

	private void addOpenCharOption(List<Integer> actions, List<CharSequence> options, Context c, String hanzi) {
		options.add(String.format(Locale.US, c.getString(R.string.meaning_see_text), hanzi));
		actions.add(ACTION_OPEN_CHAR);
	}

	private void addCopyCharOption(List<Integer> actions, List<CharSequence> options, Context c, String hanzi, Integer action) {
		options.add(String.format(Locale.US, c.getString(R.string.meaning_copy_text), hanzi));
		actions.add(action);
	}

	private void addStrokeOrderOption(List<Integer> actions, List<CharSequence> options, Context c, String hanzi, boolean exist) {
		if (exist) {
			options.add(String.format(Locale.US, c.getString(R.string.meaning_see_stroke), hanzi));
			actions.add(ACTION_STROKE_ORDER);
		}
	}

	private void addListenOption(List<Integer> actions, List<CharSequence> options, Context c, String hanzi) {
		if (mAddListenFeature) {
			options.add(String.format(Locale.US, c.getString(R.string.meaning_listen), hanzi));
			actions.add(ACTION_LISTEN_HANZI);
		}
	}
}
