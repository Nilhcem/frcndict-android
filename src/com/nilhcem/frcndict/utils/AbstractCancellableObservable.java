package com.nilhcem.frcndict.utils;

import java.util.Observable;

public abstract class AbstractCancellableObservable extends Observable {
	protected boolean cancel;

	public AbstractCancellableObservable() {
		cancel = false;
	}

	public void cancel() {
		this.cancel = true;
	}
}
