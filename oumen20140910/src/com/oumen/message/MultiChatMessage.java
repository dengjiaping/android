package com.oumen.message;

import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Parcel;
import android.os.Parcelable;

import com.oumen.R;
import com.oumen.android.App;
import com.oumen.db.DatabaseHelper;
import com.oumen.tools.ELog;

public class MultiChatMessage extends BaseMessage implements Parcelable{
	public static final String TABLE = "multi_chat";

	public static final String KEY_ACTIVITY_ID = "aid";
	public static final String KEY_MULTI_ID = "mid";

	protected ActivityMessage activityMessage;
	protected int activityId;
	protected int multiId;

	protected String selfName;
	protected String selfPhotoUrl;
	protected boolean send;

	public MultiChatMessage() {
	}

	public MultiChatMessage(ActivityMultiChatStatusMessage src) {
		activityId = src.activityId;
		multiId = src.multiId;
		actionType = src.actionType;
		type = src.type;
		content = src.content;
		datetime = src.datetime;
		targetId = src.multiId;
		targetNickname = src.activityMessage.title;
		targetPhotoUrl = src.activityMessage.picUrl;
//		read = false;
		sendType = SendType.UNREAD;
	}

	public MultiChatMessage(JSONObject json) throws NumberFormatException, JSONException {
		super(json);
		activityId = Integer.parseInt(json.getString("atid"));
		multiId = json.getInt("teamid");
//		read = false;
		sendType = SendType.UNREAD;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(actionType.code());
		dest.writeInt(type.code());
		dest.writeInt(toId);
		dest.writeString(selfName);
		dest.writeString(selfPhotoUrl);
		dest.writeInt(targetId);
		dest.writeString(targetNickname);
		dest.writeString(targetPhotoUrl);
		dest.writeLong(datetime == null ? 0 : datetime.getTime());
		dest.writeString(content);
		dest.writeValue(send);
//		dest.writeValue(read);
		dest.writeInt(sendType.code());
		dest.writeParcelable(activityMessage, PARCELABLE_WRITE_RETURN_VALUE);
		dest.writeInt(multiId);
		dest.writeInt(activityId);
	}

	public static final Parcelable.Creator<MultiChatMessage> CREATOR = new Parcelable.Creator<MultiChatMessage>() {
		public MultiChatMessage createFromParcel(Parcel in) {
			MultiChatMessage bean = new MultiChatMessage();
			bean.actionType = ActionType.parseActionType(in.readInt());
			bean.type = Type.parseMessageType(in.readInt());
			bean.toId = in.readInt();
			bean.selfName = in.readString();
			bean.selfPhotoUrl = in.readString();
			bean.targetId = in.readInt();
			bean.targetNickname = in.readString();
			bean.targetPhotoUrl = in.readString();
			long time = in.readLong();
			if (time != 0)
				bean.datetime = new Date(time);
			bean.content = in.readString();
			bean.send = (Boolean) in.readValue(Boolean.class.getClassLoader());
//			bean.read = (Boolean) in.readValue(Boolean.class.getClassLoader());
			bean.sendType = SendType.parseSendType(in.readInt());
			bean.activityMessage = in.readParcelable(ActivityMessage.class.getClassLoader());
			bean.multiId = in.readInt();
			bean.activityId = in.readInt();
			return bean;
		}

		public MultiChatMessage[] newArray(int size) {
			return new MultiChatMessage[size];
		}
	};
	
	@Override
	public int describeContents() {
		return 0;
	}

//	public static void test(int selfUid, DatabaseHelper helper) {
//		synchronized (helper) {
//			SQLiteDatabase db = helper.getReadableDatabase();
//			String sql = DatabaseHelper.KEY_SELF_UID + "=" + selfUid;
//			Cursor cursor = db.query(TABLE, null, sql, null, null, null, null);
//			while (cursor.moveToNext()) {
//				MultiChatMessage msg = cursor2bean(cursor);
//				ELog.v("Activity:" + msg.activityId + "/" + msg.multiId + " User:" + msg.toId + "/" + msg.targetId + " Read:" + msg.read);
//			}
//		}
//	}

