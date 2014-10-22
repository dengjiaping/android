package com.oumen.android.peers.entity;

import java.io.Serializable;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.oumen.android.App;

import android.text.TextUtils;

public class CircleUserBasicMsg implements Serializable {
	private static final long serialVersionUID = -7158492743081829365L;
	private final String str = "http://114.113.228.183:8088/resource/0/png/nopic";// no pic flag

	public static final int MODE_SHARE = 1;
	public static final int MODE_EXCHANGE = 2;
	public static final int MODE_HELP = 3;
	
	
	private int circleId;// 文章编号
	
	private int uid;// 用户编号
	private String nickname;// 昵称
	private String photoUrl;// 头像

	private int modes;// 模式：分享，求助
	private String content;// 文字内容
	private String position;// 发文时的位置

	private long time;// 发表的时间
	private String createtime;// 生成时间(因为有中文，所以不能转成int)
	private boolean open;// 是否公开（1所有人，0好友可见）
	private String district;// 区域
	// 新增距离信息
	private String distance;
	//新增 个数（评论，赞人数，查看人数）
	private String priseNum;
	private String commentNum;
	private String lookNum;

	public final ArrayList<String> photos = new ArrayList<String>();// 发表图片数组

	public CircleUserBasicMsg() {
	}

	public CircleUserBasicMsg(JSONObject resobj) throws JSONException {
		uid = resobj.getInt("uid");// 用户uid
		nickname = resobj.getString("nickname");// 昵称
		photoUrl = resobj.getString("head_photo");// 头像
		content = resobj.getString("content");// 内容
		
		String tempMode = resobj.getString("modes");
		if (TextUtils.isEmpty(tempMode)) {
			modes = MODE_SHARE;// 模式
		}
		else {
			modes = Integer.valueOf(tempMode);
		}
		position = resobj.getString("position");// 位置
		circleId = Integer.parseInt(resobj.getString("cnid"));
		createtime = resobj.getString("createtime");
		
		String tempTime = resobj.has("time") ? resobj.getString("time") : null;
		if (TextUtils.isEmpty(tempTime)) {
			time = App.INT_UNSET;
		}
		else {
			time = Long.valueOf(tempTime);
		}
		
		district = resobj.has("district") ? resobj.getString("district") : null;
		open = resobj.has("isopen") ? true : false;
		distance = resobj.has("distance") ? resobj.getString("distance") : "";// 距离信息

		JSONArray array = resobj.getJSONArray("pic");
		if (array != null) {
			for (int i = 0; i < array.length(); i++) {
				if (!array.getString(i).equals(str)) {
					photos.add(array.getString(i));
				}
			}
		}
		
		if (resobj.has("prisenum")) {
			priseNum = resobj.getString("prisenum");
		}
		
		if (resobj.has("commentnum")) {
			commentNum = resobj.getString("commentnum");
		}
		
		if (resobj.has("looknum")) {
			lookNum = resobj.getString("looknum");
		}
	}

	public String getDistance() {
		return distance;
	}

	public void setDistance(String distance) {
		this.distance = distance;
	}

	public boolean isOpen() {
		return open;
	}

	public void setOpen(boolean open) {
		this.open = open;
	}

	public String getDistrict() {
		return district;
	}

	public void setDistrict(String district) {
		this.district = district;
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

	public boolean hasHeadPhoto() {
		return !TextUtils.isEmpty(photoUrl);
	}

	public String getHeadPhotoSourceUrl() {
		return photoUrl;
	}

	public String getHeadPhotoUrl(int maxLength) {
		if (TextUtils.isEmpty(photoUrl))
			return null;

		return photoUrl + "/small?l=" + maxLength;
	}

	public void setHeadphoto(String headphoto) {
		this.photoUrl = headphoto;
	}

	public int getModes() {
		return modes;
	}

	public void setModes(int modes) {
		this.modes = modes;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public int getCircleId() {
		return circleId;
	}

	public void setCircleId(int circleId) {
		this.circleId = circleId;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public String getCreatetime() {
		return createtime;
	}

	public void setCreatetime(String createtime) {
		this.createtime = createtime;
	}

	public String getPriseNum() {
		return priseNum;
	}

	public void setPriseNum(String priseNum) {
		this.priseNum = priseNum;
	}

	public String getCommentNum() {
		return commentNum;
	}

	public void setCommentNum(String commentNum) {
		this.commentNum = commentNum;
	}

	public String getLookNum() {
		return lookNum;
	}

	public void setLookNum(String lookNum) {
		this.lookNum = lookNum;
	}
	
}
