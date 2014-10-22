package com.oumen.wxapi;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.os.Handler.Callback;
import android.text.TextUtils;
import android.widget.Toast;

import com.oumen.R;
import com.oumen.android.App;
import com.oumen.android.BaseActivity;
import com.oumen.android.util.Constants;
import com.oumen.http.DefaultHttpCallback;
import com.oumen.http.ExceptionHttpResult;
import com.oumen.http.HttpRequest;
import com.oumen.http.HttpResult;
import com.oumen.message.MessageService;
import com.oumen.tools.ELog;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

public class WXEntryActivity extends BaseActivity implements IWXAPIEventHandler, Callback {
	public static final String WEIXIN_FIRST_HTTP_REQUEST = "https://api.weixin.qq.com/sns/oauth2/access_token";

	private final int HANDLER_GET_TOKEN_SUCCESS = 1;

	private final int HANDLER_GET_TOKEN_AGAIN_SUCCESS = 2;

	private final int HANDLER_GET_TOKEN_FAIL = 3;

	// 第三方应用发送到微信的请求处理后的响应结果，会回调到该方法
	// IWXAPI 是第三方app和微信通信的openapi接口
	private IWXAPI api;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.weixin_layout);
		// 通过WXAPIFactory工厂，获取IWXAPI的实例
		api = WXAPIFactory.createWXAPI(this, Constants.WEIXIN_APP_ID, false);
		api.handleIntent(getIntent(), this);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		setIntent(intent);
		api.handleIntent(intent, this);
	}

	@Override
	public void onResp(BaseResp resp) {
		String result = null;
		switch (resp.errCode) {
			case BaseResp.ErrCode.ERR_OK:
				if (resp instanceof SendAuth.Resp) {// 如果为登录相应返回
					SendAuth.Resp tempResp = (SendAuth.Resp) resp;
					//TODO 用判断一下state和发送的是否一样？
					// 然后发送请求获得
					useCodeRequestToken(tempResp.code);
				}
				else {
					result = "发送成功";
					Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
					finish();
				}
				break;
			case BaseResp.ErrCode.ERR_USER_CANCEL:
				result = "发送取消";
				Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
				finish();
				break;
			case BaseResp.ErrCode.ERR_AUTH_DENIED:
				result = "发送被拒绝";
				Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
				finish();
				break;
			default:
				result = "发送返回";
				Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
				finish();
				break;
		}
	}

	@Override
	public void onReq(BaseReq arg0) {

	}

	private void useCodeRequestToken(String code) {
		DefaultHttpCallback callback = new DefaultHttpCallback(new DefaultHttpCallback.EventListener() {

			@Override
			public void onSuccess(HttpResult result) {
				try {
					String str = result.getResult();
					ELog.i(str);
					handler.sendMessage(handler.obtainMessage(HANDLER_GET_TOKEN_SUCCESS, str));
				}
				catch (Exception e) {
					ELog.e("Exception:" + e.getMessage());
					e.printStackTrace();
				}
			}

			@Override
			public void onForceClose(ExceptionHttpResult result) {
				handler.sendMessage(handler.obtainMessage(HANDLER_GET_TOKEN_FAIL, "强制关闭跳出"));
			}

			@Override
			public void onException(ExceptionHttpResult result) {
				handler.sendMessage(handler.obtainMessage(HANDLER_GET_TOKEN_FAIL, result.getException().getMessage()));
			}
		});

		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();

		params.add(new BasicNameValuePair("appid", Constants.WEIXIN_APP_ID));
		params.add(new BasicNameValuePair("secret", Constants.WEIXIN_APP_SECRET));
		params.add(new BasicNameValuePair("code", code));
		params.add(new BasicNameValuePair("grant_type", "authorization_code"));

		HttpRequest req = new HttpRequest(WEIXIN_FIRST_HTTP_REQUEST, params, HttpRequest.Method.GET, callback);
		App.THREAD.execute(req);
	}

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
			case HANDLER_GET_TOKEN_SUCCESS:// 获取token返回
				String str = (String) msg.obj;
//				Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
				String tip = "";
				try {
					if (str.contains("access_token")) {// 授权成功
						JSONObject json = new JSONObject(str);
						String token = json.getString("access_token");
						int expires = json.getInt("expires_in");
						long expiresTime = (System.currentTimeMillis() + expires) / 1000;
						String refreshToken = json.getString("refresh_token");
						String openId = json.getString("openid");
						// 1.保存数据
						App.PREFS.setWeiXinId(json.getString("openid"));
						App.PREFS.setWeiXinToken(token);
						App.PREFS.setWeiXinExprise(expiresTime);
						//2. 发广播通知界面
						Intent intent = MessageService.createResponseNotify(MessageService.TYPE_WEIXIN_AUTH);
						intent.putExtra(MessageService.INTENT_KEY_DATA, MessageService.RESULT_SUCCESS);
						sendBroadcast(intent);
						//3.关闭界面
//						tip = "token = " + token + ",refreshToken = " + refreshToken + ",expires = " + expires + ",openId= " + openId;
						tip = "授权成功";
					}
					else if (str.contains("errmsg")) {
						//发广播通知界面
						Intent intent = MessageService.createResponseNotify(MessageService.TYPE_WEIXIN_AUTH);
						intent.putExtra(MessageService.INTENT_KEY_DATA, MessageService.RESULT_FAILED);
						sendBroadcast(intent);

						JSONObject json = new JSONObject(str);
						tip = json.getString("errmsg");
					}
					else {
						//发广播通知界面
						Intent intent = MessageService.createResponseNotify(MessageService.TYPE_WEIXIN_AUTH);
						intent.putExtra(MessageService.INTENT_KEY_DATA, MessageService.RESULT_FAILED);
						sendBroadcast(intent);
						tip = "授权失败";
					}
				}
				catch (JSONException e) {
					tip = "授权失败";
					e.printStackTrace();
				}

				if (!TextUtils.isEmpty(tip)) {
					Toast.makeText(this, tip, Toast.LENGTH_SHORT).show();
				}
				finish();

				break;
			case HANDLER_GET_TOKEN_AGAIN_SUCCESS:
				break;
			case HANDLER_GET_TOKEN_FAIL:
				Toast.makeText(this, (String) msg.obj, Toast.LENGTH_SHORT).show();
				finish();
				break;
		}
		return false;
	}

}
