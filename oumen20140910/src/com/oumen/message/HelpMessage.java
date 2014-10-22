package com.oumen.message;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.oumen.db.DatabaseHelper;

public class HelpMessage extends BaseMessage {
	public static final String TABLE = "helps";
	
	public static final String KEY_GROUP_ID = "gid";
	
	protected int groupId;//求助帖子id

	public HelpMessage() {}
	
	public HelpMessage(JSONObject json) throws NumberFormatException, JSONException {
		actionType = ActionType.parseActionType(json.optString("action_type"));
		type = Type.parseType(json.getString("type"));
		toId = json.getInt("to");
		targetId = json.getInt("from");
		targetNickname = json.optString("fromNick");
		targetPhotoUrl = json.optString("fromPhoto");
		datetime = new Date(json.getLong("timestamp"));
		groupId = json.getInt("body");
	}
	
	public static List<HelpMessage> query(int selfUid, DatabaseHelper helper) {
		LinkedList<HelpMessage> results = new LinkedList<HelpMessage>();
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
	
	public static void insert(int selfUid, HelpMessage bean, DatabaseHelper helper) {
		synchronized (helper) {
			SQLiteDatabase db = helper.getReadableDatabase();
			Cursor cursor = db.query(TABLE, null, DatabaseHelper.KEY_SELF_UID + "=" + selfUid + " AND " + KEY_GROUP_ID + "=" + bean.groupId, null, null, null, null);
			if (cursor == null || cursor.getCount() > 0)
				return;
		}
		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.KEY_SELF_UID, selfUid);
		values.put(KEY_GROUP_ID, bean.groupId);
		values.put(KEY_TARGET_ID, bean.targetId);
		values.put(KEY_TARGET_NAME, bean.targetNickname);
		values.put(KEY_TARGET_PHOTO_URL, bean.targetPhotoUrl);
		values.put(KEY_DATETIME, bean.datetime.getTime());
		values.put(KEY_TYPE, bean.type.code());
		values.put(KEY_ACTION_TYPE, bean.actionType.code());
//		values.put(KEY_READ, bean.read ? 1 : 0);
		values.put(KEY_READ, bean.sendType.code());
		synchronized (helper) {
			SQLiteDatabase db = helper.getWritableDatabase();
			db.insert(TABLE, null, values);
		}
	}
	
	public static void delete(long datetime, DatabaseHelper helper) {
		synchronized (helper) {
			SQLiteDatabase db = helper.getWritableDatabase();
			db.delete(TABLE, KEY_DATETIME + "=" + datetime, null);
		}
	}
	
	public static void update2read(long datetime, DatabaseHelper helper) {
		synchronized (helper) {
			SQLiteDatabase db = helper.getWritableDatabase();
			db.execSQL("UPDATE " + TABLE + " SET " + KEY_READ + "=1 WHERE " + KEY_DATETIME + "=" + datetime);
		}
	}

	public static HelpMessage cursor2bean(Cursor cursor) {
		HelpMessage bean = new HelpMessage();
		bean.groupId = cursor.getInt(cursor.getColumnIndex(KEY_GROUP_ID));
		bean.actionType = ActionType.parseActionType(cursor.getInt(cursor.getColumnIndex(KEY_ACTION_TYPE)));
		bean.type = Type.parseMessageType(cursor.getInt(cursor.getColumnIndex(KEY_TYPE)));
		bean.targetId = cursor.getInt(cursor.getColumnIndex(KEY_TARGET_ID));
		bean.targetNickname = cursor.getString(cursor.getColumnIndex(KEY_TARGET_NAME));
		bean.targetPhotoUrl = cursor.getString(cursor.getColumnIndex(KEY_TARGET_PHOTO_URL));
		bean.datetime = new Date(cursor.getLong(cursor.getColumnIndex(KEY_DATETIME)));
//		bean.read = cursor.getInt(cursor.getColumnIndex(KEY_READ)) == 1 ? true : false;
		bean.sendType = SendType.parseSendType(cursor.getInt(cursor.getColumnIndex(KEY_READ)));
		return bean;
	}

	@Override
	public int getNewCount() {
		if (SendType.UNREAD.equals(sendType)) {
			return 1;
		}
		else {
			return 0;
		}
//		return read ? 0 : 1;
	}

	@Override
	public String getDescription() {
		return "向你发起了一个求助";
	}
}
