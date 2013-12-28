package com.nilhcem.frcndict.about;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.webkit.WebView;

import com.nilhcem.frcndict.R;
import com.nilhcem.frcndict.core.Log;

public final class AboutDialog extends Dialog {

	private static final String ABOUT_ASSET_DIR = "about";
	private static final String ABOUT_ASSET_FILE = "about";
	private static final String ABOUT_ASSET_FILE_SEPARATOR = "-";
	private static final String ABOUT_URL_EXTENSION = ".html";
	private static final String ABOUT_URL = "file:///android_asset/" + AboutDialog.ABOUT_ASSET_DIR + "/";
	private final Context mParentContext;

	public AboutDialog(Context context, int theme) {
		super(context, theme);
		mParentContext = context;
	}

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
		setTitle(R.string.about_title);

		// Create interface
		JavascriptInterface jsInterface = new JavascriptInterface(mParentContext, this);

		// Get webview and enable JS
		WebView webView = (WebView) findViewById(R.id.aboutWebView);
		webView.getSettings().setJavaScriptEnabled(true);

		// Add interface
		webView.addJavascriptInterface(jsInterface, "android"); // "android" is the keyword that will be exposed in js

		// Load file
		webView.loadUrl(getLocaleUrl());
	}

	private String getLocaleUrl() {
		boolean found = false;
		String localeUrl = null;

		try {
			Locale locale = Locale.getDefault();
			List<String> assets = Arrays.asList(mParentContext.getResources().getAssets().list(AboutDialog.ABOUT_ASSET_DIR));

			localeUrl = String.format(Locale.US, "%s%s%s%s", AboutDialog.ABOUT_ASSET_FILE,
					AboutDialog.ABOUT_ASSET_FILE_SEPARATOR, locale.getLanguage(),
					AboutDialog.ABOUT_URL_EXTENSION);
			if (assets.contains(localeUrl)) {
				found = true;
			}
			if (locale.getCountry().equals("CN")) {
				localeUrl = localeUrl.replace("zh", "zh-simplified");
			}
		} catch (IOException e) {
			Log.e(AboutDialog.class.getSimpleName(), e, "Can't find about asset");
			found = false;
		}

		if (!found) {
			localeUrl = String.format(Locale.US, "%s%s", AboutDialog.ABOUT_ASSET_FILE, AboutDialog.ABOUT_URL_EXTENSION);
		}
		return AboutDialog.ABOUT_URL + localeUrl;
	}
}
