package com.oumen.auth;

import java.io.File;
import java.net.URL;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.oumen.android.App;
import com.oumen.tools.ELog;
import com.oumen.tools.ImageTools;
import com.sina.weibo.sdk.api.ImageObject;
import com.sina.weibo.sdk.api.TextObject;
import com.sina.weibo.sdk.api.WebpageObject;
import com.sina.weibo.sdk.api.WeiboMultiMessage;
import com.sina.weibo.sdk.api.share.IWeiboShareAPI;
import com.sina.weibo.sdk.api.share.SendMultiMessageToWeiboRequest;
import com.sina.weibo.sdk.api.share.WeiboShareSDK;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuth;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.utils.Utility;

public class SinaAuthAdapter extends AuthAdapter {
	public static final String APP_KEY = "3640355425";
	private final String REDIRECT_URL = "https://api.weibo.com/oauth2/default.html";// 应用回调页
	private final String SCOPE = "";

	private IWeiboShareAPI mWeiboShareAPI;
	private Activity activity;

	private File tempFile;

	public SinaAuthAdapter(AuthListener listener) {
		super(listener);
	}

	private SsoHandler sso;

	@Override
	public void authorize(Activity activity) {
		this.activity = activity;
		sso = new SsoHandler(activity, new WeiboAuth(activity, APP_KEY, REDIRECT_URL, SCOPE));
		sso.authorize(sinaListener);
	}

	@Override
	public void shareInit(final Activity activity) {
		this.activity = activity;
		// 创建微博分享接口实例
		mWeiboShareAPI = WeiboShareSDK.createWeiboAPI(activity, APP_KEY);

		// 注册到新浪微博
		if (mWeiboShareAPI.checkEnvironment(true)) {
			mWeiboShareAPI.registerApp();
		}
	}

