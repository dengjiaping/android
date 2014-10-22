package com.oumen.android.util;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;

import com.oumen.android.App;
import com.oumen.tools.ELog;

@SuppressLint("applyPrefEdits")
public class SharePreferenceUtil {
	private SharedPreferences sp;
	private Editor editor;

	/**
	 * 构造函数
	 * */
	public SharePreferenceUtil(Context context, String file) {
		sp = context.getSharedPreferences(file, Context.MODE_PRIVATE);
		// 利用edit()方法获取Editor对象。
		editor = sp.edit();
	}

	public boolean setEmail(String email, String loginType) {
		if (TextUtils.isEmpty(email)) {
			editor.putString("email", "");
		}
		else {
			editor.putString("email", loginType + email);
		}
		return editor.commit();
	}

	public String[] getEmail() {
		String email = sp.getString("email", "");
		if (email.length() > 0) {
			return new String[] { email.substring(0, 1), email.substring(1) };
		}
		return null;
	}
	
	public void addHistorySearch(String str) {
		Set<String> list = null;
		if (getHistorySearchList() == null) {
			list = new TreeSet<String>();
		}
		else {
			list = getHistorySearchList();
		}
		list.add(str);
		
		editor.putStringSet("history_search", list);
		editor.putString("last_history_search", str);
		editor.commit();
	}
	
	public Set<String> getHistorySearchList() {
		return sp.getStringSet("history_search", new TreeSet<String>());
	}
	
	public String getLastHistorySearch() {
		return sp.getString("last_history_search", null);
	}
	
	public void setPhoneLogin(boolean flag) {
		editor.putBoolean("phone_login", flag);
		editor.commit();
	}
	
	public boolean isPhoneLogin() {
		return sp.getBoolean("phone_login", false);
	}

	public boolean setUserProfile(String profile) {
		editor.putString("profile", profile);
		return editor.commit();
	}

	public String getUserProfile() {
		return sp.getString("profile", null);
	}

	public boolean setHadLogin(int selfUid, boolean first) {
		String current = sp.getString("had_login", null);
		if (TextUtils.isEmpty(current)) {
			current = selfUid + "|" + (first ? 1 : 0);
		}
		else {
			boolean updated = false;
			String tmp = "";
			String[] groups = current.split(",");
			for (String group : groups) {
				String[] values = group.split("\\|");
				if (selfUid == Integer.parseInt(values[0])) {
					tmp += "," + selfUid + "|" + (first ? 1 : 0);
					updated = true;
				}
				else {
					tmp += "," + group;
				}
			}
			
			if (!updated) {
				tmp += "," + selfUid + "|" + (first ? 1 : 0);
			}

			if (tmp.length() > 0) {
				current = tmp.substring(1);
			}
		}
		editor.putString("had_login", current);
		return editor.commit();
	}

	public boolean isHadLogin(int selfUid) {
		String tmp = sp.getString("had_login", null);
		if (TextUtils.isEmpty(tmp)) {
			return false;
		}
		else {
			String[] groups = tmp.split(",");
			for (String group : groups) {
				String[] values = group.split("\\|");
				if (selfUid == Integer.parseInt(values[0])) {
					return values[1].equals("1") ? true : false;
				}
			}
			return false;
		}
	}

	// 保存密码
	public boolean setPwd(String pwd) {
		editor.putString("password", pwd);
		return editor.commit();
	}

	public String getPwd() {
		return sp.getString("password", "");
	}

	// 保存用户uid
	public boolean setUid(int uid) {
		editor.putInt("id", uid);
		return editor.commit();
	}

	public int getUid() {
		return sp.getInt("id", 0);
	}

	public float getLatitude() {
		return sp.getFloat("lat", 0.0f);
	}

	public boolean setLatitude(float value) {
		editor.putFloat("lat", value);
		return editor.commit();
	}

	public float getLongitude() {
		return sp.getFloat("lng", 0.0f);
	}

	public boolean setLongitude(float value) {
		editor.putFloat("lng", value);
		return editor.commit();
	}
	
	public void setCurrentCityName(String name) {
		editor.putString("current_city_name", name);
		editor.commit();
	}
	
	public String getCurrentCityName() {
		return sp.getString("current_city_name", "北京");
	}

	/**
	 * 新浪授权的值
	 */
	public boolean setSinaId(String uid) {
		editor.putString("sina_id", uid);
		return editor.commit();
	}

	public String getSinaId() {
		return sp.getString("sina_id", null);
	}

	public boolean setSinaToken(String token) {
		editor.putString("sina_token", token);
		return editor.commit();
	}

	public String getSinaToken() {
		return sp.getString("sina_token", null);
	}

	public boolean setSinaExprise(long exprise) {
		editor.putLong("sina_exprise", exprise);
		return editor.commit();
	}

	public long getSinaExprise() {
		return sp.getLong("sina_exprise", App.INT_UNSET);
	}
	
