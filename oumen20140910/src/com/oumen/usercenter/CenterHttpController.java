package com.oumen.usercenter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import com.oumen.android.App;
import com.oumen.android.UserProfile;
import com.oumen.android.util.Constants;
import com.oumen.auth.AuthAdapter;
import com.oumen.auth.AuthAdapter.Type;
import com.oumen.http.DefaultHttpCallback;
import com.oumen.http.ExceptionHttpResult;
import com.oumen.http.HttpRequest;
import com.oumen.http.HttpResult;
import com.oumen.http.DefaultHttpCallback.EventListener;
import com.oumen.tools.ApacheZip;
import com.oumen.tools.ELog;

import android.os.Handler;

/**
 * 用户中心界面http管理
 * 
 */
public class CenterHttpController {
	public static final int HANDLER_BIND = 0;
	public static final int HANDLER_UPLOAD_PHOTO_SUCCESS = 1;
	public static final int HANDLER_UPLOAD_PHOTO_FAIL = 2;

	private Handler handler;

	private File zipFile;

	public CenterHttpController(Handler handler) {
		this.handler = handler;
	}

	/**
	 * 第三方授权
	 */
	public void bindThirdpart(Type type) {
		String url = null;
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("user_id", String.valueOf(App.PREFS.getUid())));

		if (AuthAdapter.Type.SINA_WEIBO.equals(type)) {
			url = Constants.BIND_SINA_WEIBO;
			params.add(new BasicNameValuePair("UserIDKey", App.PREFS.getSinaId()));
			params.add(new BasicNameValuePair("AccessTokenKey", App.PREFS.getSinaToken()));
			params.add(new BasicNameValuePair("ExpirationDateKey", String.valueOf(App.PREFS.getSinaExprise())));
		}
		else {
			url = Constants.BIND_TENCENT_QQ;
			params.add(new BasicNameValuePair("UserIDKey", App.PREFS.getQQId()));
			params.add(new BasicNameValuePair("AccessTokenKey", App.PREFS.getQQToken()));
			params.add(new BasicNameValuePair("ExpirationDateKey", String.valueOf(App.PREFS.getQQExprise())));
		}

		HttpRequest req = new HttpRequest(url, params, HttpRequest.Method.POST, bindThirdpartCallback);
		App.THREAD.execute(req);
	}

	final DefaultHttpCallback bindThirdpartCallback = new DefaultHttpCallback(new EventListener() {

		@Override
		public void onSuccess(HttpResult result) {
			try {
				String str = result.getResult();
				ELog.i(str);

				if ("1".equals(str)) {
					handler.sendEmptyMessage(HANDLER_BIND);
				}
				else {
					handler.sendMessage(handler.obtainMessage(HANDLER_BIND, "绑定失败"));
				}
			}
			catch (Exception e) {
				ELog.e("Exception:" + e.getMessage());
				handler.sendMessage(handler.obtainMessage(HANDLER_BIND, "绑定失败"));
				e.printStackTrace();
			}
		}

		@Override
		public void onForceClose(ExceptionHttpResult result) {
		}

		@Override
		public void onException(ExceptionHttpResult result) {
			handler.sendMessage(handler.obtainMessage(HANDLER_BIND, "绑定失败"));
		}
	});

	/**
	 * 更换头像
	 * 
	 * @throws IOException
	 */
	public void uploadUserPhoto(String path) throws IOException {
		// 将图片进行压缩
		final String zippath = Constants.IMAGE_PATH + UUID.randomUUID().toString() + ".zip";
		ELog.i("Zip:" + zippath);
		zipFile = new File(zippath);
		if (!zipFile.exists()) {
			if (!zipFile.getParentFile().exists()) {
				zipFile.getParentFile().mkdirs();
			}
			zipFile.createNewFile();
		}
		ApacheZip.writeByApacheZipOutputStream(path, zippath, "", ".jpg");

		final List<NameValuePair> params = new LinkedList<NameValuePair>();
		params.add(new BasicNameValuePair("typs", "11"));
		params.add(new BasicNameValuePair("uid", String.valueOf(App.PREFS.getUid())));

		HttpRequest req = new HttpRequest(Constants.USERCENTER_PHOTO, params, new BasicNameValuePair("img", zippath), null, HttpRequest.Method.POST, reqUploadCallback);
		App.THREAD.execute(req);
	}

	private final DefaultHttpCallback reqUploadCallback = new DefaultHttpCallback(new EventListener() {

		@Override
		public void onSuccess(HttpResult result) {
			try {
				String str = result.getResult();
				ELog.i(str);

				JSONObject obj = new JSONObject(str);
				// 经压缩文件删除了
				zipFile.delete();
				int flag = Integer.parseInt(obj.getString("success"));
				if (flag == 1) {
					// 修改成功
					handler.sendMessage(handler.obtainMessage(HANDLER_UPLOAD_PHOTO_SUCCESS, obj.getString("path")));
				}
				else if (flag == 0) {
					handler.sendMessage(handler.obtainMessage(HANDLER_UPLOAD_PHOTO_FAIL, obj.getString("tip")));
				}
			}
			catch (Exception e) {
				ELog.e("Exception e=" + e);
				handler.sendMessage(handler.obtainMessage(HANDLER_UPLOAD_PHOTO_FAIL, "更换头像失败"));
			}
		}

		@Override
		public void onForceClose(ExceptionHttpResult result) {
		}

		@Override
		public void onException(ExceptionHttpResult result) {
			handler.sendMessage(handler.obtainMessage(HANDLER_UPLOAD_PHOTO_FAIL, "更换头像失败"));
		}
	});
