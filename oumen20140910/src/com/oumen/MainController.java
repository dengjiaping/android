package com.oumen;

import java.io.File;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.widget.Toast;

import com.oumen.MainActivity.Frag;
import com.oumen.account.AccountFragment;
import com.oumen.account.LoginTask;
import com.oumen.android.App;
import com.oumen.android.util.Constants;
import com.oumen.home.HomeFragment;
import com.oumen.http.DefaultHttpCallback;
import com.oumen.http.ExceptionHttpResult;
import com.oumen.http.HttpRequest;
import com.oumen.http.HttpResult;
import com.oumen.message.MessageService;
import com.oumen.message.ext.LoginMessage;
import com.oumen.tools.ELog;
import com.oumen.tools.FileTools;
import com.oumen.usercenter.BabyStateActivity;

public class MainController extends Controller<MainActivity> {
	final Handler handler = new Handler();
	private HttpRequest req;
	private boolean hasObtainUid = false;
	private boolean hadHttpLogin = false;

	MainController(MainActivity host) {
		super(host);
	}

	void onCreate(Bundle savedInstanceState) {
		ELog.i("");
		host.registerReceiver(receiver, receiverFilter);

		initialize();
	}

	void onDestroy() {
		host.unregisterReceiver(receiver);
	}

	private void initialize() {
		host.startService(new Intent(host, MessageService.class));
		
		// 开启百度定位
		App.locationClient.start();
		/*
		 *第一次进入，如果没有网络， 如果第一次进入没有网络，就停留在闪屏界面；如果有用户名和密码，就自动登录，进入主界面
		 */
		if (App.NetworkType.NONE.equals(App.getNetworkType())) {
			//如果没有网络
			if (App.PREFS.getEmail() != null && !TextUtils.isEmpty(App.PREFS.getPwd())) {
				//如果用户登录过，直接跳转到HOME页
				host.switchFragment(Frag.HOME, HomeFragment.TYPE_ACTIVITY);

				Toast.makeText(host, R.string.err_network_invalid, Toast.LENGTH_SHORT).show();

				loginFromAuto();
			}
			else {// 如果第一次进入没有网络，就停留在闪屏界面
				host.switchFragment(Frag.SPLASH);
			}
		}
		else {
			int uid = App.PREFS.getUid();
			if (uid == 0) {
				//如果没有uid，则先获取游客uid，获取到游客uid后会再次执行initialize()方法，届时会完成登录操作
				host.switchFragment(Frag.SPLASH);

				hasObtainUid = false;
				hadHttpLogin = true;
				obtainGuestId();
			}
			else {
				hasObtainUid = true;
				if (LoginTask.hadLogin()) {
					//如果已经登录，则直接跳转到HOME页面
					hadHttpLogin = true;
					host.switchFragment(Frag.HOME, HomeFragment.TYPE_ACTIVITY);
				}
				else {
					//如果未登录，则现实Splash页面，同时执行登录操作
					host.switchFragment(Frag.SPLASH);

					loginFromAuto();
				}
			}

			obtainSplashImage();
		}
	}

	/**
	 * 获取广告图
	 */
	private void obtainSplashImage() {
		DefaultHttpCallback callback = new DefaultHttpCallback(new DefaultHttpCallback.EventListener() {

			@Override
			public void onSuccess(HttpResult result) {
				try {
					String str = result.getResult();
					ELog.i(str);

					JSONObject json = new JSONObject(str);
					JSONObject picJson = json.getJSONArray("data").getJSONObject(0);
					String url = picJson.optString("pic2");

					File splashImageFile = new File(App.PATH_SPLASH_IMAGE);

					if (!TextUtils.isEmpty(url) && (!url.equals(App.PREFS.getSplashImageUrl()) || !splashImageFile.exists())) {
						File splashTempFile = new File(App.getDownloadCachePath(), String.valueOf(System.currentTimeMillis()));
						FileTools.download(url, splashTempFile);

						if (splashImageFile.exists()) {
							splashImageFile.delete();
						}
						FileTools.copyFile(splashTempFile, splashImageFile);

						App.PREFS.setSplashImageUrl(url);

						splashTempFile.delete();
					}
				}
				catch (Exception e) {
					ELog.e("Exception:" + e.getMessage());
					e.printStackTrace();
				}
			}

			@Override
			public void onForceClose(ExceptionHttpResult result) {
			}

			@Override
			public void onException(ExceptionHttpResult result) {
			}
		});
		req = new HttpRequest(Constants.OBTAIN_SPLASH_IMAGE, null, HttpRequest.Method.GET, callback);
		App.THREAD.execute(req);
	}

