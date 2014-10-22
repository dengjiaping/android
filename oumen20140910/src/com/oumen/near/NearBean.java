package com.oumen.near;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

public class NearBean implements Parcelable {

	private String vipExpried;
	private String vipCircleExpired;
	private int uid;
	private String photoUrl;
	private String manifesto;
	private int babyType;
	private String gravidity;
	private String birthday;
	private String gender;
	private String address;
	private String description;
	private String username;
	// 新添加的背景图片
	private String backgroundUrl;

	private String diss;
	private String push;
	private String userType;
	private String userGender;

	public NearBean(JSONObject resobj) throws JSONException {
		vipExpried = resobj.getString("vip_expried");
		vipCircleExpired = resobj.getString("vip_circle_expired");
		uid = Integer.parseInt(resobj.getString("user_id"));
		photoUrl = resobj.getString("head_photo");
		manifesto = resobj.getString("manifesto");
		babyType = Integer.parseInt(resobj.getString("babytype"));
		gravidity = resobj.getString("gravidity");
		birthday = resobj.getString("birthday");
		gender = resobj.getString("sex");
		address = resobj.getString("address");
		// result.setDis(getString(resobj.getString("dis")));
		username = resobj.getString("username");
		diss = resobj.getString("diss");
		userGender = resobj.getString("user_sex");
	}
	
	NearBean(Parcel in) {
		vipExpried = in.readString();
		vipCircleExpired = in.readString();
		uid = in.readInt();
		photoUrl = in.readString();
		manifesto = in.readString();
		babyType = in.readInt();
		gravidity = in.readString();
		birthday = in.readString();
		gender = in.readString();
		address = in.readString();
		username = in.readString();
		diss = in.readString();
		userGender = in.readString();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(vipExpried);
		dest.writeString(vipCircleExpired);
		dest.writeInt(uid);
		dest.writeString(photoUrl);
		dest.writeString(manifesto);
		dest.writeInt(babyType);
		dest.writeString(gravidity);
		dest.writeString(birthday);
		dest.writeString(gender);
		dest.writeString(address);
		dest.writeString(username);
		dest.writeString(diss);
		dest.writeString(userGender);
	}

	public static final Parcelable.Creator<NearBean> CREATOR = new Parcelable.Creator<NearBean>() {
		public NearBean createFromParcel(Parcel in) {
			return new NearBean(in);
		}

		public NearBean[] newArray(int size) {
			return new NearBean[size];
		}
	};

	public String getUsersex() {
		return userGender;
	}

	public void setUsersex(String usersex) {
		this.userGender = usersex;
	}
	
	public boolean hasBackground() {
		return !TextUtils.isEmpty(backgroundUrl);
	}
	
	public String getBackgroundSourceUrl() {
		return backgroundUrl;
	}

	public String getBackgroundUrl(int maxLength) {
		if (TextUtils.isEmpty(backgroundUrl))
			return null;
		
		return photoUrl + "/small?l=" + maxLength;
	}

	public void setBackground(String background) {
		this.backgroundUrl = background;
	}

	public NearBean() {
		super();
	}

	public String getVip_expried() {
		return vipExpried;
	}

	public String getDiss() {
		return diss;
	}

	public void setDiss(String diss) {
		this.diss = diss;
	}

	public String getIstui() {
		return push;
	}

	public void setIstui(String istui) {
		this.push = istui;
	}

	public String getUsertype() {
		return userType;
	}

	public void setUsertype(String usertype) {
		this.userType = usertype;
	}

	public void setVip_expried(String vip_expried) {
		this.vipExpried = vip_expried;
	}

	public String getVip_circle_expired() {
		return vipCircleExpired;
	}

	public void setVip_circle_expired(String vip_circle_expired) {
		this.vipCircleExpired = vip_circle_expired;
	}

	public int getUser_id() {
		return uid;
	}

	public void setUser_id(int user_id) {
		this.uid = user_id;
	}
	
	public boolean hasPhoto() {
		return !TextUtils.isEmpty(photoUrl);
	}
	
	public String getPhotoSourceUrl() {
		return photoUrl;
	}

	public String getPhotoUrl(int maxLength) {
		if (TextUtils.isEmpty(photoUrl))
			return null;
		
		return photoUrl + "/small?l=" + maxLength;
	}

	public void setPhotoUrl(String photoUrl) {
		this.photoUrl = photoUrl;
	}

	public String getManifesto() {
		return manifesto;
	}

	public void setManifesto(String manifesto) {
		this.manifesto = manifesto;
	}

	public int getBabytype() {
		return babyType;
	}

	public void setBabytype(int babytype) {
		this.babyType = babytype;
	}

	public String getGravidity() {
		return gravidity;
	}

	public void setGravidity(String gravidity) {
		this.gravidity = gravidity;
	}

	public String getBirthday() {
		return birthday;
	}

	public void setBirthday(String birthday) {
		this.birthday = birthday;
	}

	public String getSex() {
		return gender;
	}

	public void setSex(String sex) {
		this.gender = sex;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getDis() {
		return description;
	}

	public void setDis(String dis) {
		this.description = dis;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	@Override
	public String toString() {
		return "NearByResult [vip_expried=" + vipExpried + ", vip_circle_expired=" + vipCircleExpired + ", user_id=" + uid + ", head_photo=" + photoUrl + ", manifesto=" + manifesto + ", babytype=" + babyType + ", address=" + address + ", username=" + username + ", diss="
				+ diss + ", istui=" + push + ", usertype=" + userType + "]";
	}
}
