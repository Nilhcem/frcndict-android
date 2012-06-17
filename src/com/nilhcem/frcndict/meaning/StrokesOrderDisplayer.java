package com.nilhcem.frcndict.meaning;

import java.io.File;

import com.nilhcem.frcndict.utils.FileHandler;

import android.app.AlertDialog;
import android.content.Context;
import android.view.Gravity;
import android.webkit.WebView;
import android.widget.LinearLayout;

public final class StrokesOrderDisplayer {
	private static final int WEBVIEW_SIZE = 256; // dp
	private static final String WEBVIEW_MIME = "text/html";
	private static final String WEBVIEW_ENCODING = "UTF-8";
	private static final String STROKES_DIR_PATH;

	static {
		File strokeDir = FileHandler.getStrokesDir();
		if (strokeDir == null) {
			STROKES_DIR_PATH = null;
		} else {
			STROKES_DIR_PATH = strokeDir.getAbsolutePath();
		}
	}

	public StrokesOrderDisplayer() {
	}

	public void display(Context context, File strokeOrderFile) {
		if (context != null && strokeOrderFile.isFile()) {
			WebView wv = createWebView(context, strokeOrderFile);
			AlertDialog dialog = new AlertDialog.Builder(context)
				.setView(wv)
				.create();
			dialog.show();
		}
	}

	private WebView createWebView(Context context, File strokeOrderFile) {
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(WEBVIEW_SIZE, WEBVIEW_SIZE);
		params.gravity = Gravity.CENTER;

		WebView webView = new WebView(context);
		webView.setLayoutParams(params);
		webView.getSettings().setAllowFileAccess(true);
		webView.getSettings().setJavaScriptEnabled(false);
		webView.getSettings().setBuiltInZoomControls(false);
		webView.loadDataWithBaseURL("", getHtml(strokeOrderFile), WEBVIEW_MIME, WEBVIEW_ENCODING, "");
		return webView;
	}

	private String getHtml(File strokeOrderFile) {
		StringBuilder sb = new StringBuilder();
		sb.append("<html>")
			.append("<body>")
			.append("<div align=\"center\" style=\"background-image:url('file://")
			.append(STROKES_DIR_PATH)
			.append("/background.png'); background-position: center; background-repeat: no-repeat;\">")
			.append("<img src=\"file://")
			.append(strokeOrderFile.getAbsolutePath())
			.append("\" />")
			.append("</div>")
			.append("</body>")
			.append("</html>");
		return sb.toString();
	}
}
