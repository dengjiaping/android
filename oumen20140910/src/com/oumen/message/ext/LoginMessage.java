package com.oumen.message.ext;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginMessage {
	public static final int CODE_SUCCESS = 0;
	public static final int CODE_FAILED = 1;
	public static final int CODE_KICKED = 2;
	
	protected boolean success;
	protected int code;
	protected String info;
	protected long serverTimeOffset;
	
	public LoginMessage(JSONObject json) throws JSONException {
		success = json.getBoolean("success");
		code = json.getInt("code");
		info = json.optString("info");
		if (json.has("serverTime")) {
			serverTimeOffset = Long.parseLong(json.getString("serverTime")) - System.currentTimeMillis();
		}
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public long getServerTimeOffset() {
		return serverTimeOffset;
	}

	public void setServerTimeOffset(long serverTimeOffset) {
		this.serverTimeOffset = serverTimeOffset;
	}
}
