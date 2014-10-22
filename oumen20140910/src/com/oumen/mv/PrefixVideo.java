package com.oumen.mv;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.oumen.android.App;
import com.oumen.android.util.Constants;
import com.oumen.tools.ELog;
import com.oumen.tools.FileTools;
import com.oumen.tools.ImageTools;
import com.oumen.widget.downloader.DownloadListener;
import com.oumen.widget.downloader.DownloadTask;

public class PrefixVideo implements Parcelable, DownloadListener {
	public static final String TABLE = "prefix_video";
	
	public static final String KEY_ID = "id";
	public static final String KEY_NAME = "title";
	public static final String KEY_TOTAL = "total";
	public static final String KEY_TOTAL_DESCRIPTION = "size";
	public static final String KEY_TYPE = "type";
	public static final String KEY_TYPE_TITLE = "type_title";
	public static final String KEY_DESCRIPTION = "description";
	public static final String KEY_CREARE_AT = "create_at";
	
	public static final String URL_TEMPLATE_COVER_SQUARE = Constants.DEFALUT_URL + "img/square/{id}.jpg";
	public static final String URL_TEMPLATE_COVER_CIRCLE = Constants.DEFALUT_URL + "img/cell/{id}.png";
	public static final String URL_TEMPLATE_VIDEO = Constants.DEFALUT_URL + "mv/act/down?name={id}";
	
	public static final byte STATE_DESCRIPTION = 0;
	public static final byte STATE_DOWNLOAD = 1;
	public static final byte STATE_DOWNLOADING = 2;
	public static final byte STATE_COMPLETE = 3;
	
	protected int id;
	protected String name;
	protected long total;
	protected String totalDescription;
	protected int type;
	protected String typeTitle;
	protected String description;
	protected String createAt;
	
	protected File videoFile;
	protected File coverFile;
	protected File coverCircleFile;

	protected long current;
	protected boolean selected;
	protected byte state;
	protected DownloadTask downloadTask;
	protected DownloadListener downloadListener;
	
	public PrefixVideo(JSONObject json, String title) throws JSONException {
		id = Integer.parseInt(json.getString("mvid"));
		name = json.getString("name");
		totalDescription = json.getString("size");
		description = json.getString("dis");
		createAt = json.optString("createtime");
		type = Integer.parseInt(json.getString("mvtype"));
		typeTitle = title;
		
		buildFile();
	}
	
