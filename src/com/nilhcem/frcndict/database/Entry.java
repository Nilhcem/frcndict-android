package com.nilhcem.frcndict.database;

public final class Entry {
	private int id;
	private String simplified;
	private String traditional;
	private String pinyin;
	private String desc;

	public Entry() {
		// Do nothing.
	}

	public Entry(int id, String simp, String trad, String piny, String desc) {
		this.id = id;
		this.simplified = simp;
		this.traditional = trad;
		this.pinyin = piny;
		this.desc = desc;
	}

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}

	public String getSimplified() {
		return simplified;
	}
	public void setSimplified(String simplified) {
		this.simplified = simplified;
	}

	public String getTraditional() {
		return traditional;
	}
	public void setTraditional(String traditional) {
		this.traditional = traditional;
	}

	public String getPinyin() {
		return pinyin;
	}
	public void setPinyin(String pinyin) {
		this.pinyin = pinyin;
	}

	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
}