	public static int queryNewCount(int selfUid, DatabaseHelper helper) {
		int count = 0;
		synchronized (helper) {
			SQLiteDatabase db = helper.getReadableDatabase();
			Cursor cursor = db.query(TABLE, null, DatabaseHelper.KEY_SELF_UID + "=" + selfUid + " AND " + KEY_READ + "=0", null, null, null, null);
			if (cursor != null) {
				count = cursor.getCount();
				cursor.close();
			}
		}
		return count;
	}

	public static int queryNewCount(int selfUid, int multiId, DatabaseHelper helper) {
		int count = 0;
		synchronized (helper) {
			SQLiteDatabase db = helper.getReadableDatabase();
			Cursor cursor = db.query(TABLE, null, DatabaseHelper.KEY_SELF_UID + "=" + selfUid + " AND " + KEY_MULTI_ID + "=" + multiId + " AND " + KEY_READ + "= 0", null, null, null, null);
			if (cursor != null) {
				count = cursor.getCount();
				cursor.close();
			}
		}
		return count;
	}

	public static List<MultiChatMessage> query(int selfUid, DatabaseHelper helper) {
		ArrayList<MultiChatMessage> results = new ArrayList<MultiChatMessage>();
		synchronized (helper) {
			SQLiteDatabase db = helper.getReadableDatabase();
			Cursor cursor = db.query(TABLE, null, DatabaseHelper.KEY_SELF_UID + "=" + selfUid, null, null, null, KEY_DATETIME + " DESC");
			if (cursor != null) {
				while (cursor.moveToNext()) {
					results.add(cursor2bean(cursor));
				}
				cursor.close();
			}
		}
		return results;
	}

	public static List<MultiChatMessage> queryGroups(int selfUid, DatabaseHelper helper) {
		LinkedList<MultiChatMessage> results = new LinkedList<MultiChatMessage>();
		synchronized (helper) {
			SQLiteDatabase db = helper.getReadableDatabase();
			Cursor cursor = db.query(TABLE, null, DatabaseHelper.KEY_SELF_UID + "=" + selfUid, null, KEY_MULTI_ID, null, KEY_DATETIME + " DESC");
			if (cursor != null) {
				while (cursor.moveToNext()) {
					results.add(cursor2bean(cursor));
				}
				cursor.close();
			}
		}
		return results;
	}
	
	public static MultiChatMessage queryOneGroup(int selfUid, int targetId, int multiId, ActionType type, DatabaseHelper helper) {
		String str = null;
		if (targetId != App.INT_UNSET) {
			str = DatabaseHelper.KEY_SELF_UID + "=" + selfUid +" AND " + KEY_TARGET_ID + "=" + targetId +  " AND " + KEY_MULTI_ID + "=" + multiId + "AND " + KEY_ACTION_TYPE + "=" + type.code(); 
		}
		else {
			str = DatabaseHelper.KEY_SELF_UID + "=" + selfUid + " AND " + KEY_MULTI_ID + "=" + multiId + "AND " + KEY_ACTION_TYPE + "=" + type.code();
		}
		MultiChatMessage result = null;
		synchronized (helper) {
			SQLiteDatabase db = helper.getReadableDatabase();
			Cursor cursor = db.query(TABLE, null, str, null, KEY_MULTI_ID, null, KEY_DATETIME + " DESC");
			if (cursor != null) {
				while (cursor.moveToNext()) {
					result = cursor2bean(cursor);
				}
				cursor.close();
			}
		}
		return result;
	}

