package com.oumen.auth;

import java.lang.reflect.Constructor;

import android.app.Activity;
import android.content.Intent;

public abstract class AuthAdapter {
	public enum Type {
		SINA_WEIBO {

			@Override
			protected String getClassPath() {
				return "com.oumen.auth.SinaAuthAdapter";
			}

			@Override
			public int value() {
				return 1;
			}
		},

		QQ {

			@Override
			protected String getClassPath() {
				return "com.oumen.auth.QqAuthAdapter";
			}

			@Override
			public int value() {
				return 2;
			}
		},

		TENCENT_WEIBO {

			@Override
			protected String getClassPath() {
				return "com.oumen.auth.TencentAuthAdapter";
			}

			@Override
			public int value() {
				return 4;
			}
			
		},
		WEIXIN {
			
			@Override
			protected String getClassPath() {
				return "com.oumen.auth.WeixinAuthAdapter";
			}
			
			@Override
			public int value() {
				return 3;
			}
		};
		
		abstract protected String getClassPath();
		abstract public int value();
	}

	public static final String DEFAULT_IMAGE = "http://www.oumen.com/images/icon.png";
	
	//===============分享需要的参数==============
	public enum MessageType {
		TEXT, //文字消息
		IMAGE_ONLY, //图片消息
		TEXT_IMAGE, //文字+图片消息
		VIDEO//视频消息
	};

	public static final String PACKAGE_NAME = "com.oumen";
	public static final String WEB_SITE = "http://oumen.com";

	protected AuthListener listener;

	protected Type type;

	abstract public void authorize(Activity activity);

	abstract public void onActivityResult(int requestCode, int resultCode, Intent data);

	abstract public void shareInit(Activity activity);

	abstract public void share(MessageType msgType, int actionType, String title, String content, String linkUrl, String imageUrl);
	
//	abstract public void onNewIntent(Intent intent,IWeiboHandler.Response res);

	public AuthAdapter(AuthListener listener) {
		this.listener = listener;
	}

	protected String uid;
	protected String accessToken;
	protected long expires;

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public long getExpires() {
		return expires;
	}

	public void setExpires(long expires) {
		this.expires = expires;
	}

	public Type getType() {
		return type;
	}

	public static AuthAdapter create(AuthListener listener, Type type) {
		AuthAdapter adapter = null;
		try {
			Class<?> cls = Class.forName(type.getClassPath());
			Constructor<?> constructor = cls.getConstructor(AuthListener.class);
			adapter = (AuthAdapter) constructor.newInstance(listener);
			adapter.type = type;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return adapter;
	}
}
