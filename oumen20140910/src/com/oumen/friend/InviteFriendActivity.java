package com.oumen.friend;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.oumen.R;
import com.oumen.TitleBar;
import com.oumen.android.App;
import com.oumen.android.BaseActivity;
import com.oumen.android.UserProfile;
import com.oumen.android.util.Constants;
import com.oumen.auth.AuthAdapter;
import com.oumen.auth.WeixinAuthAdapter;
import com.oumen.auth.AuthAdapter.MessageType;
import com.oumen.auth.AuthListener;
import com.oumen.http.DefaultHttpCallback;
import com.oumen.http.DefaultHttpCallback.EventListener;
import com.oumen.http.ExceptionHttpResult;
import com.oumen.http.HttpRequest;
import com.oumen.http.HttpResult;
import com.oumen.tools.ELog;
import com.sina.weibo.sdk.api.share.BaseResponse;
import com.sina.weibo.sdk.api.share.IWeiboHandler;
import com.sina.weibo.sdk.api.share.IWeiboShareAPI;
import com.sina.weibo.sdk.constant.WBConstants;
import com.sina.weibo.sdk.exception.WeiboShareException;

/**
 * 邀请好友
 * 
 */

public class InviteFriendActivity extends BaseActivity implements IWeiboHandler.Response{
	private final String STATE_USERPROFILE = "userprofile";

	private final int INVITE_FRIEND_SUCCESS = 1;
	private final int INVITE_FRIEND_FAIL = 2;

	private TitleBar titlebar;
	private Button btnBack, btnSubmit;
	private TextView tvTitle;

	private LinearLayout llFrist, llSecond;
	private TextView tvEmail, tvSina, tvWeiXin;
	private EditText etInput;
	private TextView tvContent;
	private boolean Flag = true;
	private boolean EmailFlag = true;