	/**
	 * 新浪授权的值
	 */
	public boolean setWeiXinId(String uid) {
		editor.putString("weixin_id", uid);
		return editor.commit();
	}

	public String getWeiXinId() {
		return sp.getString("weixin_id", null);
	}

	public boolean setWeiXinToken(String token) {
		editor.putString("weixin_token", token);
		return editor.commit();
	}

	public String getWeiXinToken() {
		return sp.getString("weixin_token", null);
	}

	public boolean setWeiXinExprise(long exprise) {
		editor.putLong("weixin_exprise", exprise);
		return editor.commit();
	}

	public long getWeiXinExprise() {
		return sp.getLong("weixin_exprise", App.INT_UNSET);
	}

	/**
	 * qq授权的值
	 * 
	 */
	public boolean setQQId(String uid) {
		editor.putString("qq_id", uid);
		return editor.commit();
	}

	public String getQQId() {
		return sp.getString("qq_id", null);
	}

	public boolean setQQToken(String token) {
		editor.putString("qq_token", token);
		return editor.commit();
	}

	public String getQQToken() {
		return sp.getString("qq_token", null);
	}

	public boolean setQQExprise(long exprise) {
		editor.putLong("qq_exprise", exprise);
		return editor.commit();
	}

	public long getQQExprise() {
		return sp.getLong("qq_exprise", App.INT_UNSET);
	}
	
	public boolean setMVShareUrl(String url) {
		editor.putString("mvshareurl", url);
		return editor.commit();
	}
	
	public String getMVShareUrl() {
		return sp.getString("mvshareurl", "");
	}
	
	public boolean setActivityListMsg(String json) {
		editor.putString("activity_msg", json);
		return editor.commit();
	}
	
	public String getActivityListMsg() {
		return sp.getString("activity_msg", null);
	}
	
	public boolean setAcceptMsg(boolean accept) {
		editor.putBoolean("accept_msg", accept);
		return editor.commit();
	}
	
	public boolean isAcceptMsg() {
		return sp.getBoolean("accept_msg", true);
	}
	
	public boolean setSoundOpen(boolean open) {
		editor.putBoolean("sound_msg", open);
		return editor.commit();
	}
	
	public boolean isSoundOpen() {
		return sp.getBoolean("sound_msg", true);
	}
	
	public boolean setShakeOpen(boolean shake) {
		editor.putBoolean("shake_msg", shake);
		return editor.commit();
	}
	
	public boolean isShakeOpen() {
		return sp.getBoolean("shake_msg", true);
	}
	
	public void setMVDate(int[] date) {
		String uid = String.valueOf(getUid()),
			value = date[0] + App.NUMBER_FORMAT.format(date[1]) + App.NUMBER_FORMAT.format(date[2]);
		try {
			String json = sp.getString("mv_date", null);
			JSONObject root = TextUtils.isEmpty(json) ? new JSONObject() : new JSONObject(json);
			root.put(uid, value);
			editor.putString("mv_date", root.toString());
		}
		catch (Exception e) {
			ELog.e("Exception:" + e.getMessage());
			e.printStackTrace();
		}
	}
	
	public int[] getMVDate() {
		String json = sp.getString("mv_date", null);
		if (TextUtils.isEmpty(json)) {
			return null;
		}
		else {
			try {
				JSONObject root = new JSONObject(json);
				String uid = String.valueOf(getUid()), value = root.optString(uid);
				if (!TextUtils.isEmpty(value)) {
					int[] date = new int[3];
					date[0] = Integer.parseInt(value.substring(0, 4));
					date[1] = Integer.parseInt(value.substring(4, 6));
					date[2] = Integer.parseInt(value.substring(6));
					return date;
				}
			}
			catch (Exception e) {
				ELog.e("Exception:" + e.getMessage());
				e.printStackTrace();
			}
			return null;
		}
	}
	
	//左侧导航
	public boolean isFirstToUserCenter() {
		return sp.getBoolean("first_to_usercenter", true);
	}
	
	public boolean setFirstToUserCenter(boolean first) {
		editor.putBoolean("first_to_usercenter", first);
		return editor.commit();
	}
	
	public boolean isFristToMV() {
		return sp.getBoolean("first_to_mv", true);
	}
	
	public boolean setFirstToMV(boolean first) {
		editor.putBoolean("first_to_mv", first);
		return editor.commit();
	}
	
	public boolean isFirstToCircle() {
		return sp.getBoolean("first_to_circle", true);
	}
	
	public boolean setFirstToCircle(boolean first) {
		editor.putBoolean("first_to_circle", first);
		return editor.commit();
	}
	
	public boolean isFirstToSetting() {
		return sp.getBoolean("first_to_setting", true);
	}
	
	public boolean setFirstToSetting(boolean first) {
		editor.putBoolean("first_to_setting", first);
		return editor.commit();
	}
	
	public String getSplashImageUrl() {
		return sp.getString("splash_img", null);
	}
	
	public boolean setSplashImageUrl(String url) {
		editor.putString("splash_img", url);
		return editor.commit();
	}
}