	public PrefixVideo(Cursor cursor) {
		id = cursor.getInt(cursor.getColumnIndex(KEY_ID));
		name = cursor.getString(cursor.getColumnIndex(KEY_NAME));
		total = cursor.getLong(cursor.getColumnIndex(KEY_TOTAL));
		totalDescription = cursor.getString(cursor.getColumnIndex(KEY_TOTAL_DESCRIPTION));
		type = cursor.getInt(cursor.getColumnIndex(KEY_TYPE));
		typeTitle = cursor.getString(cursor.getColumnIndex(KEY_TYPE_TITLE));
		description = cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION));
		createAt = cursor.getString(cursor.getColumnIndex(KEY_CREARE_AT));
		
		buildFile();
	}
	
	public PrefixVideo(Parcel in) {
		id = in.readInt();
		name = in.readString();
		total = in.readLong();
		totalDescription = in.readString();
		type = in.readInt();
		typeTitle = in.readString();
		description = in.readString();
		createAt = in.readString();
		
		buildFile();
	}
	
	private void buildFile() {
		videoFile = new File(MvHelper.getPrefixPath(name));
		coverFile = new File(MvHelper.getCoverPath(name));
		coverCircleFile = new File(coverFile.getParentFile(), coverFile.getName().replace(MvHelper.EXTENSION_COVER, MvHelper.EXTENSION_COVER_CIRCLE));
	}
	
	public void initialize() {
		current = videoFile.exists() ? videoFile.length() : 0;
		
		if (total > 0) {
			downloadTask = new DownloadTask(getVideoUrl(), videoFile.getAbsolutePath(), total);
		}
		else {
			downloadTask = new DownloadTask(getVideoUrl(), videoFile.getAbsolutePath());
		}
		downloadTask.setListener(this);
		
		if (total != 0 && current == total) {
			state = STATE_COMPLETE;
		}
		else if (total != 0 && current != 0) {
			state = STATE_DOWNLOADING;
		}
		else {
			state = STATE_DESCRIPTION;
		}
	}
	
	public void setDownloadListener(DownloadListener downloadListener) {
		this.downloadListener = downloadListener;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getTotal() {
		return total;
	}

	public void setTotal(long total) {
		this.total = total;
	}

	public String getTotalDescription() {
		return totalDescription;
	}

	public void setTotalDescription(String totalDescription) {
		this.totalDescription = totalDescription;
	}

	public long getCurrent() {
		return current;
	}

	public void setCurrent(long current) {
		this.current = current;
	}

	public File getVideoFile() {
		return videoFile;
	}

	public File getCoverFile() {
		return coverFile;
	}
	
	public File getCoverCircleFile() {
		return coverCircleFile;
	}
	
	public String getVideoLocalUrl() {
		return App.SCHEMA_FILE + videoFile.getAbsolutePath();
	}
	
	public String getCoverLocalUrl() {
		return App.SCHEMA_FILE + coverFile.getAbsolutePath();
	}
	
	public String getCoverCircleLocalUrl() {
		return App.SCHEMA_FILE + coverCircleFile.getAbsolutePath();
	}
	
	public String getVideoUrl() {
		return URL_TEMPLATE_VIDEO.replace("{id}", String.valueOf(id));
	}
	
	public String getCoverUrl() {
		return URL_TEMPLATE_COVER_SQUARE.replace("{id}", String.valueOf(id));
	}
	
	public String getCoverCircleUrl() {
		return URL_TEMPLATE_COVER_CIRCLE.replace("{id}", String.valueOf(id));
	}
	
	boolean exists() {
		return videoFile.exists() && downloadTask.getTotal() == videoFile.length();
	}
	
	public void downloadCoverImage() {
		String urlCover = getCoverUrl();
		if (!coverFile.exists() && !TextUtils.isEmpty(urlCover)) {
			ELog.i("Download cover:" + urlCover);
			File cache = new File(App.getDownloadCachePath(), name);
			if (cache.exists()) {
				FileTools.copyFile(cache, coverFile);
				createCircleCoverFile();
				return;
			}
			
			try {
				if (FileTools.download(urlCover, cache)) {
					FileTools.copyFile(cache, coverFile);
					createCircleCoverFile();
				}
			}
			catch (Exception e) {
				if (cache.exists()) {
					cache.delete();
				}
				e.printStackTrace();
			}
			ELog.i("End download cover:" + urlCover + " " + coverFile.getAbsolutePath());
		}
	}
	
	public void createCircleCoverFile() {
		if (!coverCircleFile.exists()) {
			Bitmap imgSrc = ImageTools.decodeSourceFile(coverFile.getAbsolutePath());
			if (imgSrc == null) {
				return ;
			}
			Bitmap imgSquare = ImageTools.clip2square(imgSrc);
			Bitmap circle = ImageTools.toOvalBitmap(imgSquare);
			FileOutputStream fos = null;
			try {
				coverCircleFile.createNewFile();
				fos = new FileOutputStream(coverCircleFile);
				circle.compress(CompressFormat.PNG, 100, fos);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			finally {
				if (fos != null) {
					try {fos.close();}catch(Exception e){}
				}
			}
		}
	}

	public static void insert(PrefixVideo obj) {
		synchronized (PrefixVideo.class) {
			ELog.i("ID:" + obj.id + " Title:" + obj.name);
			ContentValues values = new ContentValues();
			values.put(KEY_ID, obj.id);
			values.put(KEY_NAME, obj.name);
			values.put(KEY_TOTAL, obj.total);
			values.put(KEY_TOTAL_DESCRIPTION, obj.totalDescription);
			values.put(KEY_TYPE, obj.type);
			values.put(KEY_TYPE_TITLE, obj.typeTitle);
			values.put(KEY_DESCRIPTION, obj.description);
			values.put(KEY_CREARE_AT, obj.createAt);
			SQLiteDatabase db = App.DB.getWritableDatabase();
			db.insert(TABLE, null, values);
		}
	}

	public static void insert(List<PrefixVideo> list) {
		synchronized (PrefixVideo.class) {
			String sql = "INSERT INTO " + TABLE + " (`" + KEY_ID + "`,`" + KEY_NAME + "`, `" + KEY_TOTAL + "`, `" + KEY_TOTAL_DESCRIPTION
					+ "`, `" + KEY_TYPE + "`, `" + KEY_TYPE_TITLE + "`, `" + KEY_DESCRIPTION + "`, `" + KEY_CREARE_AT + "`) VALUES ";
			for (PrefixVideo i : list) {
				sql += "(" + i.id + ",'" + i.name + "', " + i.total + ", '" + i.totalDescription
						+ "', " + i.type + ", '" + i.typeTitle + "', '" + i.description + "', '" + i.createAt + "'), ";
			}
			sql = sql.substring(0, sql.length() - 2);
			SQLiteDatabase db = App.DB.getWritableDatabase();
			db.execSQL(sql);
		}
	}

	public static void update(PrefixVideo obj) {
		synchronized (PrefixVideo.class) {
			ELog.i("ID:" + obj.id + " Title:" + obj.name);
			ContentValues values = new ContentValues();
			values.put(KEY_NAME, obj.name);
			values.put(KEY_TOTAL, obj.total);
			values.put(KEY_TOTAL_DESCRIPTION, obj.totalDescription);
			values.put(KEY_TYPE, obj.type);
			values.put(KEY_TYPE_TITLE, obj.typeTitle);
			values.put(KEY_DESCRIPTION, obj.description);
			values.put(KEY_CREARE_AT, obj.createAt);
			SQLiteDatabase db = App.DB.getWritableDatabase();
			db.update(TABLE, values, KEY_ID + " = " + obj.id, null);
		}
	}
	
	public static PrefixVideo query(int id) {
		SQLiteDatabase db = App.DB.getReadableDatabase();
		Cursor cursor = db.query(TABLE, null, KEY_ID + "=" + id, null, null, null, null);
		if (cursor != null && cursor.moveToNext()) {
			PrefixVideo bean = cursor2bean(cursor);
			cursor.close();
			return bean;
		}
		return null;
	}
	
	public static List<PrefixVideo> query() {
		LinkedList<PrefixVideo> beans = new LinkedList<PrefixVideo>();
		synchronized (PrefixVideo.class) {
			SQLiteDatabase db = App.DB.getReadableDatabase();
			Cursor cursor = db.query(TABLE, null, null, null, null, null, null);
			if (cursor != null) {
				while (cursor.moveToNext()) {
					beans.add(cursor2bean(cursor));
				}
				cursor.close();
			}
		}
		return beans;
	}
	
	public static String obtainTitle(int prefixId) {
		String title = null;
		synchronized (PrefixVideo.class) {
			SQLiteDatabase db = App.DB.getReadableDatabase();
			Cursor cursor = db.query(TABLE, new String[]{KEY_NAME}, KEY_ID + "=" + prefixId, null, null, null, null);
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					title = cursor.getString(0);
				}
				cursor.close();
			}
		}
		return title;
	}
	
	public static void delete() {
		synchronized (PrefixVideo.class) {
			SQLiteDatabase db = App.DB.getWritableDatabase();
			db.delete(TABLE, null, null);
		}
	}
	
	public String getCreateTime() {
		Date date = new Date(videoFile.lastModified());
		return App.YYYY_MM_DD_FORMAT.format(date);
	}
	
	public static PrefixVideo cursor2bean(Cursor cursor) {
		PrefixVideo bean = new PrefixVideo(cursor);
		bean.initialize();
		return bean;
	}

	@Override
	public int describeContents() {
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(id);
		dest.writeString(name);
		dest.writeLong(total);
		dest.writeString(totalDescription);
		dest.writeInt(type);
		dest.writeString(typeTitle);
		dest.writeString(description);
		dest.writeString(createAt);
	}

	public final Parcelable.Creator<PrefixVideo> CREATOR = new Parcelable.Creator<PrefixVideo>() {
		public PrefixVideo createFromParcel(Parcel in) {
			PrefixVideo bean = new PrefixVideo(in);
			bean.initialize();
			return bean;
		}

		public PrefixVideo[] newArray(int size) {
			return new PrefixVideo[size];
		}
	};

	@Override
	public void onStart(DownloadTask task) {
		total = task.getTotal();
		update(this);
		
		if (downloadListener != null) {
			downloadListener.onStart(task);
		}
	}

	@Override
	public void onProgressUpdate(DownloadTask task) {
		current = task.getCurrent();
		
		if (downloadListener != null) {
			downloadListener.onProgressUpdate(task);
		}
	}

	@Override
	public void onCompleted(DownloadTask task) {
		ELog.i(task.getUrl());
		state = STATE_COMPLETE;
		
		if (downloadListener != null) {
			downloadListener.onCompleted(task);
		}
	}

	@Override
	public void onDisconnect(DownloadTask task) {
		ELog.v(task.getUrl());
		
		if (downloadListener != null) {
			downloadListener.onDisconnect(task);
		}
	}

	@Override
	public void onFailed(DownloadTask task) {
		ELog.w(task.getUrl());
		
		if (downloadListener != null) {
			downloadListener.onFailed(task);
		}
	}
}