	@Override
	public void share(MessageType msgType, int actionType, String title, String content, String linkUrl, String imageUrl) {
		// 如果未安装微博客户端，设置下载微博对应的回掉
		if (!mWeiboShareAPI.isWeiboAppInstalled() && !mWeiboShareAPI.isWeiboAppSupportAPI()) {
			Toast.makeText(activity, "本地没有安装新浪客户端或者版本不支持", Toast.LENGTH_SHORT).show();
			return;
		}
		
		if (!TextUtils.isEmpty(content) && content.length() > 120) {
			content = content.substring(0, 120);
		}
		
		if (MessageType.TEXT.equals(msgType)) {
			ELog.i("");
			sendMessage(true, false, false, title, content, linkUrl, imageUrl);
		}
		else {
			ELog.i("");
			sendMessage(true, true, true, title, content, linkUrl, imageUrl);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		ELog.i("");
		if (sso == null) {
			sso = new SsoHandler(activity, new WeiboAuth(activity, APP_KEY, REDIRECT_URL, SCOPE));
		}
		sso.authorizeCallBack(requestCode, resultCode, data);
	}

	private final WeiboAuthListener sinaListener = new WeiboAuthListener() {

		@Override
		public void onWeiboException(WeiboException e) {
			e.printStackTrace();
			ELog.e("Exception:" + e.getMessage());
			listener.onFailed(e);
			sso = null;
		}

		@Override
		public void onComplete(Bundle data) {
			Oauth2AccessToken token = Oauth2AccessToken.parseAccessToken(data);
			if (token.isSessionValid()) {
				ELog.i("Token expires");

				uid = token.getUid();
				accessToken = token.getToken();
				expires = token.getExpiresTime();

				listener.onComplete();
			}
			else {
				ELog.i("Retry authorize");
				sso.authorize(sinaListener);
			}
			sso = null;
		}

		@Override
		public void onCancel() {
			listener.onCancel();
			sso = null;
		}
	};

	/**
	 * 调用发送消息接口
	 * 
	 * @see {@link #sendMultiMessage} 或者 {@link #sendSingleMessage}
	 */
	public void sendMessage(boolean hasText, boolean hasImage, boolean hasWebpage, String title, String content, String linkUrl, String imageUrl) {
		if (mWeiboShareAPI.isWeiboAppSupportAPI()) {
			//TODO 此处先不要删除
			// int supportApi = mWeiboShareAPI.getWeiboAppSupportAPI();
			// if (supportApi >= 10351 /* ApiUtils.BUILD_INT_VER_2_2 */) {
			sendMultiMessage(hasText, hasImage, hasWebpage, title, content, linkUrl, imageUrl);
			// }
			// else {
			// sendSingleMessage(hasText, hasImage, hasWebpage, title, content,
			// linkUrl, imageUrl);
			// }
		}
		else {
			Toast.makeText(activity, "微博客户端不支持 SDK 分享或微博客户端未安装或微博客户端是非官方版本。", Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * 第三方应用发送请求消息到微博，唤起微博分享界面。 注意：当 {@link IWeiboShareAPI#getWeiboAppSupportAPI()} >= 10351 时，支持同时分享多条消息，
	 * 同时可以分享文本、图片以及其它媒体资源（网页、音乐、视频、声音中的一种）。
	 * 
	 * @param hasText
	 *            分享的内容是否有文本
	 * @param hasImage
	 *            分享的内容是否有图片
	 * @param hasWebpage
	 *            分享的内容是否有网页
	 * @param hasVideo
	 *            分享的内容是否有视频
	 */
	private void sendMultiMessage(final boolean hasText, final boolean hasImage, final boolean hasWebpage, final String title, final String content, final String linkUrl, final String imageUrl) {
		App.THREAD.execute(new Runnable() {
			@Override
			public void run() {
				try {
					// 1. 初始化微博的分享消息
					final WeiboMultiMessage weiboMessage = new WeiboMultiMessage();
					if (hasText) {
						ELog.i("");
						TextObject textObject = new TextObject();
						String temp = null;
						if (content.length() > 342) {
							temp = content.substring(0, 342);
						}
						else {
							temp = content;
						}
						textObject.text = temp;
						weiboMessage.textObject = textObject;
					}

					if (hasImage) {
						ELog.i("");
						final ImageObject imageObject = new ImageObject();
						tempFile = new File(App.getUploadCachePath(), System.currentTimeMillis() + ".jpg");
						Bitmap bmp = BitmapFactory.decodeStream(new URL(imageUrl).openStream());
						ImageTools.save(bmp, tempFile, 100);

						Bitmap thumbBmp = ImageTools.decodeSourceFile(tempFile.getAbsolutePath());
						imageObject.setImageObject(thumbBmp);
						imageObject.imagePath = tempFile.getAbsolutePath();
						imageObject.imageData = ImageTools.bmpToByteArray(thumbBmp, true);
						weiboMessage.imageObject = imageObject;
					}

					if (hasWebpage) {
						final WebpageObject mediaObject = new WebpageObject();
						tempFile = new File(App.getUploadCachePath(), System.currentTimeMillis() + ".jpg");
						Bitmap bmp = BitmapFactory.decodeStream(new URL(imageUrl).openStream());
						ImageTools.save(bmp, tempFile, 100);

						Bitmap thumbBmp = ImageTools.decodeFile(tempFile.getAbsolutePath(), App.IMAGE_LENGTH_SMALL);
						mediaObject.setThumbImage(thumbBmp);

						mediaObject.identify = Utility.generateGUID();
						mediaObject.title = title;
						mediaObject.description = content;
						//
						// TODO　　记得删除
						ELog.i(linkUrl);
						mediaObject.actionUrl = linkUrl;
						mediaObject.defaultText = title;

						weiboMessage.mediaObject = mediaObject;
					}

					// 2. 初始化从第三方到微博的消息请求
					SendMultiMessageToWeiboRequest request = new SendMultiMessageToWeiboRequest();
					// 用transaction唯一标识一个请求
					request.transaction = String.valueOf(System.currentTimeMillis());
					request.multiMessage = weiboMessage;
					mWeiboShareAPI.sendRequest(request);

				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		// 3. 发送请求消息到微博，唤起微博分享界面
	}
	
	/**
	 * 新浪是否授权
	 * @return
	 */
	public static boolean isAuthor() {
		if (!TextUtils.isEmpty(App.PREFS.getSinaId()) && !TextUtils.isEmpty(App.PREFS.getSinaToken()) && App.PREFS.getSinaExprise() != App.INT_UNSET) {
			String currenttime = String.valueOf(System.currentTimeMillis());
			if (currenttime.length() > 10) {
				currenttime = currenttime.substring(0, 10);
			}
			if (Long.valueOf(App.PREFS.getSinaExprise()) - Long.valueOf(currenttime) >= 0) {// 判断授权是否过期
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
