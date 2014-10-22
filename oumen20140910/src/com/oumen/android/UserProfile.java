package com.oumen.android;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

import com.oumen.android.util.Constants;

public class UserProfile implements Serializable {
	private static final long serialVersionUID = -5904188331971202954L;

	/**
	 * 怀孕
	 */
	public static final int BABY_TYPE_HUAI_YUN = 0;

	/**
	 * 已出生
	 */
	public static final int BABY_TYPE_CHU_SHENG = 1;

	/**
	 * 备孕
	 */
	public static final int BABY_TYPE_BEI_YUN = 2;

	/**
	 * 其他
	 */
	public static final int BABY_TYPE_QI_TA = 3;

	public static final String FIELD_KEY_USER_GENDER = "user_gender";
	public static final String FIELD_KEY_BABY_GENDER = "baby_gender";
	public static final String FIELD_KEY_BABY_TYPE = "baby_type";
	public static final String FIELD_KEY_OUMEN_ID = "oumen_id";
	public static final String FIELD_KEY_NICKNAME = "nick";
	public static final String FIELD_KEY_ADDRESS = "address";
	public static final String FIELD_KEY_MANIFESTO = "manifesto";
	public static final String FIELD_KEY_GRAVIDITY = "gravidity";
	public static final String FIELD_KEY_GRAVIDITY_TIME = "gravidity_time";
	public static final String FIELD_KEY_BIRTHDAY = "birthday";
	public static final String FIELD_KEY_BIRTHDAY_TIME = "birthday_time";

	protected int uid;// 用户的id
	protected String nickname = "游客";// 昵称
	protected String photo;// 头像

	protected String phoneNumber;// 电话
	protected String phoneType;
	protected String phoneToken;

	protected String oumenId;// 偶们账号
	protected String manifesto;// 签名
	protected int babyType = Constants.NULL_INT;
	protected String gravidity;// 怀孕状态
	protected String gravidityTime;
	protected String birthday;// 出生状态
	protected String birthdayTime;
	protected int babyGender = Constants.NULL_INT;// 性别
	protected int userGender = Constants.NULL_INT;// 用户性别
	protected String address;// 地址
	protected float latitude;// 经度
	protected float longitude;// 纬度
	protected String recentLogin;

	// 新浪
	protected boolean sinaBind;// 新浪微博是否绑定
	// 腾讯
	protected boolean qqBind;// 腾讯是否绑定

	// vip是否绑定
	protected boolean vipBind;
	protected String familyMessage;
	protected String type;

	protected boolean thirdpart;
	protected String thirdpartUid;
	protected String email;
	protected String mailMessage;

	protected String activity;

	protected boolean hasPhoneNum;

	@Override
	public boolean equals(Object o) {
		if (o instanceof UserProfile) {
			UserProfile p = (UserProfile) o;

			if (uid != p.uid)
				return false;
			if (!equalsString(nickname, p.nickname))
				return false;
			if (!equalsString(photo, p.photo))
				return false;
			if (!equalsString(phoneNumber, p.phoneNumber))
				return false;
			if (!equalsString(phoneType, p.phoneType))
				return false;
			if (!equalsString(phoneToken, p.phoneToken))
				return false;
			if (!equalsString(oumenId, p.oumenId))
				return false;
			if (!equalsString(manifesto, p.manifesto))
				return false;
			if (babyType != p.babyType)
				return false;
			if (!equalsString(gravidity, p.gravidity))
				return false;
			if (!equalsString(gravidityTime, p.gravidityTime))
				return false;
			if (!equalsString(birthday, p.birthday))
				return false;
			if (!equalsString(birthdayTime, p.birthdayTime))
				return false;
			if (babyGender != p.babyGender)
				return false;
			if (userGender != p.userGender)
				return false;
			if (!equalsString(address, p.address))
				return false;
			if (latitude != p.latitude)
				return false;
			if (longitude != p.longitude)
				return false;
			if (!equalsString(recentLogin, p.recentLogin))
				return false;

			if (sinaBind != p.sinaBind)
				return false;

			if (qqBind != p.qqBind)
				return false;

			if (vipBind != p.vipBind)
				return false;

			if (!equalsString(familyMessage, p.familyMessage))
				return false;
			if (!equalsString(type, p.type))
				return false;
			if (!equalsString(email, p.email))
				return false;
			if (!equalsString(mailMessage, p.mailMessage))
				return false;
			if (thirdpart != p.thirdpart)
				return false;
			if (!equalsString(thirdpartUid, p.thirdpartUid))
				return false;
			if (!equalsString(activity, p.activity))
				return false;
			if (hasPhoneNum != p.hasPhoneNum)
				return false;
			return true;
		}
		else {
			return false;
		}
	}

