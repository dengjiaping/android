package com.oumen.chat;

import android.graphics.Bitmap;

public class ExpressionBean {
	private String name;//图片的名称
	private Bitmap bitmap;//图片
	
	public ExpressionBean() {
		super();
	}
	public ExpressionBean(String name, Bitmap bitmap) {
		super();
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