//
//	/**
//	 * 设置昵称
//	 */
//	public void uploadNickName(String nickName) {
//		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
//		params.add(new BasicNameValuePair("user_id", String.valueOf(App.PREFS.getUid())));
//		params.add(new BasicNameValuePair("username", nickName));
//		HttpRequest req = new HttpRequest(Constants.USERCENTER_UPDATE_NICKNAME, params, HttpRequest.Method.GET, defaultCallback);
//		App.THREAD.execute(req);
//	}
//
//	/**
//	 * 设置地址
//	 * 
//	 */
//	public void uploadAddress(String address) {
//		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
//		params.add(new BasicNameValuePair("user_id", String.valueOf(App.PREFS.getUid())));
//		params.add(new BasicNameValuePair("address", address));
//		HttpRequest req = new HttpRequest(Constants.USERCENTER_UPDATE_ADDRESS, params, HttpRequest.Method.GET, defaultCallback);
//		App.THREAD.execute(req);
//	}
//
//	/**
//	 * 设置宝宝签名
//	 * 
//	 * @param sign
//	 */
//	public void uploadBabySign(String sign) {
//		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
//		params.add(new BasicNameValuePair("user_id", String.valueOf(App.PREFS.getUid())));
//		params.add(new BasicNameValuePair("manifesto", sign));
//		HttpRequest req = new HttpRequest(Constants.USERCENTER_UPDATE_SING, params, HttpRequest.Method.GET, defaultCallback);
//		App.THREAD.execute(req);
//	}
//
//	/**
//	 * 设置怀孕时间
//	 * 
//	 * @param time
//	 */
//	public void uploadHuaiYunTime(String time) {
//		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
//		params.add(new BasicNameValuePair("user_id", String.valueOf(App.PREFS.getUid())));
//		params.add(new BasicNameValuePair("time", time));
//		HttpRequest req = new HttpRequest(Constants.USERCENTER_UPDATE_HUAIYUN_TIME, params, HttpRequest.Method.GET, defaultCallback);
//		App.THREAD.execute(req);
//	}
//
//	/**
//	 * 宝宝出生时间设置
//	 * 
//	 * @param time
//	 */
//	public void uploadChuShengTime(String time) {
//		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
//		params.add(new BasicNameValuePair("user_id", String.valueOf(App.PREFS.getUid())));
//		params.add(new BasicNameValuePair("time", time));
//		HttpRequest req = new HttpRequest(Constants.USERCENTER_UPDATE_CHUSEHNG_TIME, params, HttpRequest.Method.GET, defaultCallback);
//		App.THREAD.execute(req);
//	}
//
//	/**
//	 * 设置宝宝性别
//	 * 
//	 * @param sex
//	 */
//	public void uploadBabySex(int sex) {
//		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
//		params.add(new BasicNameValuePair("user_id", String.valueOf(App.PREFS.getUid())));
//		params.add(new BasicNameValuePair("sex", String.valueOf(sex)));
//		HttpRequest req = new HttpRequest(Constants.USERCENTER_UPDATE_CHUSHENG_SEX, params, HttpRequest.Method.GET, defaultCallback);
//		App.THREAD.execute(req);
//	}
//
//	/**
//	 * 设置用户性别
//	 * 
//	 * @param sex
//	 */
//	public void uploadUserSex(int sex) {
//		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
//		params.add(new BasicNameValuePair("user_id", String.valueOf(App.PREFS.getUid())));
//		params.add(new BasicNameValuePair("user_sex", String.valueOf(sex)));
//		HttpRequest req = new HttpRequest(Constants.USERCENTER_UPDATE_USER_SEX, params, HttpRequest.Method.GET, defaultCallback);
//		App.THREAD.execute(req);
//	}
	
	/**
	 * 修改用户中心信息
	 * 
	 */
	public void updateUserInfo() {
		ArrayList<NameValuePair> list = new ArrayList<NameValuePair>();
		list.add(new BasicNameValuePair("user_id", String.valueOf(App.PREFS.getUid())));
		list.add(new BasicNameValuePair("username", App.USER.getNickname()));
		list.add(new BasicNameValuePair("address", App.USER.getAddress()));
		list.add(new BasicNameValuePair("user_sex", String.valueOf(App.USER.getUserGender())));

		list.add(new BasicNameValuePair("manifesto", App.USER.getManifesto()));
		list.add(new BasicNameValuePair("type", String.valueOf(App.USER.getBabyType())));
		switch (App.USER.getBabyType()) {
			case UserProfile.BABY_TYPE_HUAI_YUN:
				list.add(new BasicNameValuePair("time", App.USER.getGravidityTime()));
				list.add(new BasicNameValuePair("sex", "0"));
				break;

			case UserProfile.BABY_TYPE_CHU_SHENG:
				list.add(new BasicNameValuePair("time", App.USER.getBirthdayTime()));
				list.add(new BasicNameValuePair("sex", String.valueOf(App.USER.getBabyGender())));
				break;

			default:
				list.add(new BasicNameValuePair("time", "0000-00-00"));
				list.add(new BasicNameValuePair("sex", "0"));
		}

		HttpRequest req = new HttpRequest(Constants.USERCENTER_UPDATEUSERINFO, list, HttpRequest.Method.GET, null);
		App.THREAD.execute(req);
	}
}