	public static List<MultiChatMessage> querySingleGroup(int selfUid, int multiId, long minTimestamp, long maxTimestamp, int limit, DatabaseHelper helper) {
		LinkedList<MultiChatMessage> results = new LinkedList<MultiChatMessage>();
		String sql = DatabaseHelper.KEY_SELF_UID + "=" + selfUid + " AND " + KEY_MULTI_ID + "=" + multiId;
		if (minTimestamp != App.INT_UNSET) {
			sql += " AND " + KEY_DATETIME + ">" + minTimestamp;
		}
		if (maxTimestamp != App.INT_UNSET) {
			sql += " AND " + KEY_DATETIME + "<" + maxTimestamp;
		}
		synchronized (helper) {
			SQLiteDatabase db = helper.getReadableDatabase();
			Cursor cursor = db.query(TABLE, null, sql, null, null, null, KEY_DATETIME + " DESC", String.valueOf(limit));
			if (cursor != null) {
				while (cursor.moveToNext()) {
					MultiChatMessage bean = cursor2bean(cursor);
					results.add(bean);
//					ELog.i("Activity:" + bean.activityId + "/" + bean.multiId + " Self:" + selfUid + "/" + bean.selfName + " Target:" + bean.targetId + "/" + bean.targetNickname + " Send:" + bean.send);
				}
				cursor.close();
			}
		}
		return results;
	}

	public static boolean hasActivityCreateMessage(int selfUid, int multiId, DatabaseHelper helper) {
		String sql = DatabaseHelper.KEY_SELF_UID + "=" + selfUid + " AND " + KEY_MULTI_ID + "=" + multiId + " AND (" + KEY_ACTION_TYPE + "=" + ActionType.MULTI_CREATE.code() + " OR " + KEY_ACTION_TYPE + "=" + ActionType.MULTI_JOIN.code() + ")";
		synchronized (helper) {
			SQLiteDatabase db = helper.getReadableDatabase();
			Cursor cursor = db.query(TABLE, null, sql, null, null, null, null);
			ELog.i(String.valueOf(cursor.getCount()));
			if (cursor != null && cursor.getCount() > 0) {
				cursor.close();
				return true;
			}
		}
		return false;
	}

	public static void updateAllRead(int selfUid, int multiId, DatabaseHelper helper) {
		// TODO　如果是正在上传中的状态，就不修改已读状态了
		String sql = "UPDATE " + TABLE + " SET " + KEY_READ + "="+ SendType.READ.code() +" WHERE " + DatabaseHelper.KEY_SELF_UID + "=" + selfUid + " AND " + KEY_MULTI_ID + "=" + multiId + " AND " + KEY_READ + "=" + SendType.UNREAD.code();
		synchronized (helper) {
			SQLiteDatabase db = helper.getWritableDatabase();
			db.execSQL(sql);
		}
	}

	/**
	 * 更新某一条消息的发送状态
	 * 
	 * @param selfUid
	 * @param msg
	 * @param helper
	 */
	public static void updateSendTypeMsg(int selfUid, MultiChatMessage msg, DatabaseHelper helper) {
		String sql = "UPDATE " + TABLE + " SET " + KEY_READ + "=" + msg.getSendType().code() + " WHERE " + DatabaseHelper.KEY_SELF_UID + "=" + selfUid + " AND " + KEY_MULTI_ID + "=" + msg.getMultiId() + " AND " + KEY_DATETIME + "=" + msg.getTimestamp().getTime();
		synchronized (helper) {
			SQLiteDatabase db = helper.getWritableDatabase();
			db.execSQL(sql);
		}
	}
	/**
	 * 更新发送状态
	 * @param selfUid
	 * @param MultiId
	 * @param oldType
	 * @param newType
	 * @param helper
	 */
	public static void updateSendTypeMsg(int selfUid,int MultiId, SendType oldType, SendType newType, DatabaseHelper helper) {
		String sql = "UPDATE " + TABLE + " SET " + KEY_READ + "=" + newType.code() + " WHERE " + DatabaseHelper.KEY_SELF_UID + "=" + selfUid + " AND " + KEY_MULTI_ID + "=" + MultiId + " AND " + KEY_READ + "=" + oldType.code();
		synchronized (helper) {
			SQLiteDatabase db = helper.getWritableDatabase();
			db.execSQL(sql);
		}
	}
	
