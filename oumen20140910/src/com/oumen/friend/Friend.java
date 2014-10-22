package com.oumen.friend;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.oumen.android.App;
import com.oumen.android.BasicUserInfo;
import com.oumen.db.DatabaseHelper;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

public class Friend implements BasicUserInfo, Serializable{
	private static final long serialVersionUID = -601021735444213193L;
	
	public static final String TABLE = "friends";
	
	public static final String KEY_ID = "uid";
	public static final String KEY_NICKNAME = "nickname";
	public static final String KEY_PHOTOURL = "photo_url";
	public static final String KEY_DESCRIPTION = "description";
	public static final String KEY_BABY_TYPE = "baby_type";
	public static final String KEY_GRAVIDITY = "gravidity";
	public static final String KEY_BIRTHDAY = "birthday";
	public static final String KEY_GENDER = "gender";
	public static final String KEY_ADDRESS = "address";
	public static final String KEY_NUMBER = "number";
	
	//uid（用户id）,name(昵称),message（签名）,mugshot（头像）,state（状态）,sex（性别）,address（地址）,omnumber（偶们号）
	protected int uid;
	protected String nickname;
	protected String description;
	protected String photoUrl;
	protected int babyType;
	protected String gravidity;
	protected String birthday;
	protected int gender;
	protected String address;
	protected int number;

	public Friend() {}
	
