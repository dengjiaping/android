package com.oumen.message;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.oumen.R;
import com.oumen.android.App;
import com.oumen.db.DatabaseHelper;

public class ChatMessage extends BaseMessage implements Parcelable {
	public static final String TABLE = "messages";
	
	public static final int OUMEN_TEAM_ID = 1;
	
	public static final String KEY_SELF_NAME = "self_name";
	public static final String KEY_SELF_PHOTO_URL = "self_photo_url";
	public static final String KEY_SEND = "is_send";
	
	protected String selfName;
	protected String selfPhotoUrl;
	protected boolean send;
	
	public ChatMessage() {}
	
	public ChatMessage(JSONObject json) throws JSONException {
		super(json);
	}
	
	public void updateNewCountFromDB(DatabaseHelper helper) {
		newCount = queryNewCount(toId, targetId, helper);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(actionType.code());
		dest.writeInt(type.code());
		dest.writeInt(toId);
		dest.writeString(selfName);
		dest.writeString(KEY_SELF_PHOTO_URL);
		dest.writeInt(targetId);
		dest.writeString(targetNickname);
		dest.writeString(targetPhotoUrl);
		dest.writeLong(datetime == null ? 0 : datetime.getTime());
		dest.writeString(content);
		dest.writeValue(send);
//		dest.writeValue(read);
		dest.writeInt(sendType.code());
	}

	public static final Parcelable.Creator<ChatMessage> CREATOR = new Parcelable.Creator<ChatMessage>() {
		public ChatMessage createFromParcel(Parcel in) {
			ChatMessage bean = new ChatMessage();
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
			return bean;
		}

		public ChatMessage[] newArray(int size) {
			return new ChatMessage[size];
		}
	};

