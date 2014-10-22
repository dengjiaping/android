package com.oumen.message;

import java.util.Date;

import com.oumen.message.assist.Sendable;

public class SendGroupMessage extends MultiChatMessage implements Sendable {
	public SendGroupMessage() {
	}

	/**
	 * 向服务器发送群聊消息
	 * @param from
	 * @param fromnickname
	 * @param fromphoto
	 * @param activityid
	 * @param teamid
	 * @param activityMsg
	 * @param timestamp
	 * @param action_type
	 * @param InputType
	 * @param body
	 */
	public SendGroupMessage(int from, String fromnickname, String fromphoto, int activityid , int teamid , ActivityMessage activityMsg , Date timestamp, ActionType action_type, Type InputType, String body) {
		toId = from;
		selfName = fromnickname;
		selfPhotoUrl = fromphoto;
		
		multiId = teamid;
		activityId = activityid;
		activityMessage = activityMsg;
		
		datetime = timestamp;
		actionType = action_type;
		type = InputType;
		content = body;
	}
	
	@Override
	public String toMessage() {
		return "{\"GroupMessage\":{\"from\":" + toId + ",\"fromNick\":\"" + selfName + 
				"\",\"fromPhoto\":\"" + selfPhotoUrl + "\",\"to\":" + multiId +
				",\"teamid\":\""+ multiId + "\",\"atid\":\"" + activityId + "\",\"mainPhoto\":\"" + activityMessage.getPicUrl()+
				"\",\"mainuid\":"+ activityMessage.getOwnerId() +",\"timestamp\":" + datetime.getTime() + 
				",\"type\":\"" + type.text() + "\",\"action_type\":\"" + actionType.text() + "\",\"body\":\"" + content
				+ "\"}}";
	}
}
