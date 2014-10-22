package com.oumen.message.ext;

import org.json.JSONException;
import org.json.JSONObject;

public class LogoutMessage {
	protected boolean success;
	protected String info;
	
	public LogoutMessage(JSONObject json) throws JSONException {
		success = json.getBoolean("success");
		info = json.optString("info");
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}
}
