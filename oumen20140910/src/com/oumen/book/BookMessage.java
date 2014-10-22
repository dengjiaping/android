package com.oumen.book;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.oumen.R;
import com.oumen.android.App;
import com.oumen.db.DatabaseHelper;
import com.oumen.message.ActionType;
import com.oumen.message.BaseMessage;
import com.oumen.message.MessageListItemDataProvider;
import com.oumen.message.Type;

public class BookMessage implements MessageListItemDataProvider {
	public static final int BABY_TYPE_BORN = 0;
	public static final int BABY_TYPE_UNBORN = 1;
	
	public static final String TABLE = "books";
	public static final String KEY_BABY_TYPE = "baby_type";
	public static final String KEY_TITLE = "title";
	public static final String KEY_DAYS = "days";
	public static final String KEY_URL = "url";
	public static final String KEY_CREATE_AT = "create_at";

	protected int id;
	protected int babyType;
	protected String days;
	protected String title;
	protected String content;
	protected String url;
	protected ActionType actionType;
	protected Type type;
	protected Date createAt;
	protected Date timestamp;
	protected boolean read;
	
	protected int newCount;

	public BookMessage() {}

	public BookMessage(JSONObject json) throws NumberFormatException, JSONException {
		id = Integer.parseInt(json.getString("bid"));
		babyType = Integer.parseInt(json.getString("babytype"));
		days = json.optString("stitle");
		title = json.optString("title");
		content = json.optString("body");
		url = json.optString("url");
		actionType = ActionType.parseActionType(json.optString("action_type"));
		type = Type.parseType(json.optString("type"));
		createAt = new Date(json.optLong("addtime"));
		timestamp = new Date(json.optLong("timestamp"));
	}
	
	public static List<BookMessage> query(int selfUid, DatabaseHelper helper) {
		LinkedList<BookMessage> results = new LinkedList<BookMessage>();
		synchronized (helper) {
			SQLiteDatabase db = helper.getReadableDatabase();
			String sql = DatabaseHelper.KEY_SELF_UID + "=" + selfUid;
			Cursor cursor = db.query(TABLE, null, sql, null, null, null, BaseMessage.KEY_DATETIME + " DESC");
			if (cursor != null) {
				while (cursor.moveToNext()) {
					results.add(cursor2bean(cursor));
				}
				cursor.close();
			}
		}
		return results;
	}
	
