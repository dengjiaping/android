package com.oumen.mv.index;

import java.io.File;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Base64;

import com.oumen.android.App;
import com.oumen.android.util.Constants;
import com.oumen.db.DatabaseHelper;
import com.oumen.http.HttpRequest;
import com.oumen.http.HttpResult;
import com.oumen.mv.MvInfo;
import com.oumen.tools.ELog;

public class UploadTask implements Runnable {
	public static final byte RESULT_FAILED = -2;
	public static final byte RESULT_CANCEL = -1;
	public static final byte RESULT_SUCCESSED = 0;
	
	public static final String TABLE = "upload";

	public static final String KEY_NAME = "name";
	public static final String KEY_PROGRESS = "progress";
	
	public static final int UNIT_LENGTH = 200 * 1024;
	public static final String PATH_TEMPLETE = "/{name}";

	private MvInfo info;
	private int total;//文件个数
	private int progress;//当前上传到第几个
	private File source;
	private UploadListener listener;
	private boolean uploading;
	private HttpRequest req;
	
	public UploadTask(MvInfo info) {
		this.info = info;
		source = new File(info.getPath());
		update();
	}
	
	public void update() {
		total = source.exists() ? (int)Math.ceil((double)source.length() / UNIT_LENGTH) : 0;
	}
	
	public boolean isUploading() {
		return uploading;
	}

	public void setUploading(boolean uploading) {
		this.uploading = uploading;
	}
	
	public void start() {
		if (!uploading && req != null) {
			uploading = true;
		}
		else {
			App.THREAD.execute(this);
		}
	}
	
	public void stop() {
		if (uploading) {
			uploading = false;
		}
	}

	boolean buildSlices() {
		String templete = App.PATH_MV + PATH_TEMPLETE;
		String dir = templete.replace("{name}", info.getTitle());
		File fileDir = new File(dir);
		if (!fileDir.exists())
			fileDir.mkdirs();
		
		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(source, "r");
			byte[] buf = new byte[10240];
			int count = (int)Math.ceil((double)raf.length() / UNIT_LENGTH);
			for (int i = 0; i < count; i++) {
				File tmp = new File(dir, App.NUMBER_FORMAT.format(i));
				tmp.createNewFile();
				
				FileOutputStream fos = new FileOutputStream(tmp);
				
				int read = -1, readCount = 0;
				try {
					while ((read = raf.read(buf)) != -1) {
						fos.write(buf, 0, read);
						
						readCount += read;
						if (readCount == UNIT_LENGTH) {
							break;
						}
					}
				}
				catch (Exception e) {
					ELog.e("Exception:" + e.getMessage());
					e.printStackTrace();
				}
				finally {
					if (fos != null) {
						try {fos.close();}catch(Exception e){}
					}
				}
			}
			return true;
		}
		catch (Exception e) {
			ELog.e("Exception:" + e.getMessage());
			e.printStackTrace();
			
			return false;
		}
		finally {
			if (raf != null) {
				try {raf.close();}catch(Exception e){}
			}
		}
	}
	
	void clearSlices() {
		String templete = App.PATH_MV + PATH_TEMPLETE;
		File dir = new File(templete.replace("{name}", info.getTitle()));
		if (dir.exists()) {
			File[] slices = dir.listFiles();
			for (File i : slices) {
				i.delete();
			}
			dir.delete();
		}
	}
	
	public static UploadTask query(MvInfo info, DatabaseHelper helper) {
		ELog.i("Uid:" + info.getUserId() + " Name:" + info.getTitle());
		synchronized (UploadTask.class) {
			SQLiteDatabase db = helper.getReadableDatabase();
			String selection = DatabaseHelper.KEY_SELF_UID + "=" + info.getUserId() + " AND " + KEY_NAME + "='" + info.getTitle() + "'";
			
			UploadTask task = new UploadTask(info);
			Cursor cursor = db.query(TABLE, null, selection, null, null, null, null);
			if (cursor != null) {
				if (cursor.moveToNext()) {
					task.progress = cursor.getInt(cursor.getColumnIndex(KEY_PROGRESS));
					task.update();
				}
				cursor.close();
			}
			
			return task;
		}
	}
	
	public static void insert(UploadTask task, DatabaseHelper helper) {
		ELog.i("Uid:" + task.info.getUserId() + " Name:" + task.info.getTitle() + " Progress:" + task.progress + "/" + task.total);
		synchronized (UploadTask.class) {
			SQLiteDatabase db = helper.getWritableDatabase();
			
			ContentValues values = new ContentValues();
			values.put(DatabaseHelper.KEY_SELF_UID, task.info.getUserId());
			values.put(KEY_NAME, task.info.getTitle());
			values.put(KEY_PROGRESS, task.progress);
			
			db.insert(TABLE, null, values);
		}
	}
	
	public static void update(UploadTask task, DatabaseHelper helper) {
		ELog.i("Uid:" + task.info.getUserId() + " Name:" + task.info.getTitle() + " Progress:" + task.progress + "/" + task.total);
		synchronized (UploadTask.class) {
			SQLiteDatabase db = helper.getWritableDatabase();
			
			ContentValues values = new ContentValues();
			values.put(DatabaseHelper.KEY_SELF_UID, task.info.getUserId());
			values.put(KEY_NAME, task.info.getTitle());
			values.put(KEY_PROGRESS, task.progress);
			
			db.update(TABLE, values, DatabaseHelper.KEY_SELF_UID + "=" + task.info.getUserId() + " AND " + KEY_NAME + "='" + task.info.getTitle() + "'", null);
		}
	}
	
	public static void delete(int uid, String name, DatabaseHelper helper) {
		ELog.i("Uid:" + uid + " Name:" + name);
		synchronized (UploadTask.class) {
			SQLiteDatabase db = helper.getWritableDatabase();
			db.delete(TABLE, DatabaseHelper.KEY_SELF_UID + "=" + uid + " AND " + KEY_NAME + "='" + name + "'", null);
		}
	}
	
	private byte prepare() {
		if (!uploading) {
			if (listener != null) {
				listener.onCancel(this);
			}
			return RESULT_CANCEL;
		}
		
		insert(UploadTask.this, App.DB);
		
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("firstpic", String.valueOf(info.getPrefix().getId())));
		params.add(new BasicNameValuePair("name", String.valueOf(info.getTitle())));
		params.add(new BasicNameValuePair("uid", String.valueOf(info.getUserId())));
