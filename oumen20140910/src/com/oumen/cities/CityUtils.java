package com.oumen.cities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

public class CityUtils {
	
	public static Map<String, List<City>> getProvincesData(Context context) throws Exception {
		Map<String, List<City>> map = new HashMap<String, List<City>>();
		map = getProvinces(getProvincesString(context));
		return map;
	}

	/**
	 * 解析省份和城市信息
	 * @param context
	 * @return
	 */
	private static String getProvincesString(Context context) {
		InputStream inputStream = null;
		StringBuffer sb = new StringBuffer("");
		try {
			inputStream = context.getAssets().open("cities.txt");

			InputStreamReader inputStreamReader = null;

			inputStreamReader = new InputStreamReader(inputStream, "UTF-8");

			BufferedReader reader = new BufferedReader(inputStreamReader);
			
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line);
				sb.append("\n");
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally{
			try {
				if (inputStream != null) {
					inputStream.close();
				}
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}
	
	/**
	 * 获取省份列表
	 * 
	 * @param str
	 * @return
	 */
	private static Map<String, List<City>> getProvinces(String str) throws Exception {
		Map<String, List<City>> lists = new LinkedHashMap<String, List<City>>();
		try {
			JSONArray array = new JSONArray(str);
			for (int i = 0; i < array.length(); i++) {
				JSONObject obj = array.getJSONObject(i);
//				lists.put(obj.getString("State"), obj.getString("Cities"));
				lists.put(obj.getString("State"), getCitiesList(obj.getString("Cities")));
			}
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
		return lists;
	}

	/**
	 * 获取省份对应的城市列表
	 * 
	 * @param str
	 * @return
	 */
	private static List<City> getCitiesList(String str) throws Exception {
		List<City> lists = new ArrayList<City>();
		try {
			JSONArray array = new JSONArray(str);
			for (int i = 0; i < array.length(); i++) {
				lists.add(new City(array.getJSONObject(i)));
			}
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
		return lists;
	}
}
