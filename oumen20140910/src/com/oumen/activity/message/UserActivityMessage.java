package com.oumen.activity.message;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 用户参与活动基类
 * 
 * @author oumen-xin.zhang
 *
 */
public class UserActivityMessage extends BaseActivityMessage {

	private String startTime;//2014-07-12 15:00:00
	private String endTime;
	private int teamId;

	public UserActivityMessage() {
	}

	public UserActivityMessage(JSONObject json) throws JSONException {
		super(json);
		startTime = json.getString("starttime");
		endTime = json.getString("endtime");
		teamId = Integer.valueOf(json.getString("teamid"));
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public int getTeamId() {
		return teamId;
	}

	public void setTeamId(int teamId) {
		this.teamId = teamId;
	}
	
}
