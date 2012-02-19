package com.nilhcem.frcndict.core;

import java.io.IOException;
import java.util.Observable;

public abstract class AbstractCancellableObservable extends Observable {
	protected volatile boolean mCancelled;
	private int mPrevPercent = 0;

	public AbstractCancellableObservable() {
		super();
		mCancelled = false;
	}

	public void cancel() {
		mCancelled = true;
	}

	public abstract void start() throws IOException;

	protected void updateProgress(int newPercent) {
		if (newPercent != mPrevPercent) {
			setChanged();
			notifyObservers(Integer.valueOf(newPercent));
			mPrevPercent = newPercent;
		}
	}
}
