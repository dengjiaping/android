package com.oumen.account;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.oumen.MainActivity;
import com.oumen.R;
import com.oumen.android.App;
import com.oumen.android.UserProfile;
import com.oumen.android.util.Constants;
import com.oumen.app.ActivityStack;
import com.oumen.auth.AuthAdapter;
import com.oumen.http.DefaultHttpCallback;
import com.oumen.http.DefaultHttpCallback.EventListener;
import com.oumen.http.ExceptionHttpResult;
import com.oumen.http.HttpRequest;
import com.oumen.http.HttpResult;
import com.oumen.message.ActionType;
import com.oumen.message.ChatMessage;
import com.oumen.message.MessageService;
import com.oumen.message.SendType;
import com.oumen.message.Type;
import com.oumen.message.connection.MessageConnection;
import com.oumen.message.ext.LoginMessage;
import com.oumen.tools.ELog;
import com.oumen.tools.Tools;

public class LoginTask implements Runnable {
	public static final int LOGIN_HTTP = 0;
	public static final int LOGIN_IM = 1;
	public static final int LOGIN_REGISTER = 2;
	public static final int LOGIN_THIRDPART = 3;
	
	public static final int FROM_AUTO = 0;
	public static final int FROM_INPUT = 1;
	
	public static final byte LOGIN_TYPE_OUMEN = 0;// 邮箱注册
	public static final byte LOGIN_TYPE_THIRDPART = 1;// 第三方注册
	public static final byte LOGIN_TYPE_GUEST = 2;// 游客
	public static final byte LOGIN_TYPE_PHONE = 3; // 手机注册

	public static final String KEY_FROM = "from";
	public static final String KEY_GUEST_ID = "guest";
	public static final String KEY_THIRDPARTY = "thirdparty";
	public static final String KEY_INFO_ARRAY = "info";
	
	public static final String FLAG_REGISTER_EMAIL = "email_register";
	public static final String FLAG_REGISTER_PHONE = "phone_register";
	
	private static boolean hadLogin = false;
	
	private static boolean httpLogin = false;

	public final AtomicBoolean busy = new AtomicBoolean(false);
	
	private Context context;
	
	private HttpRequest req;
	
	private byte userType;
	private String email;// 此处有两种情况，可能是邮箱，也可能是手机号
	private String password;
	private String nickname;
	private String guestId;
	private String registerFlag;
	private AuthAdapter.Type thirdpartType;

	private byte cacheUserType;
	private String cacheEmail;
	private String cachePassword;
	
	synchronized public void buildCache() {
		cacheUserType = userType;
		cacheEmail = email;
		cachePassword = password;
	}
	
	synchronized public void revertFromCache() {
		ELog.e("");
		userType = cacheUserType;
		email = cacheEmail;
		password = cachePassword;
		
		clearCache();
	}
	
	synchronized public void clearCache() {
		cacheUserType = 0;
		cacheEmail = cachePassword = null;
	}
	
	public LoginTask(Context context) {
		this.context = context;
		
		String[] array = App.PREFS.getEmail();
		String pwd = App.PREFS.getPwd();
		if (array != null && !TextUtils.isEmpty(pwd)) {
			userType = Byte.parseByte(array[0]);
			email = array[1];
			password = pwd;
			
			ELog.w("UserType:" + userType + " Email:" + email + " Password:" + password);
		}
	}
	
	synchronized public void reset() {
		String[] array = App.PREFS.getEmail();
		String pwd = App.PREFS.getPwd();
		if (array != null && !TextUtils.isEmpty(pwd)) {
			userType = Byte.parseByte(array[0]);
			email = array[1];
			password = pwd;
		}
		else {
			userType = 0;
			email = password = null;
		}
		nickname = guestId = null;
		thirdpartType = null;
		registerFlag = null;
//		ELog.w("Email:" + email + " Password:" + password + " Type:" + userType);
	}
	
