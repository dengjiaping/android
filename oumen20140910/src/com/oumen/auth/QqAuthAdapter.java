package com.oumen.auth;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.oumen.android.App;
import com.oumen.tools.ELog;
import com.tencent.connect.share.QzoneShare;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

public class QqAuthAdapter extends AuthAdapter {
	public static final String APP_KEY = "100485899";
//	private final String APP_SECRET = "0c24d616bbe5992fb32bea338c8eb36b";
	
	// QZone分享， SHARE_TO_QQ_TYPE_DEFAULT 图文，SHARE_TO_QQ_TYPE_IMAGE 纯图
	public int SHARE_TYPE_IMAGE_TEXT = QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE_TEXT;
	public int SHARE_TYPE_IMAGE = QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE;

	private Tencent tencent;
	private Activity activity;

	public QqAuthAdapter(AuthListener listener) {
		super(listener);
	}

	@Override
	public void authorize(Activity activity) {
		this.activity = activity;
		tencent = Tencent.createInstance(APP_KEY, activity.getApplicationContext());
		tencent.login(activity, "all", qqListener);
	}
	
	@Override
	public void shareInit(Activity activity) {
		this.activity = activity;
		tencent = Tencent.createInstance(APP_KEY, activity.getApplicationContext());
	}

	@Override
	public void share(MessageType msgType, int actionType, String title, String content, String linkUrl, String imageUrl) {
//		if(tencent.)
		if (!TextUtils.isEmpty(content) && content.length() > 120) {
			content = content.substring(0, 120);
		}
		
		shareToQQ(false, title, content, linkUrl, imageUrl);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		
	}

	private final IUiListener qqListener = new IUiListener() {
		
		@Override
		public void onError(UiError e) {
			ELog.e(e.errorMessage + ":" + e.errorDetail);
			listener.onFailed(e);
		}
		
		@Override
		public void onCancel() {
			ELog.i("");
			listener.onCancel();
		}

		@Override
		public void onComplete(Object response) {
			JSONObject obj = (JSONObject)response;
			try {
				int ret = obj.getInt("ret");
				if (ret == 0) {
					uid = obj.getString("openid");
					accessToken = obj.getString("access_token");
					expires = obj.getLong("expires_in");
					expires = (System.currentTimeMillis() + expires) / 1000;
					listener.onComplete();
				}
				else {
					listener.onFailed("验证失败");
				}
			}
			catch (JSONException e) {
				ELog.e("Exception:" + e.getMessage());
				listener.onFailed(e);
				e.printStackTrace();
			}
		}
	};


	

	/**
	 * QQ空间分享
	 * 
	 * @param imageOnly
	 *            是否仅有图片，true仅有图片，false有图片和文字信息
	 * @param title
	 *            分享标题
	 * @param content
	 *            分享内容
	 * @param images
	 *            分享图片（最多9张图片）
	 */
	public void shareToQQ(boolean imageOnly, String title, String content, String linkUrl, String images) {
		final Bundle params = new Bundle();
		if (imageOnly) {
			params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE, SHARE_TYPE_IMAGE);
		}
		else {
			params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE, SHARE_TYPE_IMAGE_TEXT);
		}
		params.putString(QzoneShare.SHARE_TO_QQ_TITLE, title);
		params.putString(QzoneShare.SHARE_TO_QQ_SUMMARY, content);
		params.putString(QzoneShare.SHARE_TO_QQ_TARGET_URL, linkUrl);
		ArrayList<String> templist = new ArrayList<String>();
		templist.add(images);
		params.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL, templist);
		doShareToQzone(params);
	}

	/**
	 * 用异步方式启动分享
	 * 
	 * @param params
	 */
	private void doShareToQzone(final Bundle params) {
		App.THREAD.execute(new Runnable() {
			@Override
			public void run() {
				tencent.shareToQzone(activity, params, new IUiListener() {

					@Override
					public void onCancel() {
						ELog.i("onCancel: ");
					}

					@Override
					public void onError(UiError e) {
						ELog.e("onError: " + e.errorMessage);
					}

					@Override
					public void onComplete(Object response) {
						ELog.i("onComplete: " + response.toString());
					}

				});
			}
		});
	}
	
	public static boolean isAuthor() {
		if (!TextUtils.isEmpty(App.PREFS.getQQId()) && !TextUtils.isEmpty(App.PREFS.getQQToken()) && App.PREFS.getQQExprise() != App.INT_UNSET) {
			String currenttime = String.valueOf(System.currentTimeMillis());
			if (currenttime.length() > 10) {
				currenttime = currenttime.substring(0, 10);
			}
			if (Long.valueOf(App.PREFS.getQQExprise()) - Long.valueOf(currenttime) >= 0) {// 判断授权是否过期
				// 已经授权
				return true;
			}
			else {
				return false;
			}
		}
		else {
			return false;
		}
	}

}