	public static void insert(ChatMessage bean, DatabaseHelper helper) {
		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.KEY_SELF_UID, bean.toId);
		values.put(KEY_SELF_NAME, bean.selfName);
		values.put(KEY_SELF_PHOTO_URL, bean.selfPhotoUrl);
		values.put(KEY_TARGET_ID, bean.targetId);
		values.put(KEY_TARGET_NAME, bean.targetNickname);
		values.put(KEY_TARGET_PHOTO_URL, bean.targetPhotoUrl);
		values.put(KEY_DATETIME, bean.datetime.getTime());
		values.put(KEY_CONTENT, bean.content);
		values.put(KEY_SEND, bean.send ? 1 : 0);
		values.put(KEY_TYPE, bean.type.code());
		values.put(KEY_ACTION_TYPE, bean.actionType.code());
//		values.put(KEY_READ, bean.read ? 1 : 0);
		values.put(KEY_READ, bean.getSendType().code());
		synchronized (helper) {
			SQLiteDatabase db = helper.getWritableDatabase();
			db.insert(TABLE, null, values);
		}
	}

	public static List<ChatMessage> query(int selfUid, DatabaseHelper helper) {
		List<ChatMessage> results = new LinkedList<ChatMessage>();
		synchronized (helper) {
			SQLiteDatabase db = helper.getReadableDatabase();
			Cursor cursor = db.query(TABLE, null, DatabaseHelper.KEY_SELF_UID + "=" + selfUid, null, null, null, KEY_DATETIME + " DESC");
			if (cursor != null) {
				while (cursor.moveToNext()) {
					ChatMessage bean = cursor2bean(cursor);
					results.add(bean);
				}
				cursor.close();
			}
		}
		return results;
	}

	public static List<ChatMessage> querySingleGroup(int selfUid, int targetId, long minTimestamp, long maxTimestamp, int limit, DatabaseHelper helper) {
		List<ChatMessage> results = new LinkedList<ChatMessage>();
		String sql = DatabaseHelper.KEY_SELF_UID + "=" + selfUid + " AND " + KEY_TARGET_ID + "=" + targetId;
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
					ChatMessage bean = cursor2bean(cursor);
					results.add(bean);
				}
				cursor.close();
			}
		}
		return results;
	}
	
	public static List<ChatMessage> querySingleGroup(int selfUid, int targetId,SendType type, DatabaseHelper helper) {
		List<ChatMessage> results = new LinkedList<ChatMessage>();
		String sql = DatabaseHelper.KEY_SELF_UID + "=" + selfUid + " AND " + KEY_TARGET_ID + "=" + targetId + " AND " + KEY_READ + "=" + type.code();
		synchronized (helper) {
			SQLiteDatabase db = helper.getReadableDatabase();
			Cursor cursor = db.query(TABLE, null, sql, null, null, null, KEY_DATETIME + " DESC", null);
			if (cursor != null) {
				while (cursor.moveToNext()) {
					ChatMessage bean = cursor2bean(cursor);
					results.add(bean);
				}
				cursor.close();
			}
		}
		return results;
	}

	public static List<ChatMessage> queryGroups(int selfUid, DatabaseHelper helper) {
		List<ChatMessage> results = new LinkedList<ChatMessage>();
		synchronized (helper) {
			SQLiteDatabase db = helper.getReadableDatabase();
			Cursor cursor = db.query(TABLE, null, DatabaseHelper.KEY_SELF_UID + "=" + selfUid, null, KEY_TARGET_ID, null, KEY_DATETIME + " DESC");
			if (cursor != null) {
				while (cursor.moveToNext()) {
					ChatMessage bean = cursor2bean(cursor);
					results.add(bean);
				}
				cursor.close();
			}
		}
		return results;
	}

	public static int delete(int selfUid, int targetId, DatabaseHelper helper) {
		int results = 0;
		synchronized (helper) {
			SQLiteDatabase db = helper.getWritableDatabase();
			results = db.delete(TABLE, DatabaseHelper.KEY_SELF_UID + "=" + selfUid + " AND " + KEY_TARGET_ID + "=" + targetId, null);
		}
		return results;
	}
	
	public static int delete(long datetime, DatabaseHelper helper) {
		int results = 0;
		synchronized (helper) {
			SQLiteDatabase db = helper.getWritableDatabase();
			results = db.delete(TABLE, KEY_DATETIME + "=" + datetime, null);
		}
		return results;
	}

	public static ChatMessage cursor2bean(Cursor cursor) {
		ChatMessage bean = new ChatMessage();
		bean.actionType = ActionType.parseActionType(cursor.getInt(cursor.getColumnIndex(KEY_ACTION_TYPE)));
		bean.type = Type.parseMessageType(cursor.getInt(cursor.getColumnIndex(KEY_TYPE)));
		bean.toId = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.KEY_SELF_UID));
		bean.selfName = cursor.getString(cursor.getColumnIndex(KEY_SELF_NAME));
		bean.selfPhotoUrl = cursor.getString(cursor.getColumnIndex(KEY_SELF_PHOTO_URL));
		bean.targetId = cursor.getInt(cursor.getColumnIndex(KEY_TARGET_ID));
		bean.targetNickname = cursor.getString(cursor.getColumnIndex(KEY_TARGET_NAME));
		bean.targetPhotoUrl = cursor.getString(cursor.getColumnIndex(KEY_TARGET_PHOTO_URL));
		bean.datetime = new Date(cursor.getLong(cursor.getColumnIndex(KEY_DATETIME)));
		bean.content = cursor.getString(cursor.getColumnIndex(KEY_CONTENT));
		bean.send = cursor.getInt(cursor.getColumnIndex(KEY_SEND)) == 1;