	private boolean equalsString(String value1, String value2) {
		if (value1 == null && value2 == null)
			return true;
		if (value1 != null && value2 != null)
			return value1.equals(value2);
		return false;
	}

	public void copyFrom(UserProfile profile) {
		uid = profile.uid;
		nickname = profile.nickname;
		photo = profile.photo;

		phoneNumber = profile.phoneNumber;
		phoneType = profile.phoneType;
		phoneToken = profile.phoneToken;

		oumenId = profile.oumenId;
		manifesto = profile.manifesto;
		babyType = profile.babyType;
		gravidity = profile.gravidity;
		gravidityTime = profile.gravidityTime;
		birthday = profile.birthday;
		birthdayTime = profile.birthdayTime;
		babyGender = profile.babyGender;
		userGender = profile.userGender;
		address = profile.address;
		latitude = profile.latitude;
		longitude = profile.longitude;
		recentLogin = profile.recentLogin;

		sinaBind = profile.sinaBind;

		qqBind = profile.qqBind;

		vipBind = profile.vipBind;
		familyMessage = profile.familyMessage;
		type = profile.type;

		email = profile.email;
		mailMessage = profile.mailMessage;
		thirdpart = profile.thirdpart;
		thirdpartUid = profile.thirdpartUid;

		activity = profile.activity;
		hasPhoneNum = profile.hasPhoneNum;
	}

	public UserProfile() {
	}

	public UserProfile(JSONObject obj) throws JSONException {
		this.sinaBind = obj.has("weibo_type") ? obj.getString("weibo_type").equals("1") : false;
		this.qqBind = obj.has("qq_type") ? obj.getString("qq_type").equals("1") : false;
		this.vipBind = obj.has("vip_type") ? obj.getString("vip_type").equals("1") : false;
//		this.familyMessage = obj.getString("family_message");
		this.type = obj.getString("type");
		if (!TextUtils.isEmpty(obj.getString("other_login")))
			this.thirdpart = obj.getString("other_login").equals("1");
		if (!TextUtils.isEmpty(obj.getString("other_uid")))
			this.thirdpartUid = obj.getString("other_uid");
		this.email = obj.getString("email");
		this.mailMessage = obj.getString("mail_message");
		this.activity = obj.getString("activity");

		JSONObject u = obj.getJSONObject("user_message");
		this.uid = Integer.valueOf(u.getString("user_id"));
		this.nickname = u.getString("nickname");
		this.photo = u.getString("head_photo");
		this.phoneNumber = u.getString("phonenum");
		this.phoneType = u.getString("phone_type");
		this.phoneToken = u.getString("phone_token");
		this.oumenId = u.getString("omnumber");
		this.manifesto = u.getString("manifesto");
		if (!TextUtils.isEmpty(u.getString("babytype"))) {
			this.babyType = Integer.parseInt(u.getString("babytype"));
		}
		else {
			this.babyType = BABY_TYPE_QI_TA;
		}

		if (babyType == BABY_TYPE_HUAI_YUN) {// 怀孕
			this.gravidityTime = u.getString("gravidity_time");
			this.gravidity = u.getString("gravidity");
			this.babyGender = Constants.NULL_INT;
		}
		else if (babyType == BABY_TYPE_CHU_SHENG) {// 出生
			this.birthday = u.getString("birthday");
			this.birthdayTime = u.getString("birthday_time");

			if (!TextUtils.isEmpty(u.getString("sex"))) {
				this.babyGender = Integer.parseInt(u.getString("sex"));
			}
			else {
				this.babyGender = Constants.NULL_INT;
			}
		}
		this.userGender = Integer.parseInt(u.getString("user_sex"));
		this.address = u.getString("address");
		this.latitude = Float.parseFloat(u.getString("lat"));
		this.longitude = Float.parseFloat(u.getString("lng"));
		this.recentLogin = u.getString("last_appear");
		this.hasPhoneNum = u.getInt("hasphone") == 1 ? true : false;
	}

	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public boolean hasPhoto() {
		return !TextUtils.isEmpty(photo);
	}

	public String getPhotoSourceUrl() {
		return photo;
	}

	public String getPhotoUrl(int maxLength) {
		return App.getSmallPicUrl(photo, maxLength);
	}

	public void setPhoto(String photo) {
		this.photo = photo;
	}

	public String getPhoneNum() {
		return phoneNumber;
	}

