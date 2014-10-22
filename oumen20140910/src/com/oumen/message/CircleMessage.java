package com.oumen.message;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONObject;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.oumen.db.DatabaseHelper;

public class CircleMessage extends BaseMessage implements Parcelable {
	public static final String TABLE = "circles";
	
	public static final String KEY_CIRCLE_ID = "circle_id";
	public static final String KEY_CIRCLE_TITLE = "circle_title";
	public static final String KEY_CIRCLE_PIC = "circle_pic";
	public static final String KEY_ABOUT_ID = "about_id";

	protected int circleId;
	protected String circleTitle;
	protected String circlePic;
	protected int aboutId;//赞或者评论的id

	public CircleMessage() {
	}

	public CircleMessage(JSONObject json) throws Exception {
		actionType = ActionType.parseActionType(json.getString("action_type"));
		type = Type.parseType(json.getString("type"));
		targetNickname = json.optString("nickname");
		targetPhotoUrl = json.optString("header_photo");
		circleTitle = json.optString("ctitle");
		circlePic = json.optString("pic");

		Object tmp = json.get("timetemp");
		if (tmp instanceof String) {
			datetime = new Date(Long.parseLong((String) tmp));
		}
		else {
			datetime = new Date((Long) tmp);
		}

		tmp = json.get("cid");
		if (tmp instanceof String) {
			circleId = Integer.parseInt((String) tmp);
		}
		else {
			circleId = (Integer) tmp;
		}

		tmp = json.get("from");
		if (tmp instanceof String) {
			targetId = Integer.parseInt((String) tmp);
		}
		else {
			targetId = (Integer) tmp;
		}

		tmp = json.get("to");
		if (tmp instanceof String) {
			toId = Integer.parseInt((String) tmp);
		}
		else {
			toId = (Integer) tmp;
		}

		tmp = json.get("from");
		if (tmp instanceof String) {
			targetId = Integer.parseInt((String) tmp);
		}
		else {
			targetId = (Integer) tmp;
		}

		if (json.has("commen_id")) {
			aboutId = json.getInt("commen_id");
			content = json.optString("commen_content");
		}
		else if (json.has("prize_id")) {
			aboutId = json.getInt("prize_id");
		}
	}
	
	public static int queryNewCount(int selfUid, DatabaseHelper helper) {
		int count = 0;
		synchronized (helper) {
			SQLiteDatabase db = helper.getReadableDatabase();
			Cursor cursor = db.query(TABLE, null, DatabaseHelper.KEY_SELF_UID + "=" + selfUid + " AND " + BaseMessage.KEY_READ + "=" + SendType.UNREAD.code(), null, null, null, null);
			if (cursor != null) {
				count = cursor.getCount();
			}
			cursor.close();
		}
		return count;
	}
	
	public static List<CircleMessage> queryNews(int selfUid, DatabaseHelper helper) {
		List<CircleMessage> results = new LinkedList<CircleMessage>();
		String sql = DatabaseHelper.KEY_SELF_UID + "=" + selfUid + " AND " + KEY_READ + "=" + SendType.UNREAD.code();
		synchronized (helper) {
			SQLiteDatabase db = helper.getReadableDatabase();
			Cursor cursor = db.query(TABLE, null, sql, null, null, null, null);
			if (cursor != null) {
				while (cursor.moveToNext()) {
					results.add(cursor2bean(cursor));
				}
				cursor.close();
			}
		}
		return results;
	}
	
	public static void updateAllRead(int selfUid, DatabaseHelper helper) {
		String sql = "UPDATE " + TABLE + " SET " + KEY_READ + "="+ SendType.READ.code() +" WHERE " + DatabaseHelper.KEY_SELF_UID + "=" + selfUid + " AND " + KEY_READ + "="+ SendType.UNREAD.code();
		synchronized (helper) {
			SQLiteDatabase db = helper.getWritableDatabase();
			db.execSQL(sql);
		}
	}

