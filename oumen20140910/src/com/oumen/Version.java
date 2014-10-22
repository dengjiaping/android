package com.oumen;

import org.json.JSONException;
import org.json.JSONObject;

import com.oumen.android.util.Constants;

import android.text.TextUtils;

/**
 * 版本的实体类
 * @author oumen
 *
 */
 public class Version {
	private String vid;
	private String description;
	private String apkPath;
	private String createTime;
	private int version1;
	private int version2;
	private int version3;
	
	private Version() {}
	
	public Version(String version) {
		parse(version);
	}
	
	public Version(JSONObject obj) throws JSONException {
		vid = obj.getString("vid");
		description = obj.getString("disc");
		apkPath = obj.getString("apk_path");
		createTime = obj.getString("createtime");
		String ip = obj.optString("ip"), port = obj.optString("port");
		if (!TextUtils.isEmpty(ip) && !TextUtils.isEmpty(port)) {
			Constants.SOCKET_SERVER_IP = ip;
			Constants.SOCKET_SERVER_PORT = Integer.parseInt(port);
		}

		parse(obj.getString("version"));
	}
	
	private void parse(String version) {
		version = version.replaceAll("V", "");
		String[] tmp = version.split("\\.");
		version1 = Integer.parseInt(tmp[0]);
		version2 = Integer.parseInt(tmp[1]);
		version3 = Integer.parseInt(tmp[2]);
	}
	
	public String getVersion() {
		return "V" + version1 + "." + version2 + "." + version3;
	}

	public String getVid() {
		return vid;
	}

	public void setVid(String vid) {
		this.vid = vid;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getApkPath() {
		return apkPath;
	}

	public void setApkPath(String apkPath) {
		this.apkPath = apkPath;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public int getVersion1() {
		return version1;
	}

	public void setVersion1(int version1) {
		this.version1 = version1;
	}

	public int getVersion2() {
		return version2;
	}

	public void setVersion2(int version2) {
		this.version2 = version2;
	}

	public int getVersion3() {
		return version3;
	}

	public void setVersion3(int version3) {
		this.version3 = version3;
	}
	
	public Version clone() {
		Version dest = new Version();
		dest.apkPath = apkPath;
		dest.createTime = createTime;
		dest.description = description;
		dest.version1 = version1;
		dest.version2 = version2;
		dest.version3 = version3;
		dest.vid = vid;
		return dest;
	}

	@Override
	public String toString() {
		return "Version [vid=" + vid + ", description=" + description + ", apkPath=" + apkPath + ", createTime=" + createTime + ", version1=" + version1 + ", version2=" + version2 + ", version3=" + version3 + "]";
	}

}
