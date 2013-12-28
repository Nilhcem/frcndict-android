package com.nilhcem.frcndict.core;

import java.io.IOException;
import java.util.Observable;

public abstract class AbstractProgressObservable extends Observable {

    private int mPrevPercent = 0;

	public abstract void start() throws IOException;

	protected void updateProgress(int newPercent) {
		if (newPercent != mPrevPercent) {
			setChanged();
			notifyObservers(newPercent);
			mPrevPercent = newPercent;
		}
	}
}
