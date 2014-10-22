package com.oumen.mv;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.oumen.db.DatabaseHelper;

/**
 * 上传文件参数
 */
public class Upload {
	public static final String TABLE_UPLOAD = "upload";

	public static final String KEY_FILE_ID = "file_id";
	public static final String KEY_NAME = "name";
	public static final String KEY_TOTAL_FILE = "total";
	public static final String KEY_CURRENT_FILE = "current";

	private int selfUid;//用户id
	private int fileId;//编号
	private String name;//名称
	private int total;//文件个数
	private int current;//当前上传到第几个


	public int getSelfUid() {
		return selfUid;
	}

	public void setSelfUid(int selfUid) {
		this.selfUid = selfUid;
	}

	public int getFileId() {
		return fileId;
	}

	public void setFileId(int fileId) {
		this.fileId = fileId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public int getCurrent() {
		return current;
	}

	public void setCurrent(int current) {
		this.current = current;
	}

	/**
	 * 插入数据
	 * 
	 * @param helper
	 */
	public static void insert(Upload data, DatabaseHelper helper) {
		synchronized (helper) {
			SQLiteDatabase db = helper.getReadableDatabase();
			Cursor cursor = db.query(TABLE_UPLOAD, null, DatabaseHelper.KEY_SELF_UID + "=" + data.selfUid + " AND " + KEY_FILE_ID + "=" + data.fileId, null, null, null, null);
			if (cursor != null) {
				if (cursor.getCount() > 0) {
					//更新数据库
					update(data, helper);
				}
				else {
					//向数据库中插入新数据
					ContentValues values = new ContentValues();
					values.put(DatabaseHelper.KEY_SELF_UID, data.selfUid);
					values.put(KEY_FILE_ID, data.fileId);
					values.put(KEY_NAME, data.name);
					values.put(KEY_TOTAL_FILE, data.total);
					values.put(KEY_CURRENT_FILE, data.current);
					db.insert(TABLE_UPLOAD, null, values);
				}
				cursor.close();
			}
		}
	}

	/**
	 * 更新数据库
	 */
	public static void update(Upload data, DatabaseHelper helper) {
		synchronized (helper) {
			SQLiteDatabase db = helper.getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put(KEY_CURRENT_FILE, data.current);
			values.put(KEY_TOTAL_FILE, data.total);
			db.update(TABLE_UPLOAD, values, DatabaseHelper.KEY_SELF_UID + "=" + data.selfUid + " AND " + KEY_FILE_ID + "=" + data.fileId, null);
		}
	}
	
	/**
	 * 更新数据库
	 */
	public static void update(int selfUid ,int fileId ,int count, DatabaseHelper helper) {
		synchronized (helper) {
			SQLiteDatabase db = helper.getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put(KEY_CURRENT_FILE, count);
			db.update(TABLE_UPLOAD, values, DatabaseHelper.KEY_SELF_UID + "=" + selfUid + " AND " + KEY_FILE_ID + "=" + fileId, null);
		}
	}
	
	/**
	 * 查询
	 */
	public static Upload query(int selfId, int fileId, DatabaseHelper helper) {
		Upload upload = null;
		synchronized (helper) {
			SQLiteDatabase db = helper.getReadableDatabase();
			Cursor cursor = db.query(TABLE_UPLOAD, null, DatabaseHelper.KEY_SELF_UID + "=" + selfId + " AND " + KEY_FILE_ID + "=" + fileId, null, null, null, null);
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					upload = new Upload();
					upload.selfUid = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.KEY_SELF_UID));
					upload.fileId = cursor.getInt(cursor.getColumnIndex(KEY_FILE_ID));
					upload.name = cursor.getString(cursor.getColumnIndex(KEY_NAME));
					upload.total = cursor.getInt(cursor.getColumnIndex(KEY_TOTAL_FILE));
					upload.current = cursor.getInt(cursor.getColumnIndex(KEY_CURRENT_FILE));
				}
				cursor.close();
			}
		}
		return upload;
	}
	
	public static int delete(int selfUid, int fileId, DatabaseHelper helper) {
		int results = 0;
		synchronized (helper) {
			SQLiteDatabase db = helper.getWritableDatabase();
			results = db.delete(TABLE_UPLOAD, DatabaseHelper.KEY_SELF_UID + "=" + selfUid + " AND " + KEY_FILE_ID + "=" + fileId, null);
		}
		return results;
	}

}
