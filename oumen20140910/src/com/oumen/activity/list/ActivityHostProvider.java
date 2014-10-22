package com.oumen.activity.list;

import java.util.List;

import org.json.JSONObject;

import com.oumen.activity.message.BaseActivityMessage;

public interface ActivityHostProvider {
	
	public List<BaseActivityMessage> parseJson(JSONObject json);
}
