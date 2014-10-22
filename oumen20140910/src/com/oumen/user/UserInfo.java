package com.oumen.user;

import java.io.Serializable;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

import com.oumen.android.App;
import com.oumen.android.BasicUserInfo;
import com.oumen.android.peers.entity.CircleUserBasicMsg;

/**
 * 个人信息展示
 * 
 * @author oumen
 * 
 */
public class UserInfo implements BasicUserInfo, Serializable {
	private static final long serialVersionUID = -4970263536271610043L;
	
	// 每页数据量
	private int pagenum;
	// 当前是第几页
	private int page;
	// 总共有多少页
	private int pagetotal;
	// 总共有多少条动态
	private int total;
	// 个人基本信息
	private int uid;
	private String nickname;
	private String headPhotoUrl;
	private String sign;
	private int babytype;
	private String gravidity;
	private String address;
	private String backPic;
	private boolean isFriend;

	// 个人发表的动态集合
	private ArrayList<CircleUserBasicMsg> userinfos = new ArrayList<CircleUserBasicMsg>();

	public UserInfo() {
		super();
	}

	public UserInfo(JSONObject object) throws JSONException {
		total = Integer.valueOf(object.getString("total"));
		page = Integer.valueOf(object.getString("page"));
		pagenum = object.getInt("pagenum");
		pagetotal = object.getInt("pagetotal");

		org.json.JSONObject r = object.getJSONObject("userinfor");
		uid = Integer.parseInt(r.getString("user_id"));
		nickname = r.getString("nickname");
		headPhotoUrl = r.getString("head_photo");
		sign = r.getString("manifesto");
		babytype = Integer.parseInt(r.getString("babytype"));
		gravidity = r.getString("gravidity");
		address = r.getString("address");
		backPic = r.getString("backpic");
		if (0 == r.getInt("isfrind")) {
			isFriend = false;
		}
		else
			if (1 == r.getInt("isfrind")) {
				isFriend = true;
			}
		// 解析个人发表的信息集合
		org.json.JSONArray resultArray = object.getJSONArray("data");
		if (resultArray != null) {
			for (int i = 0; i < resultArray.length(); i++) {
				CircleUserBasicMsg p = new CircleUserBasicMsg(resultArray.getJSONObject(i));
				userinfos.add(p);
			}
		}
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public void setBabytype(int babytype) {
		this.babytype = babytype;
	}

	public int getPagenum() {
		return pagenum;
	}

	public void setPagenum(int pagenum) {
		this.pagenum = pagenum;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getPagetotal() {
		return pagetotal;
	}

	public void setPagetotal(int pagetotal) {
		this.pagetotal = pagetotal;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	@Override
	public int getUid() {
		return uid;
	}

	@Override
	public void setUid(int uid) {
		this.uid = uid;
	}

	@Override
	public String getNickname() {
		return nickname;
	}

	@Override
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	
	@Override
	public boolean hasPhoto() {
		return !TextUtils.isEmpty(headPhotoUrl);
	}

	@Override
	public String getPhotoSourceUrl() {
		return headPhotoUrl;
	}

	@Override
	public String getPhotoUrl(int maxLength) {
		return App.getSmallPicUrl(headPhotoUrl, maxLength);
	}

	@Override
	public void setPhotoUrl(String photoUrl) {
		this.headPhotoUrl = photoUrl;
	}

	public String getSign() {
		return sign;
	}

	public void setSign(String sign) {
		this.sign = sign;
	}

	public String getGravidity() {
		return gravidity;
	}

	public void setGravidity(String gravidity) {
		this.gravidity = gravidity;
	}

	public int getBabytype() {
		return babytype;
	}

	public String getBackPic() {
		return backPic;
	}

	public void setBackPic(String backPic) {
		this.backPic = backPic;
	}

	public String getBackgroundUrl(int maxLength) {
		if (TextUtils.isEmpty(backPic))
			return null;

		return backPic + "/small?l=" + maxLength;
	}

	public boolean isFriend() {
		return isFriend;
	}

	public void setFriend(boolean isFriend) {
		this.isFriend = isFriend;
	}

	public ArrayList<CircleUserBasicMsg> getUserinfos() {
		return userinfos;
	}

	public void setUserinfos(ArrayList<CircleUserBasicMsg> userinfos) {
		this.userinfos = userinfos;
	}

}