	/**
	 * 自动登录
	 */
	private void loginFromAuto() {
		App.THREAD.execute(new Runnable() {

			@Override
			public void run() {
				//必须等Service启动后才能向Service发送登录请求
				while (!MessageService.isRunning) {
					try {
						TimeUnit.MILLISECONDS.sleep(500);
					}
					catch (Exception e) {
					}
				}

				Intent intent = MessageService.createRequestNotify(MessageService.TYPE_LOGIN);
				intent.putExtra(LoginTask.KEY_FROM, LoginTask.FROM_AUTO);
				host.sendBroadcast(intent);
			}
		});
	}

	private void initialized() {
		if (!hadHttpLogin || !hasObtainUid) {
			return;
		}

		host.switchFragment(Frag.HOME, HomeFragment.TYPE_ACTIVITY);
	}

	private final IntentFilter receiverFilter = new IntentFilter(MessageService.RESPONSE_ACTION);

	private final BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			int type = intent.getIntExtra(MessageService.INTENT_KEY_TYPE, 0);
			if (type == MessageService.TYPE_LOGIN) {
				int result = intent.getIntExtra(MessageService.INTENT_KEY_RESULT, App.INT_UNSET);
				if (result == MessageService.RESULT_FAILED) {
					String msg = intent.getStringExtra(MessageService.INTENT_KEY_MESSAGE);
					Toast.makeText(host, msg, Toast.LENGTH_SHORT).show();

					Fragment current = host.getCurrentFragment();
					if (!(current instanceof AccountFragment)) {
						host.switchFragment(Frag.ACCOUNT);
					}
				}
				else {
					int code = intent.getIntExtra(MessageService.INTENT_KEY_DATA, App.INT_UNSET);
					if (code == LoginMessage.CODE_KICKED) {
						Toast.makeText(host, R.string.login_err_kicked, Toast.LENGTH_SHORT).show();

						host.switchFragment(Frag.ACCOUNT);
					}
					else {
						int param = intent.getIntExtra(MessageService.INTENT_KEY_PARAM, App.INT_UNSET);
						if (param == LoginTask.LOGIN_HTTP) {
							hadHttpLogin = true;

							initialized();
						}
						else if (param == LoginTask.LOGIN_REGISTER || param == LoginTask.LOGIN_THIRDPART) {
							hadHttpLogin = true;
							hasObtainUid = true;

							initialized();

							Intent target = new Intent(host, BabyStateActivity.class);
							target.putExtra(BabyStateActivity.INTENT_KEY_DATA, BabyStateActivity.REQUEST_FROM_REGISTER);
							host.startActivity(target);
						}
					}
				}
			}
			else if (type == MessageService.TYPE_SWITCH_FRAGMENT) {
				Frag target = (Frag) intent.getSerializableExtra(MessageService.INTENT_KEY_PARAM);
				host.switchFragment(target);
			}
		}
	};

	/**
	 * 获取游客ID
	 */
	private void obtainGuestId() {
		DefaultHttpCallback callback = new DefaultHttpCallback(new DefaultHttpCallback.EventListener() {

			@Override
			public void onSuccess(HttpResult result) {
				try {
					String str = result.getResult();
					ELog.i(str);

					JSONObject json = new JSONObject(str);

					if (json.getBoolean("status")) {
						int uid = json.getInt("uid");
						String pwd = json.getString("password");
						hasObtainUid = true;

						App.PREFS.setUid(uid);
						App.PREFS.setPwd(pwd);
						App.PREFS.setEmail(uid + App.GUEST_EMAIL_SUFFIX, String.valueOf(LoginTask.LOGIN_TYPE_GUEST));

						handler.post(new Runnable() {

							@Override
							public void run() {
								//获取游客uid后重新执行initialize()方法，会完成登录操作
								initialize();
							}
						});
					}
					else {
						obtainGuestId();
					}
				}
				catch (Exception e) {
					ELog.e("Exception:" + e.getMessage());
					e.printStackTrace();
				}
			}

			@Override
			public void onForceClose(ExceptionHttpResult result) {
			}

			@Override
			public void onException(ExceptionHttpResult result) {
			}
		});

		LinkedList<NameValuePair> params = new LinkedList<NameValuePair>();
		params.add(new BasicNameValuePair("lat", String.valueOf(App.latitude)));
		params.add(new BasicNameValuePair("lng", String.valueOf(App.longitude)));
		req = new HttpRequest(Constants.OBTAIN_GUEST_ID, params, HttpRequest.Method.GET, callback);
		App.THREAD.execute(req);
	}
}
