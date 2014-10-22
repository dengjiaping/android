package com.oumen.message;

import android.graphics.Bitmap;

public class BiaoQing {
	protected String name;//图片的名称
	protected Bitmap bitmap;//图片

	public BiaoQing() {
	}

	public BiaoQing(String name, Bitmap bitmap) {
		this.name = name;
		this.bitmap = bitmap;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Bitmap getBitmap() {
		return bitmap;
	}

	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}
}
