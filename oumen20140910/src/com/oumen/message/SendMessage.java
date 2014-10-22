package com.oumen.message;

import java.util.Date;

import com.oumen.message.assist.Sendable;

public class SendMessage extends BaseMessage implements Sendable {
	public SendMessage() {
	}

	public SendMessage(int from, String fromnickname, String fromphoto, int to, Date timestamp, ActionType action_type, Type InputType, String body) {
		targetId = from;
		targetNickname = fromnickname;
		targetPhotoUrl = fromphoto;
		toId = to;
		datetime = timestamp;
		actionType = action_type;
		type = InputType;
		content = body;
	}

	@Override
	public String toMessage() {
		return "{\"Message\":{\"from\":" + targetId + ",\"fromNick\":\"" + targetNickname + "\",\"fromPhoto\":\"" + targetPhotoUrl + "\",\"to\":" + toId + ",\"timestamp\":" + datetime.getTime() + ",\"type\":\"" + type.text() + "\",\"action_type\":\"" + actionType.text() + "\",\"body\":\"" + content
				+ "\"}}";
	}
}
