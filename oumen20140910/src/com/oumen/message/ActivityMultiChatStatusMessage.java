package com.oumen.message;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

public class ActivityMultiChatStatusMessage extends BaseMessage {
	protected ActivityMessage activityMessage;
	protected int activityId;
	protected int multiId;

	public ActivityMultiChatStatusMessage() {}

	public ActivityMultiChatStatusMessage(JSONObject json) throws NumberFormatException, JSONException {
		super(json);
		activityId = json.getInt("atid");
		multiId = json.getInt("teamid");
		content = json.optString("body");
		actionType = ActionType.parseActionType(json.getString("action_type"));
		type = Type.parseType(json.getString("type"));
		datetime = new Date(json.getLong("timestamp"));
		activityMessage = new ActivityMessage();
		activityMessage.id = json.getInt("teamid");
		activityMessage.title = json.optString("actname");
		activityMessage.description = json.optString("dis");
		activityMessage.picUrl = json.optString("pic");
		activityMessage.address = json.optString("address");
		Object tmp = json.opt("starttime");
		if (tmp instanceof Long) {
			activityMessage.startTime = new Date((Long)tmp);
		}
		else {
			activityMessage.startTime = new Date();
		}
	}

}
