package com.oumen.message;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.oumen.activity.detail.HuodongDetailHeaderProvider;
import com.oumen.activity.message.DetailActivityMessage;
import com.oumen.activity.message.UserActivityMessage;
import com.oumen.android.App;
import com.oumen.db.DatabaseHelper;
import com.oumen.tools.ELog;

public class ActivityMessage implements Parcelable, HuodongDetailHeaderProvider {
	public static final String TABLE = "activity";
	
	public static final int FROM_SOCKET = 0;
	public static final int FROM_HTTP = 1;
	
	public static final int SCOPE_QUAN_GUO = 1;
	
	public static final String KEY_ID = "id";
	public static final String KEY_TITLE = "title";
	public static final String KEY_DESCRIPTION = "description";
	public static final String KEY_ADDRESS = "address";
	public static final String KEY_START = "start_time";
	public static final String KEY_PIC_URL = "pic_url";
	public static final String KEY_OWNER_ID = "owner_id";
	public static final String KEY_OWNER_NAME = "owner_name";
	public static final String KEY_OWNER_PHOTO_URL = "owner_photo_url";
	public static final String KEY_TIMESTAMP = "create_at";
	public static final String KEY_SCOPE = "scope";
	public static final String KEY_DELETE = "is_delete";
	public static final String KEY_PUSH = "push";
	public static final String KEY_LOOKNUM = "looknum"; 
	
	protected int id;
	protected String title;
	protected String description;
	protected String address;
	protected String picUrl;
	protected int multiId;
	protected int ownerId;
	protected String ownerName;
	protected String ownerPhotoUrl;
	protected Date startTime;
	protected Date timestamp;
	protected int scope;
	protected boolean push;
	protected boolean read;
	protected boolean delete;
	protected String lookNum = "0";//新增有多少人查看(人数太多会出现中文)
	
	protected ArrayList<ActivityMember> members = new ArrayList<ActivityMember>();
	
	public ActivityMessage() {}
	