	private static AuthAdapter auth;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.invite_friend);
		init();
		addListener();
		initData();
		try {
			if (App.USER == null && App.PREFS.getUserProfile() != null) {
				JSONObject obj = new JSONObject(App.PREFS.getUserProfile());
				UserProfile profile = new UserProfile(obj);
				App.USER.copyFrom(profile);
			}
		}
		catch (Exception e) {
			ELog.i("Exception e" + e.toString());
			e.printStackTrace();
		}
	}


	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putSerializable(STATE_USERPROFILE, App.USER);
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		App.USER.copyFrom((UserProfile) savedInstanceState.getSerializable(STATE_USERPROFILE));
		initData();
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected void onDestroy() {
		auth = null;
		super.onDestroy();
	}

	private void initData() {
		tvTitle.setText("邀请好友");
		tvContent.setText(App.USER.getMailMessage());
		llSecond.setVisibility(View.GONE);
		llFrist.setVisibility(View.VISIBLE);
		btnSubmit.setVisibility(View.GONE);
		Flag = true;
	}

	private void addListener() {
		btnBack.setOnClickListener(clickListener);
		btnSubmit.setOnClickListener(clickListener);

		tvEmail.setOnClickListener(clickListener);
		tvSina.setOnClickListener(clickListener);
		tvWeiXin.setOnClickListener(clickListener);
	}

	private void init() {
		titlebar = (TitleBar) findViewById(R.id.titlebar);
		btnBack = titlebar.getLeftButton();
		btnSubmit = titlebar.getRightButton();
		btnSubmit.setText(R.string.submit);
		tvTitle = titlebar.getTitle();

		llFrist = (LinearLayout) findViewById(R.id.ll_invite_first);

		tvEmail = (TextView) findViewById(R.id.tv_invite_email);
		tvSina = (TextView) findViewById(R.id.tv_invite_sina);
		tvWeiXin = (TextView) findViewById(R.id.tv_invite_weixin);

		llSecond = (LinearLayout) findViewById(R.id.ll_invite_sencond);
		etInput = (EditText) findViewById(R.id.et_invite_email);
		tvContent = (TextView) findViewById(R.id.tv_invite_detail);
	}

	private final OnClickListener clickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (v == btnBack) {
				if (Flag) {
					setResult(Activity.RESULT_CANCELED);
					finish();
				}
				else {
					initData();
				}
			}
			else if (v == btnSubmit) {// 提交
				if (EmailFlag) {
					if (TextUtils.isEmpty(etInput.getText().toString()) || !etInput.getText().toString().matches(Constants.PATTERN_EMAIL)) {
						Toast.makeText(mBaseApplication, "邮箱格式不正确", Toast.LENGTH_SHORT).show();
						return;
					}
					if (!App.NetworkType.NONE.equals(App.getNetworkType())) {
						// 邮箱邀请
						EmailInvite(etInput.getText().toString());
					}
					else {
						Toast.makeText(InviteFriendActivity.this, "亲，您的网络没有打开~", Toast.LENGTH_SHORT).show();
					}
				}
				else {
					try {
						auth = AuthAdapter.create(authListener, AuthAdapter.Type.SINA_WEIBO);
						
						if (!TextUtils.isEmpty(App.PREFS.getSinaId()) && !TextUtils.isEmpty(App.PREFS.getSinaToken()) &&  App.PREFS.getSinaExprise() != App.INT_UNSET) {
							ELog.i("" + System.currentTimeMillis());
							if (System.currentTimeMillis() - Long.valueOf(App.PREFS.getSinaExprise()) >= 0) {// 判断授权是否过期
								// 已经授权，向服务器发送请求
								auth.shareInit(InviteFriendActivity.this);
								auth.share(MessageType.TEXT, App.INT_UNSET, null, App.USER.getMailMessage(), null, null);
								initData();
							}
							else {
								// 分享没实现
								// 新浪从新授权
								auth.authorize(InviteFriendActivity.this);
							}
						}
						else {
							// 没有授权
							// 进行新浪授权
							auth.authorize(InviteFriendActivity.this);
						}
					}
					catch (WeiboShareException e) {
						e.printStackTrace();
						Toast.makeText(mBaseApplication, e.getMessage(), Toast.LENGTH_LONG).show();
					}
				}
			}
			else if (v == tvEmail) {
				tvTitle.setText("E-mail邀请");
				llFrist.setVisibility(View.GONE);
				llSecond.setVisibility(View.VISIBLE);
				btnSubmit.setVisibility(View.VISIBLE);
				etInput.setVisibility(View.VISIBLE);
				Flag = false;
				EmailFlag = true;
			}
			else if (v == tvSina) {
				tvTitle.setText("微博邀请");
				llFrist.setVisibility(View.GONE);
				llSecond.setVisibility(View.VISIBLE);
				btnSubmit.setVisibility(View.VISIBLE);
				etInput.setVisibility(View.GONE);
				Flag = false;
				EmailFlag = false;
			}
			else if (v == tvWeiXin) {
				// TODO 微信邀请
				auth =AuthAdapter.create(authListener, AuthAdapter.Type.WEIXIN);
				auth.shareInit(InviteFriendActivity.this);
				auth.share(MessageType.TEXT, WeixinAuthAdapter.SHARE_WEIXIN_CIRCLE, null, App.USER.getMailMessage(), null, null);
			}
		}
	};

	/**
	 * 邮箱邀请
	 * 
	 * @param email
	 */
	private void EmailInvite(String email) {
		if (!TextUtils.isEmpty(email) && !email.matches(Constants.PATTERN_EMAIL)) {
			Toast.makeText(mBaseApplication, "邮箱格式不正确", Toast.LENGTH_SHORT).show();
			return;
		}
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("user_id", String.valueOf(App.PREFS.getUid())));
		params.add(new BasicNameValuePair("f_email", email));

		HttpRequest req = new HttpRequest(Constants.INVITE_FRIEND_URL, params, HttpRequest.Method.POST, inviteFriendCallback);
		App.THREAD.execute(req);
	}

	final DefaultHttpCallback inviteFriendCallback = new DefaultHttpCallback(new EventListener() {

		@Override
		public void onSuccess(HttpResult result) {
			try {
				String response = result.getResult();
				ELog.i(response);
				if ("1".equals(response)) {
					handler.sendMessage(handler.obtainMessage(INVITE_FRIEND_SUCCESS, "E-mail邀请成功"));
				}
				else {
					handler.sendMessage(handler.obtainMessage(INVITE_FRIEND_FAIL, "E-mail邀请失败，请重新邀请"));
				}
			}
			catch (Exception e) {
				ELog.e("Exception e=" + e.toString());
				handler.sendMessage(handler.obtainMessage(INVITE_FRIEND_FAIL, "E-mail邀请失败，请重新邀请"));
				e.printStackTrace();
			}

		}

		@Override
		public void onForceClose(ExceptionHttpResult result) {
			handler.sendMessage(handler.obtainMessage(INVITE_FRIEND_FAIL, "E-mail邀请失败，请重新邀请"));
		}

		@Override
		public void onException(ExceptionHttpResult result) {
			handler.sendMessage(handler.obtainMessage(INVITE_FRIEND_FAIL, "E-mail邀请失败，请重新邀请"));
		}
	});

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
			case INVITE_FRIEND_SUCCESS:
				Toast.makeText(mBaseApplication, (String) msg.obj, Toast.LENGTH_SHORT).show();
				llFrist.setVisibility(View.VISIBLE);
				llSecond.setVisibility(View.GONE);
				break;
			case INVITE_FRIEND_FAIL:
				Toast.makeText(mBaseApplication, (String) msg.obj, Toast.LENGTH_SHORT).show();
				break;
		}
		return super.handleMessage(msg);
	}



	private final AuthListener authListener = new AuthListener() {

		@Override
		public void onFailed(Object obj) {
			ELog.e("Auth failed:" + obj);
			auth = null;
		}

		@Override
		public void onComplete() {
			ELog.i("");
			try {
				String expires = String.valueOf(auth.getExpires());
				if (expires.length() > 10) {
					expires = expires.substring(0, 10);
				}
				// 将第三方授权信息保存到本地App.PREFS
				if (auth.getType() == AuthAdapter.Type.SINA_WEIBO) {
					App.PREFS.setSinaId(auth.getUid());
					App.PREFS.setSinaToken(auth.getAccessToken());
					App.PREFS.setSinaExprise(Long.valueOf(expires));
					// 授权成功后，向服务器发送请求
					auth.shareInit(InviteFriendActivity.this);
					auth.share(MessageType.TEXT, App.INT_UNSET, null, App.USER.getMailMessage(), null, null);
					initData();
				}
			}
			catch (Exception e) {
				ELog.e("Exception e= " + e.toString());
				e.printStackTrace();
			}
		}

		@Override
		public void onCancel() {
			ELog.i("Auth cancel:" + auth.getType());
		}
	};

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		try {
			if (auth != null) {
				ELog.i("");
				auth.onActivityResult(requestCode, resultCode, data);
			}
			ELog.i("");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}


	/**
	 * 接收微客户端博请求的数据。 当微博客户端唤起当前应用并进行分享时，该方法被调用。
	 * 
	 * @param baseRequest
	 *            微博请求数据对象
	 * @see {@link IWeiboShareAPI#handleWeiboRequest}
	 */
	@Override
	public void onResponse(BaseResponse baseResp) {
		ELog.i("");
		switch (baseResp.errCode) {
			case WBConstants.ErrorCode.ERR_OK:
				 Toast.makeText(InviteFriendActivity.this, "分享成功", Toast.LENGTH_LONG).show();
				// 返回到第一个界面
				 initData();
				break;
			case WBConstants.ErrorCode.ERR_CANCEL:
				Toast.makeText(InviteFriendActivity.this, "取消分享", Toast.LENGTH_LONG).show();
				break;
			case WBConstants.ErrorCode.ERR_FAIL:
				Toast.makeText(InviteFriendActivity.this, "分享失败" + "Error Message: " + baseResp.errMsg, Toast.LENGTH_LONG).show();
				break;
		}
	}
}
