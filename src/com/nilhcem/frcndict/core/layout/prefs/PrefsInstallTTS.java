package com.nilhcem.frcndict.core.layout.prefs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.preference.Preference;
import android.util.AttributeSet;

import com.nilhcem.frcndict.R;

public class PrefsInstallTTS extends Preference {
	private static final String INTENT_MARKET_URI = "market://details?id=com.languagespace.tts";

	public PrefsInstallTTS(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
	}

	public PrefsInstallTTS(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView();
	}

	private void initView() {
		setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
				builder.setTitle(R.string.install_tts_title);
				builder.setMessage(R.string.install_tts_desc);
				builder.setPositiveButton(R.string.install_tts_btn, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Uri marketUri = Uri.parse(PrefsInstallTTS.INTENT_MARKET_URI);
						Intent marketIntent = new Intent(Intent.ACTION_VIEW, marketUri);
						marketIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
						getContext().startActivity(marketIntent);
						dialog.dismiss();
					}
				});
				AlertDialog alert = builder.create();
				alert.show();
				return false;
			}
		});
	}
}