	public ActivityMessage(DetailActivityMessage bean){
		id = bean.getId();
		title = bean.getName();
		description = bean.getDescription();
		address = bean.getAddress();
		picUrl = bean.getPicSourceUrl();
		multiId = bean.getTeamId();
		ownerId = bean.getSenderUid();
		ownerName = bean.getSenderName();
		ownerPhotoUrl = bean.getSenderPic();
		try {
			startTime = App.YYYY_MM_DD_HH_MM_FORMAT.parse(bean.getStartTime());
			timestamp = App.YYYY_MM_DD_HH_MM_FORMAT.parse(bean.getStartTime());
		}
		catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	public ActivityMessage(UserActivityMessage bean) {
		id = bean.getActivityId();
		title = bean.getName();
		address = bean.getAddress();
		picUrl = bean.getPicUrl();
		multiId = bean.getTeamId();
		try {
			startTime = App.YYYY_MM_DD_HH_MM_FORMAT.parse(bean.getStartTime());
			timestamp = App.YYYY_MM_DD_HH_MM_FORMAT.parse(bean.getStartTime());
		}
		catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	public ActivityMessage(JSONObject json, int from) throws NumberFormatException, JSONException {
		if (from == FROM_SOCKET) {
			Object tmp = json.opt("atid");
			if (tmp instanceof String)
				id = Integer.parseInt((String) tmp);
			else if (tmp instanceof Integer)
				id = (Integer) tmp;
			
			tmp = json.opt("dis");
			description = tmp instanceof String ? (String) tmp : "";
			
			tmp = json.opt("address");
			address = tmp instanceof String ? (String) tmp : "";

			tmp = json.opt("pic");
			picUrl = tmp instanceof String ? (String) tmp : null;
			
			tmp = json.opt("isquanguo");
			if (tmp instanceof String)
				scope = Integer.parseInt((String) tmp);
			else if (tmp instanceof Integer)
				scope = (Integer) tmp;
			
			title = json.getString("actname");
			startTime = new Date(json.getLong("starttime"));
			timestamp = new Date(json.has("timestamp") ? json.getLong("timestamp") : System.currentTimeMillis());
			
			ownerId = json.optInt("uid");
			ownerName = json.optString("nickname");
			ownerPhotoUrl = json.optString("head_photo");
			if (json.has("teamid")) {
				tmp = json.opt("teamid");
				if (tmp == null || JSONObject.NULL.equals(tmp) || TextUtils.isEmpty(tmp.toString())) {
					multiId = App.INT_UNSET;
				}
				else {
					multiId = Integer.parseInt((String) tmp);
				}
			}
			else {
				multiId = App.INT_UNSET;
			}
			push = true;
			
			if (json.has("looknum")) {
				tmp = json.opt("looknum");
				if (tmp == null || JSONObject.NULL.equals(tmp) || TextUtils.isEmpty(tmp.toString())) {
					lookNum = String.valueOf(0);
				}
				else {
					if (tmp instanceof Integer) {
						lookNum = String.valueOf((Integer) tmp);
					}
					else if (tmp instanceof String) {
						lookNum = (String) tmp;
					}
				}
			}
		}
		else if (from == FROM_HTTP) {
			Object tmp = json.opt("atid");
			if (tmp instanceof String)
				id = Integer.parseInt((String) tmp);
			else if (tmp instanceof Integer)
				id = (Integer) tmp;
			
			tmp = json.opt("dis");
			description = tmp instanceof String ? (String) tmp : "";
			
			tmp = json.opt("address");
			address = tmp instanceof String ? (String) tmp : "";

			tmp = json.opt("pic");
			picUrl = tmp instanceof String ? (String) tmp : null;
			
			tmp = json.opt("isquanguo");
			if (tmp instanceof String)
				scope = Integer.parseInt((String) tmp);
			else if (tmp instanceof Integer)
				scope = (Integer) tmp;
			
			title = json.getString("actname");
			timestamp = new Date(Long.parseLong(json.getString("addtime")) * 1000);
			startTime = new Date(json.getLong("starttimestamp"));
			
			JSONObject ownerJson = json.getJSONObject("sender");
			ownerId = ownerJson.getInt("uid");
			ownerName = ownerJson.getString("nickname");
			ownerPhotoUrl = ownerJson.getString("head_photo");
			
			String team = json.optString("teamid");
			if (TextUtils.isEmpty(team)) {
				multiId = App.INT_UNSET;
			}
			else {
				multiId = Integer.parseInt(team);
			}
			
			JSONArray array = json.getJSONArray("users");
			members = new ArrayList<ActivityMember>();
			for (int i = 0; i < array.length(); i++) {
				members.add(new ActivityMember(array.getJSONObject(i)));
			}
			push = false;
			
			if (json.has("looknum")) {
				tmp = json.opt("looknum");
				if (tmp == null || JSONObject.NULL.equals(tmp) || TextUtils.isEmpty(tmp.toString())) {
					lookNum = String.valueOf(0);
				}
				else {
					if (tmp instanceof Integer) {
						lookNum = String.valueOf((Integer) tmp);
					}
					else if (tmp instanceof String) {
						lookNum = (String) tmp;
					}
				}
			}
		}
		read = false;
		delete = false;
	}
	
	public static List<ActivityMessage> query(int selfUid, DatabaseHelper helper) {
		LinkedList<ActivityMessage> results = new LinkedList<ActivityMessage>();
		synchronized (helper) {
			SQLiteDatabase db = helper.getReadableDatabase();
			String sql = DatabaseHelper.KEY_SELF_UID + "=" + selfUid;
			Cursor cursor = db.query(TABLE, null, sql, null, null, null, KEY_TIMESTAMP + " DESC");
			if (cursor != null) {
				while (cursor.moveToNext()) {
					results.add(cursor2bean(cursor));
				}
				cursor.close();
			}
		}
		return results;
	}
	
	public static List<ActivityMessage> query(int selfUid, boolean isDelete, boolean isPush, DatabaseHelper helper) {
		LinkedList<ActivityMessage> results = new LinkedList<ActivityMessage>();
		synchronized (helper) {
			SQLiteDatabase db = helper.getReadableDatabase();
			String sql = DatabaseHelper.KEY_SELF_UID + "=" + selfUid + " AND " + KEY_DELETE + "=" + (isDelete ? 1 : 0) + " AND " + KEY_PUSH + "=" + (isPush ? 1 : 0);
			Cursor cursor = db.query(TABLE, null, sql, null, null, null, KEY_TIMESTAMP + " ASC");
			if (cursor != null) {
				while (cursor.moveToNext()) {
					results.add(cursor2bean(cursor));
				}
				cursor.close();
			}
		}
		return results;
	}
	
	public static ActivityMessage query(int selfUid, int id, DatabaseHelper helper) {
		ActivityMessage result = null;
		synchronized (helper) {
			SQLiteDatabase db = helper.getReadableDatabase();
			String sql = DatabaseHelper.KEY_SELF_UID + "=" + selfUid + " AND " + KEY_ID + "=" + id;
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
	
	public static ActivityMessage queryLatest(int selfUid, DatabaseHelper helper) {
		ActivityMessage result = null;
		synchronized (helper) {
			SQLiteDatabase db = helper.getReadableDatabase();
			String sql = DatabaseHelper.KEY_SELF_UID + "=" + selfUid + " AND " + KEY_PUSH + "=1";
			Cursor cursor = db.query(TABLE, null, sql, null, null, null, KEY_TIMESTAMP + " DESC");
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
			String sql = DatabaseHelper.KEY_SELF_UID + "=" + selfUid + " AND " + KEY_PUSH + "=1" + " AND " + BaseMessage.KEY_READ + "=0";
			Cursor cursor = db.query(TABLE, null, sql, null, null, null, null);
			if (cursor != null) {
				count = cursor.getCount();
			}
			cursor.close();
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
	
	public static void insert(int selfUid, ActivityMessage bean, DatabaseHelper helper) {
		if (bean.multiId <= 0) {
			ELog.w("MultiId is null ActivityId:" + bean.id + " Title:" + bean.title);
			return;
		}

		ELog.w("Activity:" + bean.id + " Self:" + selfUid + " Time:" + bean.startTime.getTime());
		SQLiteDatabase db1 = helper.getReadableDatabase();
		Cursor cursor1 = db1.query(TABLE, null, DatabaseHelper.KEY_SELF_UID + "=" + selfUid + " AND " + KEY_ID + "=" + bean.getId() + " AND " + KEY_TIMESTAMP + "=" + bean.getTimestamp().getTime(), null, null, null, null);
		if (cursor1.getCount() == 0) {//没有此活动,需要入库
			synchronized (helper) {
				SQLiteDatabase db = helper.getReadableDatabase();
				Cursor cursor = db.query(TABLE, null, DatabaseHelper.KEY_SELF_UID + "=" + selfUid + " AND " + KEY_ID + "=" + bean.id, null, null, null, null);
				if (cursor != null && cursor.getCount() > 0) {
					if (cursor.moveToFirst()) {
						boolean push = cursor.getInt(cursor.getColumnIndex(KEY_PUSH)) == 1 ? true : false;
						if (!push && bean.push) {
							String sql = "UPDATE " + TABLE + " SET " + KEY_PUSH + "=1" + " WHERE " + DatabaseHelper.KEY_SELF_UID + "=" + selfUid + " AND " + KEY_ID + "=" + bean.id;
							db.execSQL(sql);
							return;
						}
					}
				}
			}
			ContentValues values = new ContentValues();
			values.put(KEY_ID, bean.id);
			values.put(KEY_TITLE, bean.title);
			values.put(KEY_DESCRIPTION, bean.description);
			values.put(KEY_ADDRESS, bean.address);
			values.put(KEY_START, bean.startTime.getTime());
			values.put(KEY_PIC_URL, bean.picUrl);
			values.put(KEY_OWNER_ID, bean.ownerId);
			values.put(KEY_OWNER_NAME, bean.ownerName);
			values.put(KEY_OWNER_PHOTO_URL, bean.ownerPhotoUrl);
			values.put(KEY_TIMESTAMP, bean.timestamp.getTime());
			values.put(KEY_SCOPE, bean.scope);
			values.put(KEY_PUSH, bean.push ? 1 : 0);
			values.put(DatabaseHelper.KEY_SELF_UID, selfUid);
			values.put(KEY_LOOKNUM, bean.lookNum);
			synchronized (helper) {
				SQLiteDatabase db = helper.getWritableDatabase();
				db.insert(TABLE, null, values);
			}
		}
		else {
			update(selfUid, bean, helper);
		}
		
	}
	
	public static void update(int selfUid, ActivityMessage bean, DatabaseHelper helper) {
		synchronized (helper) {
			SQLiteDatabase db = helper.getWritableDatabase();
			
			ContentValues values = new ContentValues();
			values.put(KEY_TITLE, bean.title);
			values.put(KEY_DESCRIPTION, bean.description);
			values.put(KEY_ADDRESS, bean.address);
			values.put(KEY_START, bean.startTime.getTime());
			values.put(KEY_PIC_URL, bean.picUrl);
			values.put(KEY_OWNER_ID, bean.ownerId);
			values.put(KEY_OWNER_NAME, bean.ownerName);
			values.put(KEY_OWNER_PHOTO_URL, bean.ownerPhotoUrl);
			values.put(KEY_TIMESTAMP, bean.timestamp.getTime());
			values.put(KEY_SCOPE, bean.scope);
			values.put(KEY_PUSH, bean.push ? 1 : 0);
			values.put(KEY_LOOKNUM, bean.lookNum);
			values.put(BaseMessage.KEY_READ, bean.read);
			
			db.update(TABLE, values, DatabaseHelper.KEY_SELF_UID + "=" + selfUid + " AND "+ KEY_ID + "=" + bean.getId() + " AND " + KEY_TIMESTAMP + "=" + bean.getTimestamp().getTime(), null);
		}
	}
	
	public static void updateTimestamp(int selfUid, int id, long timestamp, boolean isDelete, DatabaseHelper helper) {
		String sql = "UPDATE " + TABLE + " SET " + KEY_TIMESTAMP + "=" + timestamp + "," + KEY_DELETE + "=" + (isDelete ? 1 : 0)
				+ " WHERE " + KEY_ID + "=" + id + " AND " + DatabaseHelper.KEY_SELF_UID + "=" + selfUid;
		synchronized (helper) {
			SQLiteDatabase db = helper.getWritableDatabase();
			db.execSQL(sql);
		}
	}
	
	public static void updateTimestamp(int selfUid, int id, long timestamp, DatabaseHelper helper) {
		String sql = "UPDATE " + TABLE + " SET " + KEY_TIMESTAMP + "=" + timestamp
				+ " WHERE " + KEY_ID + "=" + id + " AND " + DatabaseHelper.KEY_SELF_UID + "=" + selfUid;
		synchronized (helper) {
			SQLiteDatabase db = helper.getWritableDatabase();
			db.execSQL(sql);
		}
	}
	
	public static ActivityMessage cursor2bean(Cursor cursor) {
		ActivityMessage bean = new ActivityMessage();
		bean.id = cursor.getInt(cursor.getColumnIndex(KEY_ID));
		bean.title = cursor.getString(cursor.getColumnIndex(KEY_TITLE));
		bean.description = cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION));
		bean.address = cursor.getString(cursor.getColumnIndex(KEY_ADDRESS));
		bean.startTime = new Date(cursor.getLong(cursor.getColumnIndex(KEY_START)));
		bean.picUrl = cursor.getString(cursor.getColumnIndex(KEY_PIC_URL));
		bean.ownerId = cursor.getInt(cursor.getColumnIndex(KEY_OWNER_ID));
		bean.ownerName = cursor.getString(cursor.getColumnIndex(KEY_OWNER_NAME));
		bean.ownerPhotoUrl = cursor.getString(cursor.getColumnIndex(KEY_OWNER_PHOTO_URL));
		bean.timestamp = new Date(cursor.getLong(cursor.getColumnIndex(KEY_TIMESTAMP)));
		bean.scope = cursor.getInt(cursor.getColumnIndex(KEY_SCOPE));
		bean.read = cursor.getInt(cursor.getColumnIndex(BaseMessage.KEY_READ)) == 1 ? true : false;
		bean.delete = cursor.getInt(cursor.getColumnIndex(KEY_DELETE)) == 1 ? true : false;
		bean.lookNum = cursor.getString(cursor.getColumnIndex(KEY_LOOKNUM));
		return bean;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(id);
		dest.writeString(title);
		dest.writeString(description);
		dest.writeString(address);
		dest.writeLong(startTime.getTime());
		dest.writeString(picUrl);
		dest.writeInt(multiId);
		dest.writeInt(ownerId);
		dest.writeString(ownerName);
		dest.writeString(ownerPhotoUrl);
		dest.writeLong(timestamp.getTime());
		dest.writeInt(scope);
		dest.writeInt(read ? 1 : 0);
		dest.writeInt(delete ? 1 : 0);
		dest.writeTypedList(members);
		dest.writeString(lookNum);
	}

	public static final Parcelable.Creator<ActivityMessage> CREATOR = new Parcelable.Creator<ActivityMessage>() {
		public ActivityMessage createFromParcel(Parcel in) {
			ActivityMessage bean = new ActivityMessage();
			bean.id = in.readInt();
			bean.title = in.readString();
			bean.description = in.readString();
			bean.address = in.readString();
			bean.startTime = new Date(in.readLong());
			bean.picUrl = in.readString();
			bean.multiId = in.readInt();
			bean.ownerId = in.readInt();
			bean.ownerName = in.readString();
			bean.ownerPhotoUrl = in.readString();
			bean.timestamp = new Date(in.readLong());
			bean.scope = in.readInt();
			bean.read = in.readInt() == 1 ? true : false;
			bean.delete = in.readInt() == 1 ? true : false;
			in.readTypedList(bean.members, ActivityMember.CREATOR);
			bean.lookNum = in.readString();
			return bean;
		}

		public ActivityMessage[] newArray(int size) {
			return new ActivityMessage[size];
		}
	};

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public String getPicUrl() {
		return picUrl;
	}

	public void setPicUrl(String picUrl) {
		this.picUrl = picUrl;
	}

	public int getMultiId() {
		return multiId;
	}

	public void setMultiId(int multiId) {
		this.multiId = multiId;
	}

	public int getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(int ownerId) {
		this.ownerId = ownerId;
	}

	public String getOwnerName() {
		return ownerName;
	}

	public void setOwnerName(String ownerName) {
		this.ownerName = ownerName;
	}

	public String getOwnerPhotoUrl() {
		return ownerPhotoUrl;
	}

	public void setOwnerPhotoUrl(String ownerPhotoUrl) {
		this.ownerPhotoUrl = ownerPhotoUrl;
	}

	public ArrayList<ActivityMember> getMembers() {
		return members;
	}

	public void setMembers(ArrayList<ActivityMember> members) {
		this.members = members;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public int getScope() {
		return scope;
	}

	public void setScope(int scope) {
		this.scope = scope;
	}

	public boolean isRead() {
		return read;
	}

	public void setRead(boolean read) {
		this.read = read;
	}

	public boolean isDelete() {
		return delete;
	}

	public void setDelete(boolean delete) {
		this.delete = delete;
	}

	@Override
	public String getHuodongSenderName() {
		return ownerName;
	}

	@Override
	public String getHuodongSenderPhoto() {
		return ownerPhotoUrl;
	}

	@Override
	public String getHuodongTitle() {
		return title;
	}

	@Override
	public String getHuodongAddress() {
		return address;
	}

	@Override
	public String getHuodongTime() {
		return App.MM_DD_FORMAT.format(startTime);
	}

	@Override
	public String getLookNum() {
		return lookNum;
	}

	@Override
	public boolean getHot() {
		//TODO 没有处理
		return false;
	}

	@Override
	public boolean getTui() {
		//TODO 没有处理
		return false;
	}

	@Override
	public int getHuodongSendId() {
		return ownerId;
	}

	@Override
	public int getHuodongMultiId() {
		return multiId;
	}
	
	@Override
	public String getHuodongPic() {
		return picUrl;
	}

	@Override
	public String getHuodongPic(int lenght) {
		return picUrl + "/small?l=" + lenght;
	}
	
	@Override
	public ArrayList<String> getHuodongPics() {
		//TODO 此处待定
		ArrayList<String> lists = new ArrayList<String>();
		lists.add(picUrl);
		return lists;
	}

	@Override
	public ArrayList<String> getHuodongPics(int lenght) {
		//TODO 此处待定
		ArrayList<String> lists = new ArrayList<String>();
		lists.add(picUrl + "/small?l=" + lenght);
		return lists;
	}

	@Override
	public boolean ishuodongFinish() {
		return false;
	}
}
