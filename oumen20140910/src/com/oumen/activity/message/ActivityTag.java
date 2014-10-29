package com.oumen.activity.message;

import org.json.JSONObject;

import android.graphics.Bitmap;

/**
 * 活动标签
 * 
 * @author oumen-xin.zhang
 *
 */
public class ActivityTag {
	int tagId;
	String tagUrl;
	Bitmap tagBitmap;
	String tagName;

	public ActivityTag() {

	}

	public ActivityTag(JSONObject obj) throws Exception {
		tagId = obj.getInt("id");
		tagUrl = obj.getString("pic");
		tagName = obj.getString("tag");
	}

	public int getTagId() {
		return tagId;
	}

	public void setTagId(int tagId) {
		this.tagId = tagId;
	}

	public String getTagUrl() {
		return tagUrl;
	}

	public void setTagUrl(String tagUrl) {
		this.tagUrl = tagUrl;
	}

	public Bitmap getTagBitmap() {
		return tagBitmap;
	}

	public void setTagBitmap(Bitmap tagBitmap) {
		this.tagBitmap = tagBitmap;
	}

	public String getTagName() {
		return tagName;
	}

	public void setTagName(String name) {
		this.tagName = name;
	}

}
