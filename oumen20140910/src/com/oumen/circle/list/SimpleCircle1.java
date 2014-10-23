package com.oumen.circle.list;

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

import com.oumen.android.App;
import com.oumen.android.peers.entity.Comment;
import com.oumen.db.DatabaseHelper;
import com.oumen.tools.ELog;

public class SimpleCircle1 implements Parcelable {
	public static final String TABLE = "circle_cache";
	public static final String KEY_JSON = "json";
	
	protected int id;
	protected JSONObject reference;
	
	private SimpleCircle1() {}

	public SimpleCircle1(JSONObject obj) throws JSONException {
		Object tmp = obj.getJSONObject("content").get("cnid");
		if (tmp instanceof String)
			id = Integer.parseInt((String)tmp);
		else
			id = (Integer)tmp;
		reference = obj;
	}
	
	public void addComment(Comment target, int selfUid, DatabaseHelper helper) {
		try {
			JSONArray array = reference.has("comment") ? reference.getJSONArray("comment") : null;
			if (array == null || array.length() == 0) {
				array = new JSONArray();
				array.put(target.toJson());
				reference.put("comment", array);
				update(selfUid, this, helper);
			}
			else {
				int position = App.INT_UNSET;
				for (int i = 0; i < array.length(); i++) {
					Comment cmt = new Comment(id, array.getJSONObject(i));
					if (target.getId() > cmt.getId()) {
						position = i;
						break;
					}
					else if (target.getId() == cmt.getId()) {
						return;
					}
				}
				
				if (position != App.INT_UNSET) {
					array.put(position, target.toJson());
					update(selfUid, this, helper);
				}
			}
		}
		catch (Exception e) {
			ELog.e("Exception:" + e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void removeComment(int commentId, int selfUid, DatabaseHelper helper) {
		try {
			JSONArray array = reference.has("comment") ? reference.getJSONArray("comment") : null;
			if (array == null || array.length() == 0) {
				return;
			}
			else {
				int position = App.INT_UNSET;
				for (int i = 0; i < array.length(); i++) {
					Comment cmt = new Comment(id, array.getJSONObject(i));
					if (commentId == cmt.getId()) {
						position = i;
						break;
					}
				}
				
				if (position != App.INT_UNSET) {
					JSONArray tmp = new JSONArray();
					for (int i = 0; i < array.length(); i++) {
						if (i != position) {
							tmp.put(array.get(i));
						}
					}
					reference.put("comment", tmp);
					update(selfUid, this, helper);
				}
			}
		}
		catch (Exception e) {
			ELog.e("Exception:" + e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void addEnjoy(String targetId, String targetName, int selfUid, DatabaseHelper helper) {
		try {
			JSONObject jsonEnjoy = new JSONObject();
			jsonEnjoy.put("uid", targetId);
			jsonEnjoy.put("nickname", targetName);
			
			JSONArray array = reference.has("prise") ? reference.getJSONArray("prise") : null;
			if (array == null || array.length() == 0) {
				array = new JSONArray();
				array.put(jsonEnjoy);
				reference.put("prise", array);
				update(selfUid, this, helper);
			}
			else {
				array.put(0, jsonEnjoy);
				update(selfUid, this, helper);
			}
		}
		catch (JSONException e) {
			ELog.e("Exception:" + e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void removeEnjoy(String targetId, int selfUid, DatabaseHelper helper) {
		try {
			JSONArray array = reference.has("prise") ? reference.getJSONArray("prise") : null;
			if (array == null || array.length() == 0) {
				return;
			}
			else {
				int position = App.INT_UNSET;
				for (int i = 0; i < array.length(); i++) {
					if (array.getJSONObject(i).getString("uid").equals(targetId)) {
						position = i;
						break;
					}
				}
				
				if (position != App.INT_UNSET) {
					JSONArray tmp = new JSONArray();
					for (int i = 0; i < array.length(); i++) {
						if (i != position) {
							tmp.put(array.get(i));
						}
					}
					reference.put("prise", tmp);
					update(selfUid, this, helper);
				}
			}
		}
		catch (JSONException e) {
			ELog.e("Exception:" + e.getMessage());
			e.printStackTrace();
		}
	}
	
//	public static List<SimpleCircle> query(int selfUid, int minId, int maxId, int count, DatabaseHelper helper) {
//		ELog.i("Self:" + selfUid + " Min:" + minId + " Max:" + maxId + " Count:" + count);
//		
//		String where = DatabaseHelper.KEY_SELF_UID + "=" + selfUid;
//		if (minId != App.INT_UNSET)
//			where += " AND " + DatabaseHelper.KEY_ID + ">" + minId;
//		if (maxId != App.INT_UNSET)
//			where += " AND " + DatabaseHelper.KEY_ID + "<" + maxId;
//		String limit = count > 0 ? String.valueOf(count) : null;
//		
//		LinkedList<SimpleCircle> results = new LinkedList<SimpleCircle>();
//		synchronized (helper) {
//			SQLiteDatabase db = helper.getReadableDatabase();
//			Cursor cursor = db.query(TABLE, null, where, null, null, null, DatabaseHelper.KEY_ID + " DESC", limit);
//			if (cursor != null) {
//				while (cursor.moveToNext()) {
//					results.add(cursor2bean(cursor));
//				}
//				cursor.close();
//			}
//		}
//		return results;
//	}
	
	public static List<SimpleCircle1> query(int selfUid, DatabaseHelper helper) {
		LinkedList<SimpleCircle1> results = new LinkedList<SimpleCircle1>();
		synchronized (helper) {
			SQLiteDatabase db = helper.getReadableDatabase();
			Cursor cursor = db.query(TABLE, null, null, null, null, null, DatabaseHelper.KEY_ID + " DESC");
			if (cursor != null) {
				while (cursor.moveToNext()) {
					results.add(cursor2bean(cursor));
				}
				cursor.close();
			}
		}
		return results;
	}
	
	public static SimpleCircle1 query(int selfUid, int id, DatabaseHelper helper) {
		SimpleCircle1 result = null;
		synchronized (helper) {
			SQLiteDatabase db = helper.getReadableDatabase();
			Cursor cursor = db.query(TABLE, null, DatabaseHelper.KEY_SELF_UID + "=" + selfUid + " AND " + DatabaseHelper.KEY_ID + "=" + id, null, null, null, null);
			if (cursor != null) {
				while (cursor.moveToFirst()) {
					result = cursor2bean(cursor);
				}
				cursor.close();
			}
		}
		return result;
	}
	
	public static void update(int selfUid, SimpleCircle1 bean, DatabaseHelper helper) {
		String where = DatabaseHelper.KEY_ID + "=" + bean.id + " AND " + DatabaseHelper.KEY_SELF_UID + "=" + selfUid;
		synchronized (helper) {
			SQLiteDatabase db = helper.getWritableDatabase();
			Cursor cursor = db.query(TABLE, null, where, null, null, null, null);
			if (cursor != null && cursor.getCount() > 0) {
				ContentValues values = new ContentValues(1);
				values.put(KEY_JSON, bean.reference.toString());
				
				db.update(TABLE, values, where, null);
//				ELog.i("Update:" + selfUid + "/" + bean.id);
			}
			else {
				ContentValues values = new ContentValues(3);
				values.put(DatabaseHelper.KEY_SELF_UID, selfUid);
				values.put(KEY_JSON, bean.reference.toString());
				values.put(DatabaseHelper.KEY_ID, bean.id);
				
				db.insert(TABLE, null, values);
//				ELog.i("Insert:" + selfUid + "/" + bean.id);
			}
			
			if (cursor != null)
				cursor.close();
		}
	}
	
	public static void deleteAll(int selfUid, DatabaseHelper helper) {
		synchronized (helper) {
			SQLiteDatabase db = helper.getWritableDatabase();
			db.delete(TABLE, DatabaseHelper.KEY_SELF_UID + "=" + selfUid, null);
		}
	}

	public static SimpleCircle1 cursor2bean(Cursor cursor) {
		SimpleCircle1 bean = new SimpleCircle1();
		bean.id = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.KEY_ID));
		try {
			bean.reference = new JSONObject(cursor.getString(cursor.getColumnIndex(KEY_JSON)));
		}
		catch (JSONException e) {
			ELog.e("Exception:" + e.getMessage());
			e.printStackTrace();
		}
		return bean;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(id);
		dest.writeString(reference.toString());
	}

	public static final Parcelable.Creator<SimpleCircle1> CREATOR = new Parcelable.Creator<SimpleCircle1>() {
		public SimpleCircle1 createFromParcel(Parcel in) {
			SimpleCircle1 bean = new SimpleCircle1();
			bean.id = in.readInt();
			try {
				bean.reference = new JSONObject(in.readString());
			}
			catch (JSONException e) {
				ELog.e("Exception:" + e.getMessage());
				e.printStackTrace();
			}
			return bean;
		}

		public SimpleCircle1[] newArray(int size) {
			return new SimpleCircle1[size];
		}
	};

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public JSONObject getReference() {
		return reference;
	}

	public void setReference(JSONObject reference) {
		this.reference = reference;
	}
}