	public static void updateSendTypeMsg(int selfUid,int MultiId, SendType oldType, SendType newType, Date time, DatabaseHelper helper) {
		String sql = "UPDATE " + TABLE + " SET " + KEY_READ + "=" + newType.code() + " WHERE " + DatabaseHelper.KEY_SELF_UID + "=" + selfUid + " AND " + KEY_MULTI_ID + "=" + MultiId + " AND " + KEY_READ + "=" + oldType.code() + " AND " + KEY_DATETIME + "=" + time.getTime();
		synchronized (helper) {
			SQLiteDatabase db = helper.getWritableDatabase();
			db.execSQL(sql);
		}
	}

	public static void insert(int selfUid, MultiChatMessage bean, DatabaseHelper helper) {
		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.KEY_SELF_UID, selfUid);
		values.put(ChatMessage.KEY_SELF_NAME, bean.selfName);
		values.put(ChatMessage.KEY_SELF_PHOTO_URL, bean.selfPhotoUrl);
		values.put(KEY_TARGET_ID, bean.targetId);
		values.put(KEY_TARGET_NAME, bean.targetNickname);
		values.put(KEY_TARGET_PHOTO_URL, bean.targetPhotoUrl);
		values.put(KEY_CONTENT, bean.content);
		values.put(KEY_DATETIME, bean.datetime.getTime());
		values.put(ChatMessage.KEY_SEND, bean.send ? 1 : 0);
		values.put(KEY_TYPE, bean.type.code());
//		values.put(KEY_READ, bean.read ? 1 : 0);
		values.put(KEY_READ, bean.getSendType().code());
		values.put(KEY_ACTIVITY_ID, bean.activityId);
		values.put(KEY_MULTI_ID, bean.multiId);
		values.put(KEY_ACTION_TYPE, bean.actionType.code());
		synchronized (helper) {
			SQLiteDatabase db = helper.getWritableDatabase();
			db.insert(TABLE, null, values);
		}

//		ELog.i("Activity:" + bean.activityId + "/" + bean.multiId + " Self:" + selfUid + "/" + bean.selfName + " Target:" + bean.targetId + "/" + bean.targetNickname + " Send:" + bean.send);
	}

	public static void delete(long datetime, DatabaseHelper helper) {
		synchronized (helper) {
			SQLiteDatabase db = helper.getWritableDatabase();
			db.delete(TABLE, KEY_DATETIME + "=" + datetime, null);
		}
	}

	/**
	 * 清空群消息
	 * 
	 * @param uid
	 * @param multiId
	 * @param helper
	 */
	public static void delete(int uid, int multiId, DatabaseHelper helper) {
		synchronized (helper) {
			SQLiteDatabase db = helper.getWritableDatabase();
			String str = DatabaseHelper.KEY_SELF_UID + "=" + uid + " AND " + KEY_MULTI_ID + " =" + multiId + " AND " + BaseMessage.KEY_TARGET_ID + " <> " + App.USER.getUid();
			db.delete(TABLE, str, null);
		}
	}

	/**
	 * 清空所有的群消息
	 * 
	 * @param uid
	 * @param multiId
	 * @param helper
	 */
	public static void deleteAll(int uid, int multiId, DatabaseHelper helper) {
		synchronized (helper) {
			SQLiteDatabase db = helper.getWritableDatabase();
			String str = DatabaseHelper.KEY_SELF_UID + "=" + uid + " AND " + KEY_MULTI_ID + " =" + multiId;
			db.delete(TABLE, str, null);
		}
	}

	public static MultiChatMessage cursor2bean(Cursor cursor) {
		MultiChatMessage bean = new MultiChatMessage();
		bean.toId = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.KEY_SELF_UID));
		bean.selfName = cursor.getString(cursor.getColumnIndex(ChatMessage.KEY_SELF_NAME));
		bean.selfPhotoUrl = cursor.getString(cursor.getColumnIndex(ChatMessage.KEY_SELF_PHOTO_URL));
		bean.activityId = cursor.getInt(cursor.getColumnIndex(KEY_ACTIVITY_ID));
		bean.multiId = cursor.getInt(cursor.getColumnIndex(KEY_MULTI_ID));
		bean.actionType = ActionType.parseActionType(cursor.getInt(cursor.getColumnIndex(KEY_ACTION_TYPE)));
		bean.type = Type.parseMessageType(cursor.getInt(cursor.getColumnIndex(KEY_TYPE)));
		bean.targetId = cursor.getInt(cursor.getColumnIndex(KEY_TARGET_ID));
		bean.targetNickname = cursor.getString(cursor.getColumnIndex(KEY_TARGET_NAME));
		bean.targetPhotoUrl = cursor.getString(cursor.getColumnIndex(KEY_TARGET_PHOTO_URL));
		bean.datetime = new Date(cursor.getLong(cursor.getColumnIndex(KEY_DATETIME)));
		bean.content = cursor.getString(cursor.getColumnIndex(KEY_CONTENT));
		bean.send = cursor.getInt(cursor.getColumnIndex(ChatMessage.KEY_SEND)) == 1;