	synchronized public void logout() {
		ELog.i("");
		App.PREFS.setUid(0);
		App.PREFS.setPwd("");
		App.PREFS.setUserProfile(null);
		UserProfile profile = new UserProfile();
		App.USER.copyFrom(profile);

		App.PREFS.setSinaExprise(App.INT_UNSET);
		App.PREFS.setSinaId("");
		App.PREFS.setSinaToken("");

		App.PREFS.setQQExprise(App.INT_UNSET);
		App.PREFS.setQQId("");
		App.PREFS.setQQToken("");
		
		App.PREFS.setWeiXinId("");
		App.PREFS.setWeiXinToken("");
		App.PREFS.setWeiXinExprise(App.INT_UNSET);
		
		hadLogin = false;
		httpLogin = false;

		MessageConnection.instance.close(false, true);
	}
	
	private boolean isInterrupt = false;
	
	synchronized public void interrupt() {
		ELog.i("");
		isInterrupt = true;
		if (req != null) {
			req.close();
		}
		
		if (MessageConnection.instance.isConnected()) {
			MessageConnection.instance.close(false, true);
		}
	}

	@Override
	synchronized public void run() {
		ELog.i("Email:" + email + " Password:" + password + " Type:" + userType + " Busy:" + busy.get());
		if (busy.get())
			return;
		
		busy.set(true);
		
		if (nickname != null) {
			ELog.w("Register");
			
			ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
			//TODO 已游客身份进来，需要传临时分配的uid;从注销界面过来的，没有uid,不需要传
			if (!TextUtils.isEmpty(guestId) && !"0".equals(guestId)) {
				params.add(new BasicNameValuePair("uid", guestId));
			}
			
			if (registerFlag.equals(LoginTask.FLAG_REGISTER_EMAIL)) {
				params.add(new BasicNameValuePair("nickname", nickname));
				params.add(new BasicNameValuePair("email", email));
				params.add(new BasicNameValuePair("password", password));
			}
			else {
				params.add(new BasicNameValuePair("mobile", email));
				params.add(new BasicNameValuePair("password", password));
			}
			
			isInterrupt = false;
			req = new HttpRequest(Constants.REGISTER_URL, params, HttpRequest.Method.GET, null);
			try {
				HttpResult result = req.connect();
				String res = result.getResult();
				ELog.i(res);
				
				JSONObject obj = new JSONObject(res);
				if (res.contains("tip")) {
					onLoginFailed(obj.getString("tip"));
				}
				else if (obj.getInt("code") == 1) {
					App.PREFS.setUid(obj.getInt("user_id"));
					App.PREFS.setPwd(password);
					if (registerFlag.equals(LoginTask.FLAG_REGISTER_EMAIL)) {
						App.PREFS.setEmail(email, String.valueOf(LOGIN_TYPE_OUMEN));
						if (App.PREFS.isPhoneLogin()) {
							App.PREFS.setPhoneLogin(false);
						}
					}
					else {// 手机注册过来，需要保存手机邮箱
						ELog.i("拼的手机邮箱" + getPhoneEmail(context, email));
						App.PREFS.setEmail(getPhoneEmail(context, email), String.valueOf(LOGIN_TYPE_OUMEN));
						email = getPhoneEmail(context, email);
						App.PREFS.setPhoneLogin(true);
					}
					
					int[] date = App.PREFS.getMVDate();
					if (date == null) {
						Calendar cal = Calendar.getInstance();
						App.PREFS.setMVDate(new int[]{cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)});
					}
					
					hadLogin = true;
					
					obtainMyInfo().run();

					int ret = -1;
					synchronized (MessageConnection.instance) {
						ret = loginIM();
					}
					if (ret == -1) {
						retryConnectOnNewThread();
					}
				}
			}
			catch (Exception e) {
				ELog.e("Exception:" + e.getMessage());
				e.printStackTrace();
				onLoginFailed("注册失败，请检查网络是否畅通");
			}
			finally {
				nickname = null;
			}
			
			if (isInterrupt)
				return;

			//向界面发送登录成功广播
			Intent notify = MessageService.createResponseNotify(MessageService.TYPE_LOGIN);
			notify.putExtra(MessageService.INTENT_KEY_RESULT, MessageService.RESULT_SUCCESS);
			notify.putExtra(MessageService.INTENT_KEY_PARAM, LOGIN_REGISTER);
			context.sendBroadcast(notify);
		}
		else if (userType == LOGIN_TYPE_GUEST) {
			ELog.w("Guest");
			hadLogin = true;
			
			Intent notify = MessageService.createResponseNotify(MessageService.TYPE_LOGIN);
			notify.putExtra(MessageService.INTENT_KEY_RESULT, MessageService.RESULT_SUCCESS);
			notify.putExtra(MessageService.INTENT_KEY_PARAM, LOGIN_HTTP);
			context.sendBroadcast(notify);

			int ret = -1;
			synchronized (MessageConnection.instance) {
				ret = loginIM();
			}
			if (ret == -1) {
				retryConnectOnNewThread();
			}

			obtainMyInfo().run();
		}
		else if (userType == LOGIN_TYPE_OUMEN || (userType == LOGIN_TYPE_THIRDPART && thirdpartType == null)) {
			ELog.w("User");
			ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("email", email));
			params.add(new BasicNameValuePair("password", password));
			if (userType == LOGIN_TYPE_THIRDPART) {
				params.add(new BasicNameValuePair("md5", "1"));
			}
			guestId = null;
			
