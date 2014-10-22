package com.oumen.mv;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Parcel;
import android.os.Parcelable;

import com.oumen.android.App;
import com.oumen.auth.AuthAdapter.MessageType;
import com.oumen.auth.ShareData;
import com.oumen.db.DatabaseHelper;
import com.oumen.mv.index.UploadTask;
import com.oumen.tools.ELog;

public class MvInfo implements Parcelable, ShareData {
	private final String SHARE_CONTENT = "我正在用“偶们”制作宝宝MV，你也给你的宝宝制作一个吧！";//分享的内容
	private static final Lock lock = new ReentrantLock();

	public static final String TABLE = "compose";

	public static final String KEY_TITLE = "title";
	public static final String KEY_PREFIX_ID = "prefix_id";
	public static final String KEY_TYPE = "type";
	public static final String KEY_DATE = "date";
	public static final String KEY_SERVER_ID = "sid";
	public static final String KEY_SERVER_URL = "url";
	public static final String KEY_USER_ID = "uid";

	public static final byte TYPE_UNPUBLISHED = 0;
	public static final byte TYPE_PUBLISHED = 1;
	public static final byte TYPE_UPLOAD = 2;
	public static final byte TYPE_COMPOSE = 3;

	protected int userId;
	protected String title;
	protected PrefixVideo prefix;
	protected int type;
	protected Calendar createAt;

	protected int serverId;
	protected String serverUrl;
	
	protected UploadTask uploadTask;

	public MvInfo() {
		uploadTask = UploadTask.query(this, App.DB);
	}

