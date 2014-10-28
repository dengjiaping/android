package com.oumen.activity.message;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 活动基础类
 * @author oumen-xin.zhang
 *
 */
public class BaseActivityMessage {
	/*
	 * 活动编号
	 */
	private int activityId;
	/*
	 * 活动名称
	 */
	private String name;
	/*
	 * 活动地址
	 */
	private String address;
	/*
	 * 活动城市
	 */
	private String city;
	/*
	 * 活动图片
	 */
	private String picUrl;
	/*
	 * 活动价格
	 */
	private String money;
	/*
	 * 活动类型
	 */
	private int type;
	/*
	 *活动距离 
	 */
	private int distance;
	
	public BaseActivityMessage() {}
	
	public BaseActivityMessage(JSONObject json) throws JSONException {
		activityId = Integer.valueOf(json.getString("atid"));
		name = json.getString("actname");
		address = json.getString("address");
		picUrl = json.getString("pic");
		if (json.has("actmoney")) {
			money = json.getString("actmoney");
		}
		if (json.has("ctcode")) {
			city = json.getString("ctcode");
		}
		if (json.has("hdtypes")) {
			type = json.getInt("hdtypes");
		}
		if (json.has("distance")) {//活动列表返回
			distance = json.getInt("distance");
		}
		else if (json.has("diss")) {// 偶们附近返回
			distance = json.getInt("diss");
		}
	}

	public int getActivityId() {
		return activityId;
	}

	public void setActivityId(int activityId) {
		this.activityId = activityId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getPicUrl() {
		return picUrl;
	}

	public void setPicUrl(String picUrl) {
		this.picUrl = picUrl;
	}
	
	public String getPicUrl(int size) {
		String url = null;
		if (picUrl != null) {
			url = picUrl + "/small?l=" + size;
		}
		else {
			url = picUrl;
		}
		return url;
	}

	public String getMoney() {
		return money;
	}

	public void setMoney(String money) {
		this.money = money;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getDistance() {
		return distance;
	}

	public void setDistance(int distance) {
		this.distance = distance;
	}
}