	public static BookMessage query(int selfUid, int id, DatabaseHelper helper) {
		BookMessage result = null;
		synchronized (helper) {
			SQLiteDatabase db = helper.getReadableDatabase();
			String sql = DatabaseHelper.KEY_SELF_UID + "=" + selfUid + " AND " + DatabaseHelper.KEY_ID + "=" + id;
			Cursor cursor = db.query(TABLE, null, sql, null, null, null, null);
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					result = cursor2bean(cursor);
				}
				cursor.close();
			}
		}
		return result;
	}
	
	public static BookMessage queryLatest(int selfUid, DatabaseHelper helper) {
		BookMessage result = null;
		synchronized (helper) {
			SQLiteDatabase db = helper.getReadableDatabase();
			String sql = DatabaseHelper.KEY_SELF_UID + "=" + selfUid;
			Cursor cursor = db.query(TABLE, null, sql, null, null, null, BaseMessage.KEY_DATETIME + " DESC");
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					result = cursor2bean(cursor);
				}
				cursor.close();
			}
		}
		return result;
	}
	
	public static int queryNewCount(int selfUid, DatabaseHelper helper) {
		int count = 0;
		synchronized (helper) {
			SQLiteDatabase db = helper.getReadableDatabase();
			Cursor cursor = db.query(TABLE, null, DatabaseHelper.KEY_SELF_UID + "=" + selfUid + " AND " + BaseMessage.KEY_READ + "=0", null, null, null, null);
			if (cursor != null) {
				count = cursor.getCount();
				cursor.close();
			}
		}
		return count;
	}
	
	public static void updateAllRead(int selfUid, DatabaseHelper helper) {
		String sql = "UPDATE " + TABLE + " SET " + BaseMessage.KEY_READ + "=1 WHERE " + DatabaseHelper.KEY_SELF_UID + "=" + selfUid + " AND " + BaseMessage.KEY_READ + "=0";
		synchronized (helper) {
			SQLiteDatabase db = helper.getWritableDatabase();
			db.execSQL(sql);
		}
	}
	
	public static void insert(int selfUid, BookMessage bean, DatabaseHelper helper) {
		synchronized (helper) {
			SQLiteDatabase db = helper.getReadableDatabase();
			Cursor cursor = db.query(TABLE, null, DatabaseHelper.KEY_SELF_UID + "=" + selfUid + " AND " + DatabaseHelper.KEY_ID + "=" + bean.id, null, null, null, null);
			if (cursor != null && cursor.getCount() > 0) {
				return;
			}
		}
		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.KEY_SELF_UID, selfUid);
		values.put(DatabaseHelper.KEY_ID, bean.id);
		values.put(KEY_BABY_TYPE, bean.babyType);
		values.put(KEY_DAYS, bean.days);
		values.put(KEY_TITLE, bean.title);
		values.put(KEY_URL, bean.url);
		values.put(KEY_CREATE_AT, bean.createAt.getTime());
		values.put(BaseMessage.KEY_CONTENT, bean.content);
		values.put(BaseMessage.KEY_DATETIME, bean.timestamp.getTime());
		values.put(BaseMessage.KEY_ACTION_TYPE, bean.actionType.code());
		values.put(BaseMessage.KEY_TYPE, bean.type.code());
		synchronized (helper) {
			SQLiteDatabase db = helper.getWritableDatabase();
			db.insert(TABLE, null, values);
		}
	}
	
	public static BookMessage cursor2bean(Cursor cursor) {
		BookMessage bean = new BookMessage();
		bean.id = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.KEY_ID));
		bean.babyType = cursor.getInt(cursor.getColumnIndex(KEY_BABY_TYPE));
		bean.days = cursor.getString(cursor.getColumnIndex(KEY_DAYS));
		bean.title = cursor.getString(cursor.getColumnIndex(KEY_TITLE));
		bean.content = cursor.getString(cursor.getColumnIndex(BaseMessage.KEY_CONTENT));
		bean.url = cursor.getString(cursor.getColumnIndex(KEY_URL));
		bean.actionType = ActionType.parseActionType(cursor.getInt(cursor.getColumnIndex(BaseMessage.KEY_ACTION_TYPE)));
		bean.type = Type.parseMessageType(cursor.getInt(cursor.getColumnIndex(BaseMessage.KEY_TYPE)));
		bean.createAt = new Date(cursor.getLong(cursor.getColumnIndex(KEY_CREATE_AT)));
		bean.timestamp = new Date(cursor.getLong(cursor.getColumnIndex(BaseMessage.KEY_DATETIME)));
		bean.read = cursor.getInt(cursor.getColumnIndex(BaseMessage.KEY_READ)) == 1 ? true : false;
		return bean;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getBabyType() {
		return babyType;
	}

	public void setBabyType(int babyType) {
		this.babyType = babyType;
	}

	public String getDays() {
		return days;
	}

	public void setDays(String days) {
		this.days = days;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public ActionType getActionType() {
		return actionType;
	}

	public void setActionType(ActionType actionType) {
		this.actionType = actionType;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public Date getCreateAt() {
		return createAt;
	}

	public void setCreateAt(Date createAt) {
		this.createAt = createAt;
	}

	public boolean isRead() {
		return read;
	}

	public void setRead(boolean read) {
		this.read = read;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public void setNewCount(int newCount) {
		this.newCount = newCount;
	}

	@Override
	public int getNewCount() {
		return newCount;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public int getTitleRightIconResId() {
		return App.INT_UNSET;
	}

	@Override
	public String getDescription() {
		return content;
	}

	@Override
	public int getIconResId() {
		return R.drawable.icon_baby_book;
	}

	@Override
	public String getIconPath() {
		return null;
	}

	@Override
	public int getButtonIconResId() {
		return App.INT_UNSET;
	}

	@Override
	public Date getTimestamp() {
		return timestamp;
	}
}
