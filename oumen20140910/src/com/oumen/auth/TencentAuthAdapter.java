package com.oumen.auth;


import android.app.Activity;
import android.content.Intent;

import com.oumen.tools.ELog;
import com.tencent.weibo.component.Authorize;
import com.tencent.weibo.component.sso.AuthHelper;
import com.tencent.weibo.component.sso.OnAuthListener;
import com.tencent.weibo.component.sso.WeiboToken;

public class TencentAuthAdapter extends AuthAdapter {
	private final long APP_KEY = 100485899;
	private final String APP_SECRET = "0c24d616bbe5992fb32bea338c8eb36b";

	private Activity activity;
	public TencentAuthAdapter(AuthListener listener) {
		super(listener);
	}

	@Override
	public void authorize(Activity activity) {
		this.activity = activity;
		AuthHelper.register(activity, APP_KEY, APP_SECRET, tencentListener);
		AuthHelper.auth(activity, "");
	}

	@Override
	public void shareInit(Activity activity) {
		
	}

	@Override
	public void share(MessageType msgType, int actionType, String title, String content, String linkUrl, String imageUrl) {
		
	}

	private final OnAuthListener tencentListener = new OnAuthListener() {

		// 如果当前设备没有安装腾讯微博客户端，走这里
		@Override
		public void onWeiBoNotInstalled() {
			ELog.i("当前设备没有安装腾讯微博客户端");
			AuthHelper.unregister(activity);
			Intent i = new Intent(activity, Authorize.class);
			activity.startActivityForResult(i, Authorize.REQUEST_CODE);

			activity = null;
		}

		// 如果当前设备没安装指定版本的微博客户端，走这里
		@Override
		public void onWeiboVersionMisMatch() {
			ELog.i("当前设备没安装指定版本的微博客户端");
			AuthHelper.unregister(activity);
			Intent i = new Intent(activity, Authorize.class);
			activity.startActivityForResult(i, Authorize.REQUEST_CODE);

			activity = null;
		}

		// 如果授权失败，走这里
		@Override
		public void onAuthFail(int result, String err) {
			ELog.i("授权失败:" + err);
			AuthHelper.unregister(activity);
			listener.onFailed(result);

			activity = null;
		}

		// 授权成功，走这里
		// 授权成功后，所有的授权信息是存放在WeiboToken对象里面的，可以根据具体的使用场景，将授权信息存放到自己期望的位置，
		// 在这里，存放到了applicationcontext中
		@Override
		public void onAuthPassed(String name, WeiboToken token) {
			ELog.i("授权成功");
			uid = token.openID;
			accessToken = token.accessToken;
			expires = token.expiresIn;
			AuthHelper.unregister(activity);
			listener.onComplete();

			activity = null;
		}
	};

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == Authorize.REQUEST_CODE) {
			if (resultCode == Activity.RESULT_OK) {
				String result = data.getStringExtra(Authorize.INTENT_DATA_KEY);
				String resultParam = result.split("#")[1];
				String params[] = resultParam.split("&");
				String accessToken = params[0].split("=")[1];
				String expiresIn = params[1].split("=")[1];
				String openid = params[2].split("=")[1];
//				String openkey = params[3].split("=")[1];
//				String refreshToken = params[4].split("=")[1];
//				String state = params[5].split("=")[1];
//				String name = params[6].split("=")[1];
//				String nick = params[7].split("=")[1];

				this.uid = openid;
				this.accessToken = accessToken;
				this.expires = Long.parseLong(expiresIn);

				listener.onComplete();
			}
			else {
				listener.onCancel();
			}
		}
	}

}
