package com.nilhcem.frcndict.core;

import java.io.IOException;
import java.util.Observable;

public abstract class AbstractCancellableObservable extends Observable {
	protected boolean cancel;
	private int prevPercent = 0;

	public AbstractCancellableObservable() {
		cancel = false;
	}

	public void cancel() {
		this.cancel = true;
	}

	public abstract void start() throws IOException;

	protected void updateProgress(int newPercent) {
		if (newPercent != prevPercent) {
			setChanged();
			notifyObservers(Integer.valueOf(newPercent));
			prevPercent = newPercent;
		}
	}
}