	public MvInfo(int userId, String title, int prefixId, int type, Calendar createAt) {
		this.userId = userId;
		this.title = title;
		this.prefix = PrefixVideo.query(prefixId);
		this.type = type;
		this.createAt = createAt;

		uploadTask = UploadTask.query(this, App.DB);
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public PrefixVideo getPrefix() {
		return prefix;
	}

	public Calendar getCreateAt() {
		return createAt;
	}

	public String getCreateAtString() {
		return new SimpleDateFormat("yyyy-MM-dd", App.LOCALE).format(createAt.getTime());
	}

	public void setCreateAt(Calendar createAt) {
		this.createAt = createAt;
	}

	public String getPath() {
		return App.PATH_MV + "/" + userId + "/" + title + ".mp4";
	}

	public int getServerId() {
		return serverId;
	}

	public void setServerId(int serverId) {
		this.serverId = serverId;
	}

	public String getServerUrl() {
		return serverUrl;
	}

	public void setServerUrl(String serverUrl) {
		this.serverUrl = serverUrl;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
	
	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public UploadTask getUploadTask() {
		return uploadTask;
	}

	public static List<MvInfo> query(int userId, DatabaseHelper helper) {
		LinkedList<MvInfo> results = new LinkedList<MvInfo>();
		LinkedList<MvInfo> deletes = new LinkedList<MvInfo>();

		try {
			lock.lock();

			SQLiteDatabase db = helper.getWritableDatabase();
			Cursor cursor = db.query(TABLE, null, KEY_USER_ID + "=" + userId, null, null, null, KEY_DATE + " DESC");
			if (cursor != null) {
				while (cursor.moveToNext()) {
					MvInfo bean = cursor2bean(cursor);
					File f = new File(bean.getPath());
					if (!f.exists()) {
						deletes.add(bean);
						continue;
					}
					results.add(bean);
				}
				cursor.close();
			}

			if (!deletes.isEmpty()) {
				StringBuilder sql = new StringBuilder("DELETE FROM " + TABLE + " WHERE ");
				for (MvInfo i : deletes) {
					sql.append("(`").append(KEY_USER_ID).append("`=").append(i.userId).append(" AND `").append(KEY_TITLE).append("`='").append(i.title).append("' AND `").append(KEY_TYPE).append("`=").append(i.type).append(") OR ");
				}
				sql.delete(sql.length() - 4, sql.length());
				ELog.i("Delete:" + sql.toString());
				db.execSQL(sql.toString());
			}
		}
		finally {
			lock.unlock();
		}
		return results;
	}

	public static List<MvInfo> query(int userId, int type, DatabaseHelper helper) {
		LinkedList<MvInfo> results = new LinkedList<MvInfo>();
		LinkedList<MvInfo> deletes = new LinkedList<MvInfo>();

		try {
			lock.lock();

			SQLiteDatabase db = helper.getWritableDatabase();
			Cursor cursor = db.query(TABLE, null, KEY_USER_ID + "=" + userId + " AND " + KEY_TYPE + "=" + type, null, null, null, KEY_DATE + " ASC");
			if (cursor != null) {
				while (cursor.moveToNext()) {
					MvInfo bean = cursor2bean(cursor);
					File f = new File(bean.getPath());
					if (!f.exists()) {
						deletes.add(bean);
						continue;
					}
					results.add(bean);
				}
				cursor.close();
			}

			if (!deletes.isEmpty()) {
				StringBuilder sql = new StringBuilder("DELETE FROM " + TABLE + " WHERE ");
				for (MvInfo i : deletes) {
					sql.append("(`").append(KEY_USER_ID).append("`=").append(i.userId).append(" AND `").append(KEY_TITLE).append("`='").append(i.title).append("' AND `").append(KEY_TYPE).append("`=").append(i.type).append(") OR ");
				}
				sql.delete(sql.length() - 4, sql.length());
				ELog.i("Delete:" + sql.toString());
				db.execSQL(sql.toString());
			}
		}
		finally {
			lock.unlock();
		}
		return results;
	}

	/**
	 * 查询某天拍摄的mv个数
	 * 
	 * @param userId
	 * @param date
	 * @param helper
	 * @return
	 */
	public static List<MvInfo> query(int userId, Calendar date, DatabaseHelper helper) {
		ArrayList<MvInfo> results = new ArrayList<MvInfo>();
		LinkedList<MvInfo> deletes = new LinkedList<MvInfo>();

		Calendar tempTime = Calendar.getInstance();
		long sTime = 0, eTime = 0;

		tempTime.set(date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH), 0, 0);
		tempTime.set(Calendar.MILLISECOND, 0);
		sTime = tempTime.getTime().getTime() / 1000;
		tempTime.add(Calendar.DAY_OF_MONTH, 1);
		eTime = tempTime.getTime().getTime() / 1000;

		try {
			lock.lock();
			
			SQLiteDatabase db = helper.getReadableDatabase();
			String selection = KEY_USER_ID + "=" + userId + " AND " + KEY_DATE + ">" + sTime + " AND " + KEY_DATE + "<" + eTime;
			Cursor cursor = db.query(TABLE, null, selection, null, null, null, KEY_DATE + " ASC");
			if (cursor != null) {
				while (cursor.moveToNext()) {
					MvInfo bean = cursor2bean(cursor);
					if (bean.type == TYPE_PUBLISHED || bean.type == TYPE_UNPUBLISHED) {
						File f = new File(bean.getPath());// 判断本地文件是否删除了，如果删除了，就删除数据库对应的数据
						if (!f.exists()) {
							deletes.add(bean);
							continue;
						}
					}
					results.add(bean);
				}
				cursor.close();
			}

			if (!deletes.isEmpty()) {
				StringBuilder sql = new StringBuilder("DELETE FROM " + TABLE + " WHERE ");
				for (MvInfo i : deletes) {
					sql.append("(`").append(KEY_USER_ID).append("`=").append(i.userId).append(" AND `").append(KEY_TITLE).append("`='").append(i.title).append("' AND `").append(KEY_TYPE).append("`=").append(i.type).append(") OR ");
				}
				sql.delete(sql.length() - 4, sql.length());
				ELog.w("Delete:" + sql.toString());
				db.execSQL(sql.toString());
			}
		}
		finally {
			lock.unlock();
		}
		return results;
	}

	public static MvInfo query(int userId, String title, int type, DatabaseHelper helper) {
		MvInfo info = null;
		try {
			lock.lock();

			SQLiteDatabase db = helper.getReadableDatabase();
			Cursor cursor = db.query(TABLE, null, KEY_USER_ID + "=" + userId + " AND " + KEY_TITLE + "='" + title + "' AND " + KEY_TYPE + "=" + type, null, null, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				info = cursor2bean(cursor);
				cursor.close();
			}
		}
		finally {
			lock.unlock();
		}
		return info;
	}

	public static boolean contain(int userId, String title, DatabaseHelper helper) {
		boolean contain = false;
		try {
			lock.lock();

			SQLiteDatabase db = helper.getReadableDatabase();
			Cursor cursor = db.query(TABLE, null, KEY_USER_ID + "=" + userId + " AND " + KEY_TITLE + "='" + title + "'", null, null, null, null);
			if (cursor != null && cursor.getCount() > 0) {
				contain = true;
				cursor.close();
			}
		}
		finally {
			lock.unlock();
		}
		return contain;
	}

	public static void insert(int userId, String title, int prefixId, int type, long date, DatabaseHelper helper) {
		try {
			lock.lock();

			ELog.w("Title:" + title + " Prefix:" + prefixId + " Type:" + type + " Date:" + date);
			ContentValues values = new ContentValues();
			values.put(KEY_USER_ID, userId);
			values.put(KEY_TITLE, title);
			values.put(KEY_PREFIX_ID, prefixId);
			values.put(KEY_TYPE, type);
			values.put(KEY_DATE, date / 1000);
			SQLiteDatabase db = helper.getWritableDatabase();
			db.delete(TABLE, "`" + KEY_TITLE + "` = '" + title + "' AND `" + KEY_TYPE + "` = " + type, null);
			db.insert(TABLE, null, values);
		}
		finally {
			lock.unlock();
		}
	}

	public static void insert(MvInfo data, DatabaseHelper helper) {
		try {
			lock.lock();

			ELog.w("Title:" + data.title + " Prefix:" + data.prefix.id + " Type:" + data.type + " Date:" + App.YYYY_MM_DD_HH_MM_FORMAT.format(data.createAt.getTime()));
			ContentValues values = new ContentValues();
			values.put(KEY_USER_ID, data.userId);
			values.put(KEY_TITLE, data.title);
			values.put(KEY_PREFIX_ID, data.prefix.id);
			values.put(KEY_TYPE, data.type);
			values.put(KEY_DATE, (int)(data.createAt.getTimeInMillis() / 1000));
			values.put(KEY_SERVER_ID, data.serverId);
			values.put(KEY_SERVER_URL, data.serverUrl);
			
			SQLiteDatabase db = helper.getWritableDatabase();
			db.delete(TABLE, "`" + KEY_TITLE + "` = '" + data.title + "' AND `" + KEY_TYPE + "` = " + data.type, null);
			db.insert(TABLE, null, values);
		}
		finally {
			lock.unlock();
		}
	}
	
	public static void update(MvInfo data, DatabaseHelper helper) {
		try {
			lock.lock();
			
			ELog.w("Title:" + data.title + " Prefix:" + data.prefix.id + " Type:" + data.type + " Date:" + App.YYYY_MM_DD_HH_MM_FORMAT.format(data.createAt.getTime()));

			ContentValues values = new ContentValues();
			values.put(KEY_USER_ID, data.userId);
			values.put(KEY_TITLE, data.title);
			values.put(KEY_PREFIX_ID, data.prefix.id);
			values.put(KEY_DATE, (int)(data.createAt.getTimeInMillis() / 1000));
			values.put(KEY_SERVER_ID, data.serverId);
			
			SQLiteDatabase db = helper.getWritableDatabase();
			db.update(TABLE, values, "`" + KEY_USER_ID + "`=" + data.userId + " AND `" + KEY_TITLE + "`='" + data.title + "' AND `" + KEY_TYPE + "`=" + data.type, null);
		}
		finally {
			lock.unlock();
		}
	}

	public static void update(int userId, String title, int oldType, int newType, DatabaseHelper helper) {
		try {
			lock.lock();

			ELog.w("Uid:" + userId + " Title:" + title + " Type:" + oldType + "/" + newType);
			String sql = "UPDATE " + TABLE + " SET `" + KEY_TYPE + "` = " + newType + " WHERE `" + KEY_USER_ID + "`=" + userId + " AND `" + KEY_TITLE + "`='" + title + "' AND `" + KEY_TYPE + "`=" + oldType;
			SQLiteDatabase db = helper.getWritableDatabase();
			db.execSQL(sql);
		}
		finally {
			lock.unlock();
		}
	}

	public static void update(int userId, String title, int serverId, String serverUrl, DatabaseHelper helper) {
		try {
			lock.lock();

			ELog.w("Uid:" + userId + " Title:" + title + " Server:" + serverId + " " + serverUrl);
			String sql = "UPDATE " + TABLE + " SET `" + KEY_SERVER_ID + "`=" + serverId + ", `" + KEY_SERVER_URL + "`='" + serverUrl + "' WHERE `" + KEY_USER_ID + "`=" + userId + " AND `" + KEY_TITLE + "`='" + title + "'";
			SQLiteDatabase db = helper.getWritableDatabase();
			db.execSQL(sql);
		}
		finally {
			lock.unlock();
		}
	}

	public static void delete(int userId, String title, int type, DatabaseHelper helper) {
		try {
			lock.lock();

			ELog.w("Uid:" + userId + " Title:" + title + " Type:" + type);
			SQLiteDatabase db = helper.getWritableDatabase();
			db.delete(TABLE, "`" + KEY_USER_ID + "`=" + userId + " AND `" + KEY_TITLE + "`='" + title + "' AND `" + KEY_TYPE + "`=" + type, null);
		}
		finally {
			lock.unlock();
		}
	}

	public static MvInfo cursor2bean(Cursor cursor) {
		Calendar createAt = Calendar.getInstance();
		createAt.setTimeInMillis((long)cursor.getInt(cursor.getColumnIndex(KEY_DATE)) * 1000);
		MvInfo bean = new MvInfo(cursor.getInt(cursor.getColumnIndex(KEY_USER_ID)), cursor.getString(cursor.getColumnIndex(KEY_TITLE)), cursor.getInt(cursor.getColumnIndex(KEY_PREFIX_ID)), cursor.getInt(cursor.getColumnIndex(KEY_TYPE)), createAt);
		bean.serverId = cursor.getInt(cursor.getColumnIndex(KEY_SERVER_ID));
		bean.serverUrl = cursor.getString(cursor.getColumnIndex(KEY_SERVER_URL));
		return bean;
	}

	@Override
	public MessageType getShareType() {
		return MessageType.TEXT_IMAGE;
	}

	@Override
	public int getActionType() {
		return App.INT_UNSET;
	}

	@Override
	public String getShareTitle() {
		return "【偶们】" + title;
	}

	@Override
	public String getShareContent() {
		return SHARE_CONTENT;
	}

	@Override
	public String getShareLinkUrl() {
		return serverUrl;
	}

	@Override
	public String getShareImageUrl() {
		return prefix.getCoverUrl();
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof MvInfo) {
			MvInfo target = (MvInfo) o;
			return userId == target.userId && title != null && title.equals(target.title);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public String toString() {
		return userId + "," + title + "," + type;
	}

	public MvInfo(Parcel in) {
		userId = in.readInt();
		title = in.readString();
		prefix = (PrefixVideo) in.readParcelable(null);
		type = in.readInt();
		createAt = (Calendar) in.readSerializable();
		serverId = in.readInt();
		serverUrl = in.readString();
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(userId);
		dest.writeString(title);
		dest.writeParcelable(prefix, flags);
		dest.writeInt(type);
		dest.writeSerializable(createAt);
		dest.writeInt(serverId);
		dest.writeString(serverUrl);
	}

	public final Parcelable.Creator<MvInfo> CREATOR = new Parcelable.Creator<MvInfo>() {
		public MvInfo createFromParcel(Parcel in) {
			return new MvInfo(in);
		}

		public MvInfo[] newArray(int size) {
			return new MvInfo[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}
}
