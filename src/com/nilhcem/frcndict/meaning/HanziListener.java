package com.nilhcem.frcndict.meaning;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.text.TextUtils;

import com.nilhcem.frcndict.core.Log;
import com.nilhcem.frcndict.utils.FileHandler;

public final class HanziListener implements OnCompletionListener {
	private static final String TAG = "HanziListener";
	private static final String EXTENSION = ".mp3";

	private int mCurIdx;
	private File mVoicesDir;
	private List<File> mFiles;
	private MediaPlayer mMediaPlayer;

	public HanziListener() {
		mMediaPlayer = new MediaPlayer();
		mVoicesDir = FileHandler.getVoicesDir();
	}

	public synchronized void play(String pinyin) {
		mCurIdx = 0;
		playSoundFiles(pinyin);

		if (mFiles.size() > 0) {
			onCompletion(mMediaPlayer);
		}
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		if (mCurIdx < mFiles.size()) {
			try {
				mp.reset();
				mp.setDataSource(mFiles.get(mCurIdx++).getAbsolutePath());
				mp.prepare();
				mp.start();
				mp.setOnCompletionListener(HanziListener.this);
			} catch (IllegalArgumentException e) {
				Log.e(TAG, e);
			} catch (IllegalStateException e) {
				Log.e(TAG, e);
			} catch (IOException e) {
				Log.e(TAG, e);
			}
		}
	}

	private void playSoundFiles(String pinyin) {
		// Replace u: by v
		pinyin = pinyin.replaceAll("u:", "v");

		mFiles = new ArrayList<File>();
		String[] splitted = pinyin.split(" ");

		for (String cur : splitted) {
			File sound = getSoundFromPinyin(cur);
			if (sound != null && sound.isFile()) {
				mFiles.add(sound);
			}
		}
	}

	private File getSoundFromPinyin(String pinyin) {
		File sound = null;

		if (!TextUtils.isEmpty(pinyin)) {
			// Try to get sound from this pinyin
			sound = getFileFromName(pinyin);

			// We don't have this sound
			if (!sound.isFile()) {
				Log.w(TAG, "Sound was not found for: %s", sound.getName());
			}
		}
		return sound;
	}

	private File getFileFromName(String name) {
		return new File(mVoicesDir, String.format(Locale.US, "%s%s", name, EXTENSION));
	}
}
