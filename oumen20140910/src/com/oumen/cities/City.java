package com.oumen.cities;

import org.json.JSONObject;

/**
 * 城市实体
 * @author oumen-xin.zhang
 *
 */
public class City {
	
	String name;// 城市名
	float lat;// 经度
	float lng;//维度
	
	public City() {}
	
	public City(JSONObject obj)throws Exception {
		name = obj.getString("city");
		lat = obj.getLong("lat");
		lng = obj.getLong("lon");
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public float getLat() {
		return lat;
	}

	public void setLat(float lat) {
		this.lat = lat;
	}

	public float getLng() {
		return lng;
	}

	public void setLng(float lng) {
		this.lng = lng;
	}
	
}
