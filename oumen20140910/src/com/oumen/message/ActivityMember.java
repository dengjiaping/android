package com.oumen.message;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import com.oumen.android.App;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

public class ActivityMember implements Parcelable {
	protected int id;
	protected int activityId;
	protected int uid;
	protected Date time;
	protected String photoUrl;
	protected String nickname;
	protected int gender;
	protected String phone;

	public ActivityMember() {}
	
	public ActivityMember(JSONObject obj) throws JSONException {
		id = Integer.parseInt(obj.getString("bmid"));
		activityId = Integer.parseInt(obj.getString("atid"));
		uid = Integer.parseInt(obj.getString("uid"));
		//TODO 出现过为空的情况
		if (!TextUtils.isEmpty(obj.getString("addtime"))) {
			time = new Date(Long.parseLong(obj.getString("addtime")) * 1000);
		}
		else {
			time = new Date(App.getServerTime());
		} 
		photoUrl = obj.getString("head_photo");
		nickname = obj.getString("nickname");
		//TODO 
		String tmp = obj.getString("sex");
		if (TextUtils.isEmpty(tmp)) {
			gender = App.INT_UNSET;
		}
		else {
			gender = Integer.parseInt(tmp);
		}
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(id);
		dest.writeInt(activityId);
		dest.writeInt(uid);
		dest.writeLong(time.getTime());
		dest.writeString(photoUrl);
		dest.writeString(nickname);
		dest.writeInt(gender);
	}

	public static final Parcelable.Creator<ActivityMember> CREATOR = new Parcelable.Creator<ActivityMember>() {
		public ActivityMember createFromParcel(Parcel in) {
			ActivityMember bean = new ActivityMember();
			bean.id = in.readInt();
			bean.activityId = in.readInt();
			bean.uid = in.readInt();
			bean.time = new Date(in.readLong());
			bean.photoUrl = in.readString();
			bean.nickname = in.readString();
			bean.gender = in.readInt();
			return bean;
		}

		public ActivityMember[] newArray(int size) {
			return new ActivityMember[size];
		}
	};

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getActivityId() {
		return activityId;
	}

	public void setActivityId(int activityId) {
		this.activityId = activityId;
	}

	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public String getPhotoUrl() {
		return photoUrl;
	}
	
	public String getScalePhotoUrl(int maxLength) {
		if (TextUtils.isEmpty(photoUrl))
			return null;

		return photoUrl + "/small?l=" + maxLength;
	}

	public void setPhotoUrl(String photoUrl) {
		this.photoUrl = photoUrl;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public int getGender() {
		return gender;
	}

	public void setGender(int gender) {
		this.gender = gender;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}
	
}
