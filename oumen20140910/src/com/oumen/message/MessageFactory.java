package com.oumen.message;

import org.json.JSONObject;

import android.text.TextUtils;

import com.oumen.book.BookMessage;
import com.oumen.message.assist.Receipt;
import com.oumen.message.ext.LoginMessage;
import com.oumen.message.ext.LogoutMessage;
import com.oumen.tools.ELog;

public class MessageFactory {
	public static Object create(String input) {
		if (TextUtils.isEmpty(input)) {
			ELog.w("Input is null");
			return null;
		}
		
		JSONObject json = null;
		try {
			json = new JSONObject(input);
			if (json.has("LoginAck")) {
				return new LoginMessage(json.getJSONObject("LoginAck"));
			}
			else if (json.has("LogoutAck")) {
				return new LogoutMessage(json.getJSONObject("LogoutAck"));
			}
			else if (json.has("cycleMessage")) {
				return new CircleMessage(json.getJSONObject("cycleMessage"));
			}
			else if (json.has("HDMessage")) {
				return new ActivityGroupMessage(json.getJSONObject("HDMessage"));
			}
//			else if (json.has("helpMessage")) {
//				return new HelpMessage(json.getJSONObject("helpMessage"));
//			}
//			else if (json.has("baodianMessage")) {
//				return new BookMessage(json.getJSONObject("baodianMessage"));
//			}
//			else if (json.has("GroupMessage")) {
//				return new MultiChatMessage(json.getJSONObject("GroupMessage"));
//			}
			else if (json.has("pushEndMessage")) {
				return new BaseMessage(json.getJSONObject("pushEndMessage"));
			}
			else if (json.has("MsgAck")) {
				return new Receipt(json.getJSONObject("MsgAck"));
			}
//			else if (json.has("Message")) {
//				JSONObject jsonMsg = json.getJSONObject("Message");
//				ActionType actionType = ActionType.parseActionType(jsonMsg.getString("action_type"));
//				switch (actionType) {
//					case CHAT:
//						return new ChatMessage(jsonMsg);
//						
//					case REQUEST_FRIEND:
//					case AGREE_FRIEND:
//					case CONFIRM_FRIEND:
//						return new FriendMessage(jsonMsg);
//						
//					case MULTI_CREATE:
//						
//					case MULTI_JOIN:
//						return new ActivityMultiChatStatusMessage(jsonMsg);
//						
//					case OTHER:
//					default:
//						return null;
//				}
//			}
		}
		catch (Exception e) {
			ELog.e("Exception:" + e.getMessage());
			e.printStackTrace();
		}
		return null;
	}
}
