package com.nilhcem.frcndict;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public final class ImportDataActivity extends Activity {
	private Button mDownloadButton;
	private Button mExitButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.import_data);
		initButtons();
	}

	private void initButtons() {
		mDownloadButton = (Button) findViewById(R.id.importDwnldBtn);
		mExitButton = (Button) findViewById(R.id.importExitBtn);

		mDownloadButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startDownload();
			}
		});

		mExitButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ImportDataActivity.this.finish();
			}
		});
	}

	private void startDownload() {
	}
}