	public void setPhoneNum(String phonenum) {
		this.phoneNumber = phonenum;
	}

	public String getPhoneType() {
		return phoneType;
	}

	public void setPhoneType(String phone_type) {
		this.phoneType = phone_type;
	}

	public String getPhoneToken() {
		return phoneToken;
	}

	public void setPhoneToken(String phone_token) {
		this.phoneToken = phone_token;
	}

	public String getOmNumber() {
		return oumenId;
	}

	public void setOmNumber(String omnumber) {
		this.oumenId = omnumber;
	}

	public String getManifesto() {
		return manifesto;
	}

	public void setManifesto(String manifesto) {
		this.manifesto = manifesto;
	}

	public int getBabyType() {
		return babyType;
	}

	public void setBabyType(int babytype) {
		this.babyType = babytype;
	}

	public String getGravidity() {
		return gravidity;
	}

	public void setGravidity(String gravidity) {
		this.gravidity = gravidity;
	}

	public String getGravidityTime() {
		return gravidityTime;
	}

	public void setGravidityTime(String gravidity_time) {
		this.gravidityTime = gravidity_time;
	}

	public String getBirthday() {
		return birthday;
	}

	public void setBirthday(String birthday) {
		this.birthday = birthday;
	}

	public String getBirthdayTime() {
		return birthdayTime;
	}

	public void setBirthdayTime(String birthday_time) {
		this.birthdayTime = birthday_time;
	}

	public int getBabyGender() {
		return babyGender;
	}

	public void setBabyGender(int gender) {
		this.babyGender = gender;
	}

	public int getUserGender() {
		return userGender;
	}

	public void setUserGender(int gender) {
		this.userGender = gender;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public float getLatitude() {
		return latitude;
	}

	public void setLatitude(float value) {
		this.latitude = value;
	}

	public float getLongitude() {
		return longitude;
	}

	public void setLongitude(float value) {
		this.longitude = value;
	}

	public String getLastAppear() {
		return recentLogin;
	}

	public void setLastAppear(String lastAppear) {
		this.recentLogin = lastAppear;
	}

	public boolean isSinaBind() {
		return sinaBind;
	}

	public void setSinaBind(boolean bind) {
		this.sinaBind = bind;
	}

	public boolean isQqBind() {
		return qqBind;
	}

	public void setQqBind(boolean bind) {
		this.qqBind = bind;
	}

	public boolean isVipBind() {
		return vipBind;
	}

	public void setVipBind(boolean bind) {
		this.vipBind = bind;
	}

	public String getFamilyInfo() {
		return familyMessage;
	}

	public void setFamilyInfo(String message) {
		this.familyMessage = message;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getThirdpartyUid() {
		return thirdpartUid;
	}

	public void setThirdpartyUid(String uid) {
		this.thirdpartUid = uid;
	}

	public boolean isThirdparty() {
		return thirdpart;
	}

	public void setThirdparty(boolean thiridparty) {
		this.thirdpart = thiridparty;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getMailMessage() {
		return mailMessage;
	}

	public void setMailMessage(String message) {
		this.mailMessage = message;
	}

	public String getActivity() {
		return activity;
	}

	public void setActivity(String activity) {
		this.activity = activity;
	}
	

	public boolean hasPhoneNum() {
		return hasPhoneNum;
	}

	public void setPhoneNum(boolean hasPhoneNum) {
		this.hasPhoneNum = hasPhoneNum;
	}

	@Override
	public String toString() {
		return "UserProfile [uid=" + uid + ", nickname=" + nickname + ", photo=" + photo + ", phoneNumber=" + phoneNumber + ", phoneType=" + phoneType + ", phoneToken=" + phoneToken + ", oumenId=" + oumenId + ", manifesto=" + manifesto + ", babyType=" + babyType + ", gravidity=" + gravidity
				+ ", gravidityTime=" + gravidityTime + ", birthday=" + birthday + ", birthdayTime=" + birthdayTime + ", babyGender=" + babyGender + ", userGender=" + userGender + ", address=" + address + ", latitude=" + latitude + ", longitude=" + longitude + ", recentLogin=" + recentLogin
				+ ", sinaBind=" + sinaBind + ", qqBind=" + qqBind + ", vipBind=" + vipBind + ", familyMessage=" + familyMessage + ", type=" + type + ", thirdpart=" + thirdpart + ", thirdpartUid=" + thirdpartUid + ", email=" + email + ", mailMessage=" + mailMessage + ", activity=" + activity
				+ ", hasPhoneNum=" + hasPhoneNum + "]";
	}
	
}
