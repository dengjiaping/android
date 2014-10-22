package com.oumen.base;

import com.oumen.android.App;
import com.oumen.tools.ELog;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;


public class Cache {
	public static final String CACHE_USER_CHOOSE_LATITUDE = "user_choose_latitude";
	public static final String CACHE_USER_CHOOSE_LONGITUDE = "user_choose_longitude";
	public static final String CACHE_USER_CHOOSE_CITY_NAME = "user_choose_city";
	
	private SharedPreferences prefs;
	
	public Cache(Context context) {
		prefs = context.getSharedPreferences("cache", Context.MODE_PRIVATE);
	}
	
	public void save(String key, String value) {
		ELog.d("Key:" + key);
		Editor editor = prefs.edit();
		editor.putString(key, value);
		editor.commit();
	}
	
	public String read(String key) {
		ELog.d("Key:" + key);
		return prefs.getString(key, null);
	}
	
	public void save(String key, float value) {
		Editor editor = prefs.edit();
		editor.putFloat(key, value);
		editor.commit();
	}
	
	public float readFloat(String key) {
		ELog.d("Key:" + key);
		return prefs.getFloat(key, App.INT_UNSET);
	}
	
	public void clear() {
		prefs.edit().clear().commit();
	}
}
