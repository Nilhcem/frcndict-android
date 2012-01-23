package com.nilhcem.frcndict.core;

import android.app.Dialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.nilhcem.frcndict.R;
import com.nilhcem.frcndict.about.AboutDialog;

// classes which extends from this will have an option menu
public class AbstractMenuActivity extends AbstractDictActivity {
	private Dialog mAboutDialog = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			if (savedInstanceState.getBoolean("about-displayed", false)) {
				getAboutDialog().show();
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.main_menu_about) {
			getAboutDialog().show();
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if (mAboutDialog != null && mAboutDialog.isShowing()) {
			outState.putBoolean("about-displayed", true);
			mAboutDialog.dismiss();
		}
	}

	// lazy get
	private Dialog getAboutDialog() {
		if (mAboutDialog == null) {
			mAboutDialog = new AboutDialog(this, R.style.sAboutDialog);
		}
		return mAboutDialog;
	}
}