//		bean.read = cursor.getInt(cursor.getColumnIndex(KEY_READ)) == 1;
		bean.sendType = SendType.parseSendType(cursor.getInt(cursor.getColumnIndex(KEY_READ)));
		return bean;
	}

	/**
	 * 查询数据库中未读 的消息总数
	 * 
	 * @param selfUid
	 * @param helper
	 * @return
	 */
	public static int queryNewCount(int selfUid, DatabaseHelper helper) {
		int count = 0;
		String sql = DatabaseHelper.KEY_SELF_UID + "=" + selfUid + " AND " + KEY_READ + "=" + SendType.UNREAD.code();
		synchronized (helper) {
			SQLiteDatabase db = helper.getReadableDatabase();
			Cursor cursor = db.query(TABLE, null, sql, null, null, null, null);
			if (cursor != null) {
				count = cursor.getCount();
				cursor.close();
			}
		}
		return count;
	}

	/**
	 * 查询数据库中和某个朋友的未读的消息总数
	 */
	public static int queryNewCount(int selfUid, int targetId, DatabaseHelper helper) {
		int count = 0;
		String sql = DatabaseHelper.KEY_SELF_UID + "=" + selfUid + " AND " + KEY_TARGET_ID + "=" + targetId + " AND " + KEY_READ + "=" + SendType.UNREAD.code();
		synchronized (helper) {
			SQLiteDatabase db = helper.getReadableDatabase();
			Cursor cursor = db.query(TABLE, null, sql, null, null, null, null);
			if (cursor != null) {
				count = cursor.getCount();
				cursor.close();
			}
		}
		return count;
	}
	
	public static void update2Read(int selfUid, int targetId, DatabaseHelper helper) {
		String sql = "UPDATE " + TABLE + " SET " + KEY_READ + "="+ SendType.READ.code() +" WHERE " + DatabaseHelper.KEY_SELF_UID + "=" + selfUid + " AND " + KEY_TARGET_ID + "=" + targetId + " AND " + KEY_READ + "=" + SendType.UNREAD.code();
		synchronized (helper) {
			SQLiteDatabase db = helper.getWritableDatabase();
			db.execSQL(sql);
		}
	}
	
	public static void updateSendTypeMsg(int selfUid,ChatMessage msg,DatabaseHelper helper) {
		String sql = "UPDATE " + TABLE + " SET " + KEY_READ + "="+ msg.getSendType().code() +" WHERE " + DatabaseHelper.KEY_SELF_UID + "=" + selfUid + " AND " + KEY_TARGET_ID + "=" + msg.getTargetUid() + " AND " + KEY_DATETIME + "=" + msg.getTimestamp().getTime();
		synchronized (helper) {
			SQLiteDatabase db = helper.getWritableDatabase();
			db.execSQL(sql);
		}
	}
	
	public static void updateSendTypeMsg(int selfUid,int tagetId, SendType oldType, SendType newType , DatabaseHelper helper) {
		String sql = "UPDATE " + TABLE + " SET " + KEY_READ + "="+ newType.code() +" WHERE " + DatabaseHelper.KEY_SELF_UID + "=" + selfUid + " AND " + KEY_TARGET_ID + "=" + tagetId + " AND " + KEY_READ + "=" + oldType.code();
		synchronized (helper) {
			SQLiteDatabase db = helper.getWritableDatabase();
			db.execSQL(sql);
		}
	}
	public static void updateSendTypeMsg(int selfUid,int tagetId, SendType oldType, SendType newType , Date time , DatabaseHelper helper) {
		String sql = "UPDATE " + TABLE + " SET " + KEY_READ + "="+ newType.code() +" WHERE " + DatabaseHelper.KEY_SELF_UID + "=" + selfUid + " AND " + KEY_TARGET_ID + "=" + tagetId + " AND " + KEY_READ + "=" + oldType.code() + " AND " + KEY_DATETIME + "="+ time.getTime();
		synchronized (helper) {
			SQLiteDatabase db = helper.getWritableDatabase();
			db.execSQL(sql);
		}
	}
	@Override
	public int getIconResId() {
		return targetId == OUMEN_TEAM_ID ? R.drawable.icon_oumen_team : App.INT_UNSET;
	}

	@Override
	public String getIconPath() {
		if (TextUtils.isEmpty(targetPhotoUrl)) {
			int uid = App.USER.getUid();
			if (toId == uid) {
				return App.USER.getPhotoSourceUrl();
			}
		}
		return targetPhotoUrl;
	}

	@Override
	public String getTitle() {
		if (targetId == OUMEN_TEAM_ID) {
			return "偶们团队";
		}
		return targetNickname;
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
		result = prime * result + ((selfName == null) ? 0 : selfName.hashCode());
		result = prime * result + ((selfPhotoUrl == null) ? 0 : selfPhotoUrl.hashCode());
		result = prime * result + (send ? 1231 : 1237);
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
		ChatMessage other = (ChatMessage) obj;
		
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
		if (datetime.getTime() != other.getDatetime().getTime())
			return false;
		return true;
	}
	
}
