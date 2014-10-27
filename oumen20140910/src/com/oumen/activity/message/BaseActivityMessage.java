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
	int activityId;
	/*
	 * 活动名称
	 */
	String name;
	/*
	 * 活动地址
	 */
	String address;
	/*
	 * 活动城市
	 */
	String city;
	/*
	 * 活动图片
	 */
	String picUrl;
	/*
	 * 活动价格
	 */
	String money;
	/*
	 * 活动类型
	 * 0-->户外活动
	 * 1-->室内活动
	 * 3-->线上活动
	 * 4-->附近活动
	 */
	int HuodongType;
	/*
	 *活动距离 
	 */
	int distance;
	
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
			HuodongType = json.getInt("hdtypes");
		}
		
		//TODO 活动详情没有此字段
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
		return HuodongType;
	}

	public void setType(int type) {
		this.HuodongType = type;
	}

	public int getDistance() {
		return distance;
	}

	public void setDistance(int distance) {
		this.distance = distance;
	}
}
