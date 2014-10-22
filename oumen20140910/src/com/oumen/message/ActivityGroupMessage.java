package com.oumen.message;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.oumen.db.DatabaseHelper;
import com.oumen.tools.ELog;

public class ActivityGroupMessage {
	public final List<ActivityMessage> data = new LinkedList<ActivityMessage>();
	
	public ActivityGroupMessage(JSONObject json) throws JSONException {
		Date timestamp = new Date(json.getLong("timestamp"));
		JSONArray body = json.getJSONArray("body");
		for (int i = 0; i < body.length(); i++) {
			ActivityMessage act = new ActivityMessage(body.getJSONObject(i), ActivityMessage.FROM_SOCKET);
			act.timestamp = timestamp;
			data.add(act);
		}
	}
	
	public void insert(int selfUid, DatabaseHelper helper) {
		ELog.e("Count:" + data.size());
		for (ActivityMessage i : data) {
			ActivityMessage.insert(selfUid, i, helper);
		}
	}
}