			isInterrupt = false;
			req = new HttpRequest(Constants.LOGIN_URL, params, HttpRequest.Method.GET, null);
			try {
				HttpResult result = req.connect();
				String res = result.getResult();
				ELog.i(res);

				JSONObject obj = new JSONObject(res);
				if (res.contains("tip")) {
					httpLogin = false;
					App.PREFS.setUid(0);
					App.PREFS.setPwd("");
					onLoginFailed(obj.getString("tip"));
				}
				else if (res.contains("user_id")) {
					String tmp = obj.getString("user_id");
					int uid = Integer.parseInt(tmp);
					App.PREFS.setUid(uid);
					
					int[] date = App.PREFS.getMVDate();
					if (date == null) {
						Calendar cal = Calendar.getInstance();
						App.PREFS.setMVDate(new int[]{cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)});
					}
					
					hadLogin = true;
					httpLogin = true;
					
					Intent notify = MessageService.createResponseNotify(MessageService.TYPE_LOGIN);
					notify.putExtra(MessageService.INTENT_KEY_RESULT, MessageService.RESULT_SUCCESS);
					notify.putExtra(MessageService.INTENT_KEY_PARAM, LOGIN_HTTP);
					context.sendBroadcast(notify);

					int ret = -1;
					synchronized (MessageConnection.instance) {
						ret = loginIM();
					}
					if (ret == -1) {
						retryConnectOnNewThread();
					}
					
					obtainMyInfo().run();
				}
			}
			catch (Exception e) {
				ELog.e("Exception:" + e.getMessage());
				e.printStackTrace();
				
				httpLogin = true;

				Intent notify = MessageService.createResponseNotify(MessageService.TYPE_LOGIN);
				notify.putExtra(MessageService.INTENT_KEY_RESULT, MessageService.RESULT_SUCCESS);
				notify.putExtra(MessageService.INTENT_KEY_PARAM, LOGIN_HTTP);
				context.sendBroadcast(notify);
			}
			
			if (isInterrupt)
				return;

			//向界面发送登录成功广播
			Intent notify = MessageService.createResponseNotify(MessageService.TYPE_LOGIN);
			notify.putExtra(MessageService.INTENT_KEY_RESULT, MessageService.RESULT_SUCCESS);
			context.sendBroadcast(notify);
		}
		else if (userType == LOGIN_TYPE_THIRDPART) {
			ELog.w("Thirdpart");
			String authUid, authAccessToken, expires;
			if (AuthAdapter.Type.QQ.equals(thirdpartType)) {
				authUid = App.PREFS.getQQId();
				authAccessToken = App.PREFS.getQQToken();
				expires = String.valueOf(App.PREFS.getQQExprise());
			}
			else if (AuthAdapter.Type.SINA_WEIBO.equals(thirdpartType)){
				authUid = App.PREFS.getSinaId();
				authAccessToken = App.PREFS.getSinaToken();
				expires = String.valueOf(App.PREFS.getSinaExprise());
			}
			else {// 新增微信
				authUid = App.PREFS.getWeiXinId();
				authAccessToken = App.PREFS.getWeiXinToken();
				expires = String.valueOf(App.PREFS.getWeiXinExprise());
			}

			ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("other_uid", authUid));
			params.add(new BasicNameValuePair("token", authAccessToken));
			params.add(new BasicNameValuePair("key", expires));
			params.add(new BasicNameValuePair("type", String.valueOf(thirdpartType.value())));
			guestId = null;
			
			isInterrupt = false;
			req = new HttpRequest(Constants.LOGIN_OTHER_URL, params, HttpRequest.Method.GET, null);
			try {
				HttpResult result = req.connect();
				String response = result.getResult();
				ELog.i(response);
				
				JSONObject obj = new JSONObject(response);
				int tempflag = Integer.parseInt(obj.getString("succeed"));
				if (tempflag == 1) {// 登录成功
					String uid = obj.getString("user_id"), email = obj.getString("email"), pwd = obj.getString("password");
					boolean registered = "1".equals(obj.optString("registered"));
					
					this.email = email;
					this.password = pwd;
					
					App.PREFS.setUid(Integer.valueOf(uid));
					App.PREFS.setEmail(email, String.valueOf(userType));
					App.PREFS.setPwd(pwd);
					
					int[] date = App.PREFS.getMVDate();
					if (date == null) {
						Calendar cal = Calendar.getInstance();
						App.PREFS.setMVDate(new int[]{cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)});
					}
					
					if (App.PREFS.isPhoneLogin()) {
						App.PREFS.setPhoneLogin(false);
					}
					
					hadLogin = true;
					// 是否是第一次使用本软件，如果是第一次，就要填写用户基本信息
					Intent notify = MessageService.createResponseNotify(MessageService.TYPE_LOGIN);
					notify.putExtra(MessageService.INTENT_KEY_RESULT, MessageService.RESULT_SUCCESS);
					notify.putExtra(MessageService.INTENT_KEY_PARAM, registered ? LOGIN_HTTP : LOGIN_THIRDPART);
					context.sendBroadcast(notify);
					
					//如果用户已经登录IM，则先断开IM连接
					if (MessageConnection.instance.isConnected()) {
						MessageConnection.instance.close(false, true);
					}

					int ret = -1;
					synchronized (MessageConnection.instance) {
						ret = loginIM();
					}
					if (ret == -1) {
						retryConnectOnNewThread();
					}
					
					obtainMyInfo().run();
				}
				else {
					onLoginFailed(obj.getString("tip"));
				}
			}
			catch (Exception e) {
				ELog.e("Exception:" + e.getMessage());
				e.printStackTrace();
				onLoginFailed(context.getString(R.string.login_err));
			}
			
			if (isInterrupt)
				return;

			//向界面发送登录成功广播
			Intent notify = MessageService.createResponseNotify(MessageService.TYPE_LOGIN);
			notify.putExtra(MessageService.INTENT_KEY_RESULT, MessageService.RESULT_SUCCESS);
			context.sendBroadcast(notify);
		}

		busy.set(false);
	}
	
	synchronized private void onLoginFailed(String msg) {
		if (cacheEmail != null) {
			revertFromCache();
		}

		isInterrupt = true;
		busy.set(false);
		
		Intent resp = MessageService.createResponseNotify(MessageService.TYPE_LOGIN);
		resp.putExtra(MessageService.INTENT_KEY_RESULT, MessageService.RESULT_FAILED);
		resp.putExtra(MessageService.INTENT_KEY_MESSAGE, msg);
		context.sendBroadcast(resp);
	}
	
	private int loginIM() {
		try {
			if (MessageConnection.instance.isConnected()) {
				MessageConnection.instance.close(false, true);
			}
			
			int result = MessageConnection.instance.connect();
			if (result == MessageConnection.CONNECT_RESULT_OK) {
				// TODO　此处需要处理
				String pwd = userType == LOGIN_TYPE_OUMEN ? Tools.getEncode("MD5", password) : password;
				if (!email.matches(Constants.PATTERN_EMAIL) && email.matches(Constants.PATTERN_TEL)) {
					email = getPhoneEmail(context, email);
				}
				String send = "{\"LoginMsg\":{\"username\":\"" + email + "\",\"password\":\"" + pwd + "\"}}";
				MessageConnection.instance.send(send);
				return 1;
			}
			else {
				ELog.e("Socket网络连接失败");
				return -1;
			}
		}
		catch (Exception e) {
			ELog.e("Exception:" + e.getMessage());
			e.printStackTrace();
		}
		return -1;
	}
	
	
	synchronized public HttpRequest obtainMyInfo() {
		DefaultHttpCallback callback = new DefaultHttpCallback(new EventListener() {

			public void onSuccess(HttpResult result) {
				try {
					String res = result.getResult();
					ELog.i(res);

					JSONObject json = new JSONObject(res);
					
					UserProfile profile = new UserProfile(json);
					if (App.latitude != 0 && App.longitude != 0) {
						profile.setLatitude(App.latitude);
						profile.setLongitude(App.longitude);
					}

					if (profile != null) {
						ELog.e("Copy user profile");
						App.USER.copyFrom(profile);
					}

					if (!TextUtils.isEmpty(json.optString("email"))) {
						App.PREFS.setUserProfile(res);
//						App.PREFS.setLatitude(profile.getLatitude());
//						App.PREFS.setLongitude(profile.getLongitude());
					}
					
					context.sendBroadcast(MessageService.createResponseNotify(MessageService.TYPE_USERINFO));
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onForceClose(ExceptionHttpResult result) {}

			@Override
			public void onException(ExceptionHttpResult result) {}
		});

		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("user_id", String.valueOf(App.PREFS.getUid())));
		return new HttpRequest(Constants.USERCENTER_GET_MESSAGE, params, HttpRequest.Method.GET, callback);
	}
	
	synchronized public void processLoginMessage(LoginMessage msg, App app) {
		busy.set(false);
		if (msg.getCode() == LoginMessage.CODE_SUCCESS) {
			int selfUid = App.PREFS.getUid();
			app.setTimeOffset(msg.getServerTimeOffset());
			boolean logined = App.PREFS.isHadLogin(selfUid);
			if (!logined) {
				ELog.v("First login and insert OumenTeam welcome message");
				ChatMessage team = new ChatMessage();
				team.setTargetId(ChatMessage.OUMEN_TEAM_ID);
				team.setTargetNickname(context.getString(R.string.oumen_team));
				team.setContent(context.getString(R.string.oumen_team_welcome_msg));
				team.setActionType(ActionType.CHAT);
				team.setType(Type.TEXT);
				team.setToId(selfUid);
				long time = App.getServerTime();
				team.setDatetime(new Date(time));
				team.setSendType(SendType.UNREAD);

				ChatMessage.insert(team, App.DB);

				App.PREFS.setHadLogin(selfUid, true);
			}

			Intent notify = MessageService.createResponseNotify(MessageService.TYPE_LOGIN);
			notify.putExtra(MessageService.INTENT_KEY_RESULT, MessageService.RESULT_SUCCESS);
			notify.putExtra(MessageService.INTENT_KEY_PARAM, LOGIN_IM);
			notify.putExtra(MessageService.INTENT_KEY_DATA, msg.getCode());
			context.sendBroadcast(notify);
			
			App.PREFS.setEmail(email, String.valueOf(userType));
			App.PREFS.setPwd(password);
			
			requestPushService();
			
			clearCache();
		}
		else if (msg.getCode() == LoginMessage.CODE_FAILED) {
			// TODO 登录失败
			if (cacheEmail != null) {
				revertFromCache();
			}
			
			Intent notify = MessageService.createResponseNotify(MessageService.TYPE_LOGIN);
			notify.putExtra(MessageService.INTENT_KEY_RESULT, MessageService.RESULT_SUCCESS);
			notify.putExtra(MessageService.INTENT_KEY_DATA, msg.getCode());
			context.sendBroadcast(notify);
		}
		else if (msg.getCode() == LoginMessage.CODE_KICKED) {
			// TODO 被踢下线
			Intent notify = MessageService.createResponseNotify(MessageService.TYPE_LOGIN);
			notify.putExtra(MessageService.INTENT_KEY_RESULT, MessageService.RESULT_SUCCESS);
			notify.putExtra(MessageService.INTENT_KEY_DATA, msg.getCode());
			context.sendBroadcast(notify);
			
			hadLogin = false;
			httpLogin = false;

			MessageConnection.instance.close(false, true);
			
			Activity current = ActivityStack.getCurrent();
			if (current != null) {
				Intent open = new Intent(context, MainActivity.class);
				open.putExtra(MainActivity.INTENT_KEY_CURRENT_FRAGMENT, MainActivity.Frag.ACCOUNT);
				open.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				current.startActivity(open);
			}
		}
	}
	
	/**
	 * 获取活动推送
	 */
	synchronized private void requestPushService() {
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("uid", String.valueOf(App.PREFS.getUid())));
		HttpRequest req = new HttpRequest(Constants.GET_PUSH_ACTIVITY, params, HttpRequest.Method.GET, null);
		App.THREAD.execute(req);
	}
	
	private int retryConnectCount;
	
	private void retryConnect() {
		int ret = -1;
		synchronized (MessageConnection.instance) {
			ELog.i("isLogin:" + hadLogin + " isConnected:" + MessageConnection.instance.isConnected() + " isBusy:" + busy.get() + " Retry:" + retryConnectCount);
			if (MessageConnection.instance.isConnected() || !hadLogin || busy.get())
				return;
			
			busy.set(true);
			MessageConnection.instance.close(false, true);
			
			ret = loginIM();
			if (ret == -1) {
				try {
					TimeUnit.SECONDS.sleep(5);
				}
				catch (InterruptedException e) {}

				busy.set(false);
			}
			else {
				retryConnectCount = 0;
			}
		}
		
		//放在同步块外面，避免迭代时死锁
		if (ret == -1) {
			retryConnect();
		}
	}
	
	public void retryConnectOnNewThread() {
		App.THREAD.execute(new Runnable() {
			
			@Override
			public void run() {
				retryConnect();
			}
		});
	}
	
	public static String getPhoneEmail(Context context, String phoneNum) {
		String info = context.getString(R.string.register_phone_email);
		Formatter format = new Formatter();
		try {
			return format.format(info, phoneNum).toString();
		}
		finally {
			format.close();
		}
	}
	
	public String getRegisterFlag() {
		return registerFlag;
	}

	public void setRegisterFlag(String registerFlag) {
		this.registerFlag = registerFlag;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
		userType = LOGIN_TYPE_OUMEN;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getGuestId() {
		return guestId;
	}

	public void setGuestId(String guestId) {
		this.guestId = guestId;
	}

	public AuthAdapter.Type getThirdpartType() {
		return thirdpartType;
	}

	public void setThirdpartType(AuthAdapter.Type thirdpartType) {
		this.thirdpartType = thirdpartType;
		userType = LOGIN_TYPE_THIRDPART;
	}

	public byte getUserType() {
		return userType;
	}
	
	public static boolean hadLogin() {
		return hadLogin;
	}
	
	public static boolean isHttpLogin() {
		return httpLogin;
	}
}
