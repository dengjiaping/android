package com.oumen.activity.detail.comment;

import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

public class Comment implements Parcelable {
	public static final int PRISE_TYPE_CHAPING = 0;
	public static final int PRISE_TYPE_HAOPING = 1;

	int uid;
	String nickName;
	String photoUrl;
	int priseType;
	String content;
	long time;

	public Comment() {
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(uid);
		dest.writeString(nickName);
		dest.writeString(photoUrl);
		dest.writeInt(priseType);
		dest.writeString(content);
		dest.writeLong(time);
	}
	
	public static final Parcelable.Creator<Comment> CREATOR = new Creator<Comment>() {
		
		@Override
		public Comment[] newArray(int size) {
			return new Comment[size];
		}
		
		@Override
		public Comment createFromParcel(Parcel in) {
			Comment bean = new Comment();
			bean.uid = in.readInt();
			bean.nickName = in.readString();
			bean.photoUrl = in.readString();
			bean.priseType = in.readInt();
			bean.content = in.readString();
			bean.time = in.readLong();
			return bean;
		}
	};

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
