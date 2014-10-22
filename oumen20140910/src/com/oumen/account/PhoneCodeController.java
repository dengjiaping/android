package com.oumen.account;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import com.oumen.android.App;
import com.oumen.android.util.Constants;
import com.oumen.http.DefaultHttpCallback;
import com.oumen.http.ExceptionHttpResult;
import com.oumen.http.HttpRequest;
import com.oumen.http.HttpResult;
import com.oumen.http.DefaultHttpCallback.EventListener;
import com.oumen.tools.ELog;

import android.os.Handler;

/**
 * 获取手机验证码相关http请求
 *
 */
public class PhoneCodeController {

	public static final int REQUEST_PHONE_CODE_REGISTER = 1; // 注册时获取手机验证码的类型
	public static final int REQUEST_PHONE_CODE_FIND_PWD = 2; // 手机找回密码时获取验证码的类型

	public static final int HANDLER_GET_CODE_SUCCESS = 1;//获取验证码成功
	public static final int HANDLER_GET_CODE_FAIL = 2;//获取验证码失败
	public static final int HANDLER_CONFRIM_CODE_SUCCESS = 3;//确认验证码成功
	public static final int HANDLER_CONFRIM_CODE_FAIL = 4;//确认验证码失败

	private Handler handler;

	public PhoneCodeController(Handler handler) {
		this.handler = handler;
	}

	/**
	 * 向服务器获取手机验证码
	 * 
	 * @param phoneNum手机号
	 */
	public void getPhoneCode(String phoneNum, int type) {
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("mobile", phoneNum));
		params.add(new BasicNameValuePair("type", String.valueOf(type)));
		HttpRequest req = new HttpRequest(Constants.REQUEST_PHONE_CODE, params, HttpRequest.Method.GET, requestPhoneCodecallback);
		App.THREAD.execute(req);
	}

	private final DefaultHttpCallback requestPhoneCodecallback = new DefaultHttpCallback(new EventListener() {

		@Override
		public void onSuccess(HttpResult result) {
			try {
				String str = result.getResult();
				ELog.i(str);
				//{"success":1,"code":"778646"}
				JSONObject obj = new JSONObject(str);
				int tip = obj.getInt("success");
				if (tip == 1) {
					//获取验证码成功
					handler.sendMessage(handler.obtainMessage(HANDLER_GET_CODE_SUCCESS, obj.getString("code")));
				}
				else {
					handler.sendMessage(handler.obtainMessage(HANDLER_GET_CODE_FAIL, obj.getString("tip")));
				}

			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onForceClose(ExceptionHttpResult result) {
			handler.sendEmptyMessage(HANDLER_GET_CODE_FAIL);
		}

		@Override
		public void onException(ExceptionHttpResult result) {
			handler.sendEmptyMessage(HANDLER_GET_CODE_FAIL);
		}
	});

	/**
	 * 确认验证码
	 * 
	 * @param phoneNum
	 *            手机号码
	 * @param code
	 *            验证码
	 */
	public void sendCode(String phoneNum, String code) {
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("mobile", phoneNum));
		params.add(new BasicNameValuePair("code", code));
		HttpRequest req = new HttpRequest(Constants.CONFRIM_PHONE_CODE, params, HttpRequest.Method.GET, confirmPhoneCodecallback);
		App.THREAD.execute(req);
	}

	private final DefaultHttpCallback confirmPhoneCodecallback = new DefaultHttpCallback(new EventListener() {

		@Override
		public void onSuccess(HttpResult result) {
			try {
				String str = result.getResult();
				ELog.i(str);
				//{"success":0,"tip":"\u9a8c\u8bc1\u5931\u8d25"}
				JSONObject obj = new JSONObject(str);
				int tip = obj.getInt("success");
				if (tip == 1) {
					//获取验证码成功
					handler.sendMessage(handler.obtainMessage(HANDLER_CONFRIM_CODE_SUCCESS, obj.getString("tip")));
				}
				else {
					handler.sendMessage(handler.obtainMessage(HANDLER_CONFRIM_CODE_FAIL, obj.getString("tip")));
				}

			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onForceClose(ExceptionHttpResult result) {
			handler.sendMessage(handler.obtainMessage(HANDLER_CONFRIM_CODE_FAIL, "验证失败"));
		}

		@Override
		public void onException(ExceptionHttpResult result) {
			handler.sendMessage(handler.obtainMessage(HANDLER_CONFRIM_CODE_FAIL, "验证失败"));
		}
	});

}