//		bean.read = cursor.getInt(cursor.getColumnIndex(KEY_READ)) == 1;
		bean.sendType = SendType.parseSendType(cursor.getInt(cursor.getColumnIndex(KEY_READ)));
		return bean;
	}

	public ActivityMessage getActivityMessage() {
		return activityMessage;
	}

	public void setActivityMessage(ActivityMessage activityMessage) {
		this.activityMessage = activityMessage;
	}

	public int getActivityId() {
		return activityId;
	}

	public void setActivityId(int activityId) {
		this.activityId = activityId;
	}

	public int getMultiId() {
		return multiId;
	}

	public void setMultiId(int multiId) {
		this.multiId = multiId;
	}

//	@Override
//	public int getTitleRightIconResId() {
//		return R.drawable.icon_message_list_item_activity;
//	}

	@Override
	public int getButtonIconResId() {
		// TODO 修改图片地址
		return R.drawable.icon_message_list_item_activity1;
	}

	@Override
	public String getIconPath() {
		if (activityMessage == null)
			return "";

		return activityMessage.picUrl;
	}

	@Override
	public String getTitle() {
		if (activityMessage == null)
			return "";

		return activityMessage.title;
	}

	public static String getCreateMultiChatInfo(Context context) {
		return context.getString(R.string.multi_create_info_by_self);
	}

	public static String getJoinMultiChatInfo(Context context, String activityTitle) {
		String info = context.getString(R.string.multi_create_info_by_other);
		Formatter format = new Formatter();
		try {
			return format.format(info, activityTitle).toString();
		}
		finally {
			format.close();
		}
	}

	public static String getSetPhoneChatInfo(Context context, String activityTitle) {
		String info = context.getString(R.string.multi_create_phone);
		Formatter format = new Formatter();
		try {
			return format.format(info, activityTitle).toString();
		}
		finally {
			format.close();
		}
	}

	public String getSelfName() {
		return selfName;
	}

	public void setSelfName(String selfName) {
		this.selfName = selfName;
	}

	public String getSelfPhotoUrl() {
		return selfPhotoUrl;
	}

	public void setSelfPhotoUrl(String selfPhotoUrl) {
		this.selfPhotoUrl = selfPhotoUrl;
	}

	public boolean isSend() {
		return send;
	}

	public void setSend(boolean send) {
		this.send = send;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + activityId;
		result = prime * result + multiId;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MultiChatMessage other = (MultiChatMessage) obj;
		if (toId == App.INT_UNSET) {
			if (other.toId != App.INT_UNSET)
				return false;
		}
		else if (toId != other.toId)
			return false;
		if (targetId == App.INT_UNSET) {
			if (other.targetId != App.INT_UNSET)
				return false;
		}
		else if (targetId != other.targetId)
			return false;
		if (multiId != other.getMultiId())
			return false;
		if (datetime.getTime() != other.getDatetime().getTime())
			return false;
		return true;
	}

}