//		params.add(new BasicNameValuePair("md5", Tools.fileToMD5(info.getPath())));
		params.add(new BasicNameValuePair("totallength", String.valueOf(new File(info.getPath()).length())));
		
		try {
			req = new HttpRequest(Constants.GET_MV_ID, params, HttpRequest.Method.POST, null);
			HttpResult result = req.connect();
			String res = result.getResult();
			ELog.i(res);
			
			req = null;
			
			JSONObject obj = new JSONObject(res);
			int mvId = obj.getInt("id");
			
			String str = Base64.encodeToString(("num=" + String.valueOf(mvId)).getBytes(), Base64.DEFAULT);
			String mvUrl = Constants.MV_WEB_URL + str;
			
			if (mvId != 0 && !TextUtils.isEmpty(mvUrl)) {
				info.setServerId(mvId);
				info.setServerUrl(mvUrl);
				MvInfo.update(info.getUserId(), info.getTitle(), mvId, mvUrl, App.DB);
			}
			
			if (listener != null) {
				listener.onPrepare(this);
			}
			
			return RESULT_SUCCESSED;
		}
		catch (Exception e) {
			ELog.e("Exception:" + e.getMessage());
			e.printStackTrace();
			uploading = false;
			req = null;
			
			if (listener != null) {
				listener.onFailed(this);
			}
			return RESULT_FAILED;
		}
	}
	
	private byte upload(List<String> slicesPath) {
		if (!uploading) {
			if (listener != null) {
				listener.onCancel(this);
			}
			return RESULT_CANCEL;
		}
		
		// 设置连接超时
		HttpRequest.timeout = HttpRequest.TIME_LONG;

		try {
			for (String path : slicesPath) {
				if (!uploading) {
					if (listener != null)
						listener.onCancel(this);
					return RESULT_CANCEL;
				}
				
				ELog.v("Slice:" + path);
				ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("id", String.valueOf(info.getServerId())));// 视频id
				params.add(new BasicNameValuePair("uid", String.valueOf(info.getUserId())));// 用户id
				
				req = new HttpRequest(Constants.MV_UPLOAD_FILE, params, new BasicNameValuePair("file", path), null, HttpRequest.Method.POST, null);
				HttpResult result = req.connect();
				String res = result.getResult();
				ELog.i(res);
				
				req = null;
				
//				JSONObject obj = new JSONObject(res);
//				int mvId = Integer.valueOf(obj.getString("mvid"));
				progress++;
				
				if (progress < total) {
					update(this, App.DB);
				}
				else {
					delete(info.getUserId(), info.getTitle(), App.DB);
					MvInfo.update(info.getUserId(), info.getTitle(), info.getType(), MvInfo.TYPE_PUBLISHED, App.DB);
					info.setType(MvInfo.TYPE_PUBLISHED);
					uploading = false;
				}
				
				if (listener != null) {
					listener.onProgressUpdate(this);
				}
			}
			
			HttpRequest.timeout = HttpRequest.TIME_SHORT;
			clearSlices();
			return RESULT_SUCCESSED;
		}
		catch (Exception e) {
			HttpRequest.timeout = HttpRequest.TIME_SHORT;
			ELog.e("Exception:" + e.getMessage());
			e.printStackTrace();
			uploading = false;
			req = null;
			
			if (listener != null) {
				listener.onFailed(this);
			}
			return RESULT_FAILED;
		}
	}

	@Override
	public void run() {
		uploading = true;
		
		MvInfo.update(info.getUserId(), info.getTitle(), info.getType(), MvInfo.TYPE_UPLOAD, App.DB);
		info.setType(MvInfo.TYPE_UPLOAD);
		
		String templete = App.PATH_MV + PATH_TEMPLETE;
		String dir = templete.replace("{name}", info.getTitle());
		File fileDir = new File(dir);
		
		LinkedList<String> slicesPath = new LinkedList<String>();
		String[] files = null;
		if (!fileDir.exists() || (files = fileDir.list()) == null || files.length == 0) {
			if (total == 0) {
				update();
			}
			
			if (!buildSlices()) {
				return;
			}
		}
		
		for (int i = progress; i < total; i++) {
			slicesPath.add(dir + "/" + App.NUMBER_FORMAT.format(i));
		}
		
		ELog.v("Slices:" + slicesPath.size());
		
		if (progress == 0) {
			byte ret = prepare();
			if (ret != RESULT_SUCCESSED) {
				return;
			}
		}
		
		upload(slicesPath);
	}
	
	public UploadListener getListener() {
		return listener;
	}

	public void setListener(UploadListener listener) {
		this.listener = listener;
	}

	public MvInfo getInfo() {
		return info;
	}

	public int getTotal() {
		return total;
	}

	public int getProgress() {
		return progress;
	}

	public File getSource() {
		return source;
	}

	public interface UploadListener {
		void onPrepare(UploadTask task);
		void onProgressUpdate(UploadTask task);
		void onFailed(UploadTask task);
		void onCancel(UploadTask task);
	}
}
