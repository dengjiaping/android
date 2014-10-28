package com.oumen.activity.detail.comment;

import org.json.JSONObject;

public class Comment {
	public static final int PRISE_TYPE_CHAPING = 0;
	public static final int PRISE_TYPE_HAOPING = 1;

	private int uid;
	private String nickName;
	private String photoUrl;
	private int priseType;
	private String content;
	private long time;

	public Comment() {
	}

	public Comment(JSONObject obj) throws Exception {
		uid = obj.getInt("uid");
		nickName = obj.getString("nickname");
		photoUrl = obj.getString("head_photo");
		priseType = obj.getInt("type");
		
		content = obj.getString("content");
		time = obj.getLong("addtime") * 1000;
		
	}

	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public String getPhotoUrl() {
		return photoUrl;
	}

	public void setPhotoUrl(String photoUrl) {
		this.photoUrl = photoUrl;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public int getPriseType() {
		return priseType;
	}

	public void setPriseType(int haoPing) {
		this.priseType = haoPing;
	}
}
