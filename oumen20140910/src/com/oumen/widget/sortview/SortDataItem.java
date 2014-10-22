package com.oumen.widget.sortview;

public class SortDataItem<D> {
	private D object; // 显示的数据
	private String pinyin;

	public SortDataItem(D object, String pinyin) {
		this.object = object;
		this.pinyin = pinyin;
	}

	public D getObject() {
		return object;
	}

	public void setObject(D object) {
		this.object = object;
	}

	public String getPinyin() {
		return pinyin;
	}

	public void setPinyin(String pinyin) {
		this.pinyin = pinyin;
	}
	
	public char getFirst() {
		return pinyin.charAt(0);
	}
}
