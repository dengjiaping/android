package com.oumen.message;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.oumen.android.App;

public class BaseMessage implements MessageListItemDataProvider ,Parcelable {
	public static final String KEY_TYPE = "type";
	public static final String KEY_ACTION_TYPE = "action_type";
	public static final String KEY_TARGET_ID = "target_uid";
	public static final String KEY_TARGET_NAME = "target_nick";
	public static final String KEY_TARGET_PHOTO_URL = "target_photo_url";
	public static final String KEY_DATETIME = "datetime";
	public static final String KEY_CONTENT = "content";
	public static final String KEY_READ = "is_read";

	protected int toId;
	protected int targetId;
	protected String targetNickname;
	protected String targetPhotoUrl;
	protected Date datetime;
	protected String content;
	protected Type type;
	protected ActionType actionType;
//	protected boolean read;
	protected int newCount;
	protected SendType sendType;

	public BaseMessage() {
	}

	public BaseMessage(JSONObject json) throws JSONException {
		type = Type.parseType(json.getString("type"));
		actionType = ActionType.parseActionType(json.getString("action_type"));
		datetime = new Date(json.getLong("timestamp"));
		content = json.optString("body");
		toId = json.optInt("to");
		targetId = json.getInt("from");
		if (json.has("fromNick"))
			targetNickname = json.getString("fromNick");
		if (json.has("fromPhoto"))
			targetPhotoUrl = json.getString("fromPhoto");
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	public static final Parcelable.Creator<BaseMessage> CREATOR = new Creator<BaseMessage>() {
		
		@Override
		public BaseMessage[] newArray(int size) {
			return new BaseMessage[size];
		}
		
		@Override
		public BaseMessage createFromParcel(Parcel in) {
			BaseMessage bean = new BaseMessage();
			bean.actionType = ActionType.parseActionType(in.readInt());
			bean.type = Type.parseMessageType(in.readInt());
			bean.toId = in.readInt();
			bean.targetId = in.readInt();
			bean.targetNickname = in.readString();
			bean.targetPhotoUrl = in.readString();
			long time = in.readLong();
			if (time != 0)
				bean.datetime = new Date(time);
			bean.content = in.readString();
			bean.sendType = SendType.parseSendType(in.readInt());
			bean.newCount = in.readInt();
			return bean;
		}
	};

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(actionType.code());
		dest.writeInt(type.code());
		dest.writeInt(toId);
		dest.writeInt(targetId);
		dest.writeString(targetNickname);
		dest.writeString(targetPhotoUrl);
		dest.writeLong(datetime == null ? 0 : datetime.getTime());
		dest.writeString(content);
		dest.writeInt(sendType.code());
		dest.writeInt(newCount);
	}

	public int getToId() {
		return toId;
	}

	public void setToId(int toId) {
		this.toId = toId;
	}

	public int getTargetUid() {
		return targetId;
	}

	public void setTargetId(int targetUid) {
		this.targetId = targetUid;
	}

	public String getTargetNickname() {
		return targetNickname;
	}

	public void setTargetNickname(String targetNickname) {
		this.targetNickname = targetNickname;
	}

	public boolean hasTargetPhoto() {
		return !TextUtils.isEmpty(targetPhotoUrl);
	}

	public String getTargetPhotoSourceUrl() {
		return targetPhotoUrl;
	}

	public String getTargetPhotoUrl(int maxLength) {
		return App.getSmallPicUrl(targetPhotoUrl, maxLength);
	}

	public void setTargetPhotoUrl(String targetPhotoUrl) {
		this.targetPhotoUrl = targetPhotoUrl;
	}

	public Date getDatetime() {
		return datetime;
	}

	public void setDatetime(Date datetime) {
		this.datetime = datetime;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public ActionType getActionType() {
		return actionType;
	}

	public void setActionType(ActionType actionType) {
		this.actionType = actionType;
	}

//	public boolean isRead() {
//		return read;
//	}
//
//	public void setRead(boolean read) {
//		this.read = read;
//	}
	
	@Override
	public int getNewCount() {
		return newCount;
	}

	public SendType getSendType() {
		return sendType;
	}

	public void setSendType(SendType sendType) {
		this.sendType = sendType;
	}

	@Override
	public String getTitle() {
		return targetNickname;
	}

	@Override
	public int getTitleRightIconResId() {
		return App.INT_UNSET;
	}

	@Override
	public String getDescription() { // 对表情消息进行处理，显示为"表情"
		if (type.equals(Type.TEXT)) { // 文字消息
			return content;
		}
		else {
			return "表情";
		}
	}

	@Override
	public int getIconResId() {
		return App.INT_UNSET;
	}

	@Override
	public String getIconPath() {
		return targetPhotoUrl;
	}

	@Override
	public int getButtonIconResId() {
		return App.INT_UNSET;
	}

	@Override
	public Date getTimestamp() {
		return datetime;
	}
}
