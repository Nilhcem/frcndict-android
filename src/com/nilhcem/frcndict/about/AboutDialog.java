package com.nilhcem.frcndict.about;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.webkit.WebView;

import com.nilhcem.frcndict.R;

public final class AboutDialog extends Dialog {
	private static final String ABOUT_URL = "file:///android_asset/about/about.html";

	private Context parentContext;
	private WebView webView;

	public AboutDialog(Context context, int theme) {
		super(context, theme);
		this.parentContext = context;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
		setTitle(R.string.about_title);

		// Create interface
		JavascriptInterface jsInterface = new JavascriptInterface(parentContext, this);

		// Get webview and enable JS
		webView = (WebView) findViewById(R.id.aboutWebView);
		webView.getSettings().setJavaScriptEnabled(true);

		// Add interface
		webView.addJavascriptInterface(jsInterface, "android"); // "android" is the keyword that will be exposed in js

		// Load file
		webView.loadUrl(AboutDialog.ABOUT_URL);
	}
}
