package com.oumen.message.assist;

import org.json.JSONObject;

public class Receipt implements Sendable {
	private String from;
	private String to;
	private String timestamp;

	public Receipt() {
	}

	public Receipt(JSONObject obj)throws Exception {
		this.from = String.valueOf(obj.getInt("from"));
		this.to = String.valueOf(obj.getInt("to"));
		this.timestamp = obj.getString("timestamp");
	}
	
	public Receipt(String from, String to, String timestamp) {
		this.from = from;
		this.to = to;
		this.timestamp = timestamp;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	public String toMessage() {
		return "{\"MsgAck\":{\"from\":" + from + ",\"to\":" + to + ",\"timestamp\":" + timestamp + ",\"status\":\"read\"}}";
	}
}
