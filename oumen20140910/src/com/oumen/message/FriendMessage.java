package com.oumen.message;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.oumen.R;
import com.oumen.android.App;
import com.oumen.db.DatabaseHelper;

public class FriendMessage extends BaseMessage {
	public static final String TABLE = "req_friends";

	public FriendMessage() {
	}

	public FriendMessage(JSONObject json) throws JSONException {
		super(json);
	}

	public static void insert(FriendMessage bean, DatabaseHelper helper) {
		synchronized (helper) {
			SQLiteDatabase db = helper.getWritableDatabase();
			Cursor cursor = db.query(TABLE, null, DatabaseHelper.KEY_SELF_UID + "=" + bean.toId + " AND " + KEY_TARGET_ID + "=" + bean.targetId, null, null, null, null);
			if (cursor.getCount() == 0) {
				ContentValues values = new ContentValues();
				values.put(DatabaseHelper.KEY_SELF_UID, bean.toId);
				values.put(KEY_ACTION_TYPE, bean.actionType.code());
				values.put(KEY_TYPE, bean.type.code());
				values.put(KEY_TARGET_ID, bean.targetId);
				values.put(KEY_TARGET_NAME, bean.targetNickname);
				values.put(KEY_TARGET_PHOTO_URL, bean.targetPhotoUrl);
				values.put(KEY_CONTENT, bean.content);
				values.put(KEY_DATETIME, bean.datetime.getTime());
				values.put(KEY_READ, SendType.UNREAD.code());
				db.insert(TABLE, null, values);
			}
			else {
				// 如果数据库中已有对应的记录，就替换到
				update(bean.toId, bean.targetId, bean.content, bean.actionType, bean.datetime.getTime(), App.DB);
			}
			cursor.close();
		}
	}

	public static List<FriendMessage> query(int selfUid, DatabaseHelper helper) {
		List<FriendMessage> results = new LinkedList<FriendMessage>();
		synchronized (helper) {
			SQLiteDatabase db = helper.getReadableDatabase();
			Cursor cursor = db.query(TABLE, null, DatabaseHelper.KEY_SELF_UID + "=" + selfUid, null, null, null, null);
			if (cursor != null) {
				while (cursor.moveToNext()) {
					FriendMessage bean = cursor2bean(cursor);
					results.add(bean);
				}
				cursor.close();
			}
		}
		return results;
	}

	public static FriendMessage query(int selfUid, int targetUid, DatabaseHelper helper) {
		FriendMessage bean = null;
		synchronized (helper) {
			SQLiteDatabase db = helper.getReadableDatabase();
			Cursor cursor = db.query(TABLE, null, DatabaseHelper.KEY_SELF_UID + "=" + selfUid + " AND " + KEY_TARGET_ID + "=" + targetUid, null, null, null, KEY_DATETIME + " DESC");
			while (cursor.moveToNext()) {
				bean = cursor2bean(cursor);
				break;
			}
			cursor.close();
		}
		return bean;
	}

	/**
	 * 查询数据库中未读 的好友请求消息总数
	 */
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

	/**
	 * 修改已读标记
	 */
	public static void update2read(int selfUid, int targetId, DatabaseHelper helper) {
		synchronized (helper) {
			SQLiteDatabase db = helper.getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put(KEY_READ, 1);
			db.update(TABLE, values, DatabaseHelper.KEY_SELF_UID + "=" + selfUid + " AND " + KEY_TARGET_ID + "=" + targetId, null);
		}
	}

	public static int delete(int selfUid, int targetId, DatabaseHelper helper) {
		int results = 0;
		synchronized (helper) {
			SQLiteDatabase db = helper.getWritableDatabase();
			results = db.delete(TABLE, DatabaseHelper.KEY_SELF_UID + "=" + selfUid + " AND " + KEY_TARGET_ID + "=" + targetId, null);
		}
		return results;
	}

	public static int delete(int selfUid, DatabaseHelper helper) {
		int results = 0;
		synchronized (helper) {
			SQLiteDatabase db = helper.getWritableDatabase();
			results = db.delete(TABLE, DatabaseHelper.KEY_SELF_UID + "=" + selfUid, null);
		}
		return results;
	}

	public static FriendMessage cursor2bean(Cursor cursor) {
		FriendMessage bean = new FriendMessage();
		bean.type = Type.parseMessageType(cursor.getInt(cursor.getColumnIndex(KEY_TYPE)));
		bean.actionType = ActionType.parseActionType(cursor.getInt(cursor.getColumnIndex(KEY_ACTION_TYPE)));
		bean.toId = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.KEY_SELF_UID));
		bean.targetId = cursor.getInt(cursor.getColumnIndex(KEY_TARGET_ID));
		bean.targetNickname = cursor.getString(cursor.getColumnIndex(KEY_TARGET_NAME));
		bean.targetPhotoUrl = cursor.getString(cursor.getColumnIndex(KEY_TARGET_PHOTO_URL));
		bean.content = cursor.getString(cursor.getColumnIndex(KEY_CONTENT));
		bean.datetime = new Date(cursor.getLong(cursor.getColumnIndex(KEY_DATETIME)));
//		bean.read = cursor.getInt(cursor.getColumnIndex(KEY_READ)) == 1 ? true : false;
		bean.sendType = SendType.parseSendType(cursor.getInt(cursor.getColumnIndex(KEY_READ)));
		return bean;
	}

	public static void update(int selfUid, int targetUid, String content, ActionType actionType, long time, DatabaseHelper helper) {
		synchronized (helper) {
			SQLiteDatabase db = helper.getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put(KEY_CONTENT, TextUtils.isEmpty(content) ? "" : content);
			values.put(KEY_ACTION_TYPE, actionType.code());
			values.put(KEY_DATETIME, time);
			db.update(TABLE, values, DatabaseHelper.KEY_SELF_UID + "=" + selfUid + " AND " + KEY_TARGET_ID + "=" + targetUid, null);
		}
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
	public int getButtonIconResId() {
		return ActionType.REQUEST_FRIEND.equals(actionType) ? R.drawable.icon_plus : App.INT_UNSET;
	}
	
	@Override
	public String getDescription() {
		return ActionType.CONFIRM_FRIEND.equals(actionType) ? "您已添加" + targetNickname + "为好友" : content;
	}
}