	public static void insert(CircleMessage msg, DatabaseHelper helper) {
		String sql = DatabaseHelper.KEY_SELF_UID + "=" + msg.toId + " AND " + KEY_ABOUT_ID + "=" + msg.aboutId;
		synchronized (helper) {
			SQLiteDatabase db = helper.getWritableDatabase();
			Cursor cursor = db.query(TABLE, null, sql, null, null, null, null);
			if (cursor != null && cursor.getCount() > 0) {
				cursor.close();
				return;
			}

			ContentValues values = new ContentValues();
			values.put(DatabaseHelper.KEY_SELF_UID, msg.toId);
			values.put(KEY_CIRCLE_ID, msg.circleId);
			values.put(KEY_CIRCLE_TITLE, msg.circleTitle);
			if (!TextUtils.isEmpty(msg.circlePic))
				values.put(KEY_CIRCLE_PIC, msg.circlePic);
			if (!TextUtils.isEmpty(msg.content))
				values.put(KEY_CONTENT, msg.content);
			values.put(KEY_ABOUT_ID, msg.aboutId);
			values.put(KEY_TARGET_ID, msg.targetId);
			values.put(KEY_TARGET_NAME, msg.targetNickname);
			values.put(KEY_TARGET_PHOTO_URL, msg.targetPhotoUrl);
			values.put(KEY_DATETIME, msg.datetime.getTime());
			values.put(KEY_ACTION_TYPE, msg.actionType.code());
			values.put(KEY_TYPE, msg.type.code());
//			values.put(KEY_READ, msg.read ? 1 : 0);
			values.put(KEY_READ, msg.getSendType().code());
			
			db.insert(TABLE, null, values);
		}
	}

	public static int delete(int selfUid, DatabaseHelper helper) {
		int results = 0;
		synchronized (helper) {
			SQLiteDatabase db = helper.getWritableDatabase();
			results = db.delete(TABLE, DatabaseHelper.KEY_SELF_UID + "=" + selfUid, null);
		}
		return results;
	}

	public static CircleMessage cursor2bean(Cursor cursor) {
		CircleMessage msg = new CircleMessage();
		msg.actionType = ActionType.parseActionType(cursor.getInt(cursor.getColumnIndex(KEY_ACTION_TYPE)));
		msg.circleId = cursor.getInt(cursor.getColumnIndex(KEY_CIRCLE_ID));
		msg.circleTitle = cursor.getString(cursor.getColumnIndex(KEY_CIRCLE_TITLE));
		msg.circlePic = cursor.getString(cursor.getColumnIndex(KEY_CIRCLE_PIC));
		msg.aboutId = cursor.getInt(cursor.getColumnIndex(KEY_ABOUT_ID));
		msg.type = Type.parseMessageType(cursor.getInt(cursor.getColumnIndex(KEY_TYPE)));
		msg.toId = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.KEY_SELF_UID));
		msg.targetId = cursor.getInt(cursor.getColumnIndex(KEY_TARGET_ID));
		msg.targetNickname = cursor.getString(cursor.getColumnIndex(KEY_TARGET_NAME));
		msg.targetPhotoUrl = cursor.getString(cursor.getColumnIndex(KEY_TARGET_PHOTO_URL));
		msg.datetime = new Date(cursor.getLong(cursor.getColumnIndex(KEY_DATETIME)));
		msg.content = cursor.getString(cursor.getColumnIndex(KEY_CONTENT));
//		msg.read = cursor.getInt(cursor.getColumnIndex(KEY_READ)) == 1;
		msg.sendType = SendType.parseSendType(cursor.getInt(cursor.getColumnIndex(KEY_READ)));
		return msg;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof CircleMessage) {
			CircleMessage temp = (CircleMessage) o;
			if (temp.datetime == datetime) {
				return true;
			}
			return false;
		}
		return false;
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
		dest.writeInt(targetId);
		dest.writeString(targetNickname);
		dest.writeString(targetPhotoUrl);
		dest.writeLong(datetime == null ? 0 : datetime.getTime());
		dest.writeString(content);
//		dest.writeValue(read);
		dest.writeInt(sendType.code());

		dest.writeInt(circleId);
		dest.writeString(circleTitle);
		dest.writeString(circlePic);
		dest.writeInt(aboutId);
	}

	public static final Parcelable.Creator<CircleMessage> CREATOR = new Parcelable.Creator<CircleMessage>() {
		public CircleMessage createFromParcel(Parcel in) {
			CircleMessage bean = new CircleMessage();

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
//			bean.read = (Boolean) in.readValue(Boolean.class.getClassLoader());
			bean.sendType = SendType.parseSendType(in.readInt());

			bean.circleId = in.readInt();
			bean.circleTitle = in.readString();
			bean.circlePic = in.readString();
			bean.aboutId = in.readInt();

			return bean;
		}

		public CircleMessage[] newArray(int size) {
			return new CircleMessage[size];
		}
	};

	public int getCircleId() {
		return circleId;
	}

	public void setCircleId(int circleId) {
		this.circleId = circleId;
	}

	public String getCircleTitle() {
		return circleTitle;
	}

	public void setCircleTitle(String circleTitle) {
		this.circleTitle = circleTitle;
	}

	public String getCirclePic() {
		return circlePic;
	}

	public void setCirclePic(String circlePic) {
		this.circlePic = circlePic;
	}

	public int getAboutId() {
		return aboutId;
	}

	public void setAboutId(int aboutId) {
		this.aboutId = aboutId;
	}
	
}
