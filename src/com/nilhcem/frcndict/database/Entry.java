package com.nilhcem.frcndict.database;

public final class Entry {
	private int mId;
	private String mSimplified;
	private String mTraditional;
	private String mPinyin;
	private String mDesc;

	public Entry() {
		// Do nothing.
	}

	public Entry(int id, String simp, String trad, String piny, String desc) {
		mId = id;
		mSimplified = simp;
		mTraditional = trad;
		mPinyin = piny;
		mDesc = desc;
	}

	public int getId() {
		return mId;
	}
	public void setId(int id) {
		mId = id;
	}

	public String getSimplified() {
		return mSimplified;
	}
	public void setSimplified(String simplified) {
		mSimplified = simplified;
	}

	public String getTraditional() {
		return mTraditional;
	}
	public void setTraditional(String traditional) {
		mTraditional = traditional;
	}

	public String getPinyin() {
		return mPinyin;
	}
	public void setPinyin(String pinyin) {
		mPinyin = pinyin;
	}

	public String getDesc() {
		return mDesc;
	}
	public void setDesc(String desc) {
		mDesc = desc;
	}
}