	public Friend(Cursor cursor) {
		uid = cursor.getInt(cursor.getColumnIndex(KEY_ID));
		nickname = cursor.getString(cursor.getColumnIndex(KEY_NICKNAME));
		photoUrl = cursor.getString(cursor.getColumnIndex(KEY_PHOTOURL));
		description = cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION));
		babyType = cursor.getInt(cursor.getColumnIndex(KEY_BABY_TYPE));
		gravidity = cursor.getString(cursor.getColumnIndex(KEY_GRAVIDITY));
		birthday = cursor.getString(cursor.getColumnIndex(KEY_BIRTHDAY));
		gender = cursor.getInt(cursor.getColumnIndex(KEY_GENDER));
		address = cursor.getString(cursor.getColumnIndex(KEY_ADDRESS));
		number = cursor.getInt(cursor.getColumnIndex(KEY_NUMBER));
	}
	
	public Friend(JSONObject obj) throws JSONException {
		if (TextUtils.isEmpty(obj.getString("uid"))) {
			return ;
		}
		uid = Integer.parseInt(obj.getString("uid"));
		nickname = obj.getString("username");
		description = obj.getString("manifesto");
		photoUrl = obj.getString("head_photo");
		babyType = Integer.parseInt(obj.getString("babytype"));
		gravidity = obj.getString("gravidity");
		birthday = obj.getString("birthday");
		gender = TextUtils.isEmpty(obj.getString("sex")) ? 0 : Integer.parseInt(obj.getString("sex"));
		address = obj.getString("address");
		number = Integer.parseInt(obj.getString("omnumber"));
	}

	@Override
	public int getUid() {
		return uid;
	}

	@Override
	public void setUid(int uid) {
		this.uid = uid;
	}

	@Override
	public String getNickname() {
		return nickname;
	}

	@Override
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	
	@Override
	public boolean hasPhoto() {
		return !TextUtils.isEmpty(photoUrl);
	}

	@Override
	public String getPhotoSourceUrl() {
		return photoUrl;
	}

	@Override
	public String getPhotoUrl(int maxLength) {
		return App.getSmallPicUrl(photoUrl, maxLength);
	}

	@Override
	public void setPhotoUrl(String photoUrl) {
		this.photoUrl = photoUrl;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getBabyType() {
		return babyType;
	}

	public void setBabyType(int babyType) {
		this.babyType = babyType;
	}

	public String getGravidity() {
		return gravidity;
	}

	public void setGravidity(String gravidity) {
		this.gravidity = gravidity;
	}

	public String getBirthday() {
		return birthday;
	}

	public void setBirthday(String birthday) {
		this.birthday = birthday;
	}

	public int getGender() {
		return gender;
	}

	public void setGender(int gender) {
		this.gender = gender;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public static void insert(Friend bean, int selfUid, DatabaseHelper helper) {
		synchronized (helper) {
			SQLiteDatabase db = helper.getWritableDatabase();
			Cursor cursor = db.query(TABLE, null, "self_uid=" + selfUid + " AND uid=" + bean.uid, null, null, null, null);
			if (cursor.getCount() == 0) {
				ContentValues values = new ContentValues();
				values.put("self_uid", selfUid);
				values.put(KEY_ID, bean.uid);
				values.put(KEY_NICKNAME, bean.nickname);
				values.put(KEY_PHOTOURL, bean.photoUrl);
				values.put(KEY_DESCRIPTION, bean.description);
				values.put(KEY_BABY_TYPE, bean.babyType);
				values.put(KEY_GRAVIDITY, bean.gravidity);
				values.put(KEY_BIRTHDAY, bean.birthday);
				values.put(KEY_GENDER, bean.gender);
				values.put(KEY_ADDRESS, bean.address);
				values.put(KEY_NUMBER, bean.number);
				db.insert(TABLE, null, values);
			}
			else {
				update(selfUid, bean, App.DB);
			}
		}
	}

	public static void insert(Collection<Friend> beans, int selfUid, DatabaseHelper helper) {
		for (Friend bean : beans) {
			insert(bean, selfUid, helper);
		}
	}

	public static List<Friend> query(int selfUid, DatabaseHelper helper) {
		List<Friend> results = new LinkedList<Friend>();
		synchronized (helper) {
			SQLiteDatabase db = helper.getReadableDatabase();
			Cursor cursor = db.query(TABLE, null, "self_uid=" + selfUid, null, null, null, null);
			while (cursor.moveToNext()) {
				Friend bean = new Friend(cursor);
				results.add(bean);
			}
			cursor.close();
		}
		return results;
	}
	
	public static Friend query(int selfUid, int targetUid, DatabaseHelper helper) {
		Friend bean = null;
		synchronized (helper) {
			SQLiteDatabase db = helper.getReadableDatabase();
			Cursor cursor = db.query(TABLE, null, "self_uid=" + selfUid + " AND uid=" + targetUid, null, null, null, null);
			while (cursor.moveToNext()) {
				bean = new Friend(cursor);
				break;
			}
			cursor.close();
		}
		return bean;
	}
	/**
	 * 更新某一条数据
	 * @param selfUid
	 * @param friend
	 * @param helper
	 */
	public static void update(int selfUid, Friend friend, DatabaseHelper helper) {
		synchronized (helper) {
			SQLiteDatabase db = helper.getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put(KEY_NICKNAME, friend.nickname);
			values.put(KEY_PHOTOURL, friend.photoUrl);
			values.put(KEY_DESCRIPTION, friend.description);
			values.put(KEY_BABY_TYPE, friend.babyType);
			values.put(KEY_GRAVIDITY, friend.gravidity);
			values.put(KEY_BIRTHDAY, friend.birthday);
			values.put(KEY_GENDER, friend.gender);
			values.put(KEY_ADDRESS, friend.address);
			values.put(KEY_NUMBER, friend.number);
			db.update(TABLE, values, "self_uid=" + selfUid + " AND uid=" + friend.getUid(), null);
		}
	}
	
	public static int delete(int selfUid, int targetUid, DatabaseHelper helper) {
		int results = 0;
		synchronized (helper) {
			SQLiteDatabase db = helper.getWritableDatabase();
			results = db.delete(TABLE, "self_uid=" + selfUid + " AND uid=" + targetUid, null);
		}
		return results;
	}

	public static int deleteAll(int selfUid, DatabaseHelper helper) {
		int results = 0;
		synchronized (helper) {
			SQLiteDatabase db = helper.getWritableDatabase();
			results = db.delete(TABLE, "self_uid=" + selfUid, null);
		}
		return results;
	}
}
