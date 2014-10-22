package com.oumen.usercenter;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.oumen.R;
import com.oumen.TitleBar;
import com.oumen.android.App;
import com.oumen.android.BaseActivity;
import com.oumen.android.util.Constants;
import com.oumen.http.DefaultHttpCallback;
import com.oumen.http.DefaultHttpCallback.EventListener;
import com.oumen.http.ExceptionHttpResult;
import com.oumen.http.HttpRequest;
import com.oumen.http.HttpResult;
import com.oumen.tools.ELog;
import com.oumen.tools.Tools;

public class UpdatePwdActivity extends BaseActivity {
	final int MODIFY_OLDPWD_SUCCESS = 1;
	final int MODIFY_OLDPWD_FAIL = 2;
	final int MODIFY_NEWPWD_SUCCESS = 3;
	final int MODIFY_NEWPWD_FAIL = 4;

	private TitleBar titlebar;
	private Button btnBack, btnSubmit;
	private EditText etOldpwd, etNewpwd, etNewPwd1;
	private ImageView ivdeloldpwd, ivdelnewpwd, ivdelnewpwd1;
	private ImageView ivconfrimold, ivconfrimnew, ivconfrimnew1;
	private int flag = 0;
	private String pwd = "";
	private String newPwd = "";
	private String newPwd1 = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.updatepwd);
		init();
		addListener();
	}

	/**
	 * 初始化
	 */
	private void init() {
		titlebar = (TitleBar) findViewById(R.id.titlebar);
		titlebar.getTitle().setText(R.string.nav_pwdnotify_title);
		btnBack = titlebar.getLeftButton();
		btnSubmit = titlebar.getRightButton();
		btnSubmit.setText(R.string.update);

		etOldpwd = (EditText) findViewById(R.id.tv_updatepwd_oldpwd);
		etNewpwd = (EditText) findViewById(R.id.tv_updatepwd_newpwd);
		etNewPwd1 = (EditText) findViewById(R.id.tv_updatepwd_newpwd2);

		ivdeloldpwd = (ImageView) findViewById(R.id.iv_updatepwd_delete);
		ivdelnewpwd = (ImageView) findViewById(R.id.iv_updatepwd_delete1);
		ivdelnewpwd1 = (ImageView) findViewById(R.id.iv_updatepwd_delete2);

		ivconfrimold = (ImageView) findViewById(R.id.tv_updatepwd_correct);
		ivconfrimnew = (ImageView) findViewById(R.id.tv_updatepwd_correct1);
		ivconfrimnew1 = (ImageView) findViewById(R.id.tv_updatepwd_correct2);
	}

	private void addListener() {
		btnBack.setOnClickListener(myListener);
		btnSubmit.setOnClickListener(myListener);
		// 三个清空按钮
		ivdeloldpwd.setOnClickListener(myListener);
		ivdelnewpwd.setOnClickListener(myListener);
		ivdelnewpwd1.setOnClickListener(myListener);
		etOldpwd.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					// 获得焦点
				}
				else {
					// 失去焦点
					pwd = etOldpwd.getText().toString();
					if (TextUtils.isEmpty(pwd)) {
						ivconfrimold.setVisibility(View.GONE);
					}
					else {
						if (!App.NetworkType.NONE.equals(App.getNetworkType())){
							confrimPwd();
						}
						else{
							Toast.makeText(UpdatePwdActivity.this, "亲，您的网络没有打开~", Toast.LENGTH_SHORT).show();
						}
					}
				}
			}
		});
		etNewpwd.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					// 获得焦点
				}
				else {
					// 失去焦点
					if (flag == 1) {
						newPwd = etNewpwd.getText().toString().trim();
						if (!TextUtils.isEmpty(newPwd)) {
							if (!newPwd.equals(pwd)) {
								if (newPwd.length() >= 6) {
									ivconfrimnew.setBackgroundResource(R.drawable.mv_preview_play);
									ivconfrimnew.setVisibility(View.VISIBLE);
								}
								else {
									ivconfrimnew.setBackgroundResource(R.drawable.red_delete);
									ivconfrimnew.setVisibility(View.VISIBLE);
									Toast.makeText(mBaseApplication, "新密码长度至少6位", Toast.LENGTH_SHORT).show();
								}
							}
							else {
								ivconfrimnew.setBackgroundResource(R.drawable.red_delete);
								ivconfrimnew.setVisibility(View.VISIBLE);
								Toast.makeText(mBaseApplication, "新密码不能和旧密码相同", Toast.LENGTH_SHORT).show();
							}
						}
						else {
							ivconfrimnew.setVisibility(View.GONE);
						}
					}
				}
			}
		});
		etNewPwd1.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					// 获得焦点
					
				}
				else {
					// 失去焦点
					newPwd1 = etNewPwd1.getText().toString().trim();
					if (TextUtils.isEmpty(newPwd1)) {
						// 如果为空，就隐藏后面的图标
						ivconfrimnew1.setVisibility(View.GONE);
					}
					else {
						if (newPwd1.equals(newPwd)) {
							// 两次输入的密码相同
							ivconfrimnew1.setBackgroundResource(R.drawable.mv_preview_play);
							ivconfrimnew1.setVisibility(View.VISIBLE);
						}
						else {
							// 两次输入的密码不相同
							ivconfrimnew1.setBackgroundResource(R.drawable.red_delete);
							ivconfrimnew1.setVisibility(View.VISIBLE);
							Toast.makeText(mBaseApplication, "两次输入的密码不相同", Toast.LENGTH_SHORT).show();
						}
					}
				}
			}
		});
	}

	/**
	 * 确认密码
	 */
	private void confrimPwd() {
		// 向服务器发送密码验证请求
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("user_id", String.valueOf(App.PREFS.getUid())));
		params.add(new BasicNameValuePair("o_pass", Tools.getEncode("MD5", pwd)));

		HttpRequest req = new HttpRequest(Constants.USERCENTER_OLDPWD, params, HttpRequest.Method.POST, modifyOldPwdCallback);
		App.THREAD.execute(req);
	}

	final DefaultHttpCallback modifyOldPwdCallback = new DefaultHttpCallback(new EventListener() {

		@Override
		public void onSuccess(HttpResult result) {
			try {
				String res = result.getResult();
				ELog.i("确认旧密码是否成功：" + res);
				// 对返回来的数据进行解析
				JSONObject obj = new JSONObject(res);
				String flag = obj.getString("success");
				if ("1".equals(flag)) {
					// 输入旧密码成功
					handler.sendEmptyMessage(MODIFY_OLDPWD_SUCCESS);
				}
				else {
					// 输入密码错误
					handler.sendEmptyMessage(MODIFY_OLDPWD_FAIL);
				}
			}
			catch (Exception e) {
				ELog.e("Exception:" + e.getMessage());
				handler.sendEmptyMessage(MODIFY_OLDPWD_FAIL);
				e.printStackTrace();
			}
		}

		@Override
		public void onForceClose(ExceptionHttpResult result) {

		}

		@Override
		public void onException(ExceptionHttpResult result) {
			handler.sendEmptyMessage(MODIFY_OLDPWD_FAIL);
		}
	});

	/**
	 * 修改密码
	 */
	private void UpdatePwd() {
		// 向服务器发送密码验证请求
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		ELog.i("原来的密码：" + pwd + ",修改以后的密码：" + newPwd);
		params.add(new BasicNameValuePair("user_id", String.valueOf(App.PREFS.getUid())));
		params.add(new BasicNameValuePair("o_pass", Tools.getEncode("MD5", pwd)));
		params.add(new BasicNameValuePair("n_pass", Tools.getEncode("MD5", newPwd)));

		HttpRequest req = new HttpRequest(Constants.USERCENTER_NEWPWD, params, HttpRequest.Method.POST, modifyNewPwdCallback);
		App.THREAD.execute(req);
	}

	final DefaultHttpCallback modifyNewPwdCallback = new DefaultHttpCallback(new EventListener() {

		@Override
		public void onSuccess(HttpResult result) {
			try {
				String res = result.getResult();
				ELog.i(res);
				// 对返回来的数据进行解析
				JSONObject obj = new JSONObject(res);
				String flag = obj.getString("success");
				if ("1".equals(flag)) {
					// 输入旧密码成功
					handler.sendEmptyMessage(MODIFY_NEWPWD_SUCCESS);
				}
				else {
					// 输入密码错误
					handler.sendEmptyMessage(MODIFY_NEWPWD_FAIL);
				}
			}
			catch (Exception e) {
				ELog.e("Exception:" + e.getMessage());
				handler.sendEmptyMessage(MODIFY_OLDPWD_FAIL);
				e.printStackTrace();
			}
		}

		@Override
		public void onForceClose(ExceptionHttpResult result) {

		}

		@Override
		public void onException(ExceptionHttpResult result) {
			handler.sendEmptyMessage(MODIFY_OLDPWD_FAIL);
		}
	});

	final OnClickListener myListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
				case R.id.left:
					setResult(Activity.RESULT_CANCELED);
					finish();
					break;
				case R.id.right:
					newPwd1 = etNewPwd1.getText().toString().trim();
					if (TextUtils.isEmpty(newPwd1)) {
						// 如果为空，就隐藏后面的图标
						ivconfrimnew1.setVisibility(View.GONE);
						return ;
					}
					else {
						if (newPwd1.equals(newPwd)) {
							// 两次输入的密码相同
							ivconfrimnew1.setBackgroundResource(R.drawable.mv_preview_play);
							ivconfrimnew1.setVisibility(View.VISIBLE);
						}
						else {
							// 两次输入的密码不相同
							ivconfrimnew1.setBackgroundResource(R.drawable.red_delete);
							ivconfrimnew1.setVisibility(View.VISIBLE);
							Toast.makeText(mBaseApplication, "两次输入的密码不相同", Toast.LENGTH_SHORT).show();
							return ;
						}
					}
					// 修改密码
					if (newPwd1.equals(newPwd)) {
						if(!App.NetworkType.NONE.equals(App.getNetworkType())){
							showProgressDialog("正在修改密码，请稍后...");
							UpdatePwd();
						}else{
							Toast.makeText(UpdatePwdActivity.this, "亲，您的网络没有打开~", Toast.LENGTH_SHORT).show();
						}
					}
					break;
				case R.id.iv_updatepwd_delete:
					etOldpwd.setText("");
					break;
				case R.id.iv_updatepwd_delete1:
					etNewpwd.setText("");
					break;
				case R.id.iv_updatepwd_delete2:
					etNewPwd1.setText("");
					break;

			}
		}
	};

	@Override
	public void onBackPressed() {
		finish();
	}

	@Override
	public boolean handleMessage(Message msg) {
		// 此处可以更新UI
		switch (msg.what) {
			case MODIFY_OLDPWD_SUCCESS:
				// 输入旧密码成功
				flag = 1;
				ivconfrimold.setBackgroundResource(R.drawable.mv_preview_play);
				ivconfrimold.setVisibility(View.VISIBLE);
				break;

			case MODIFY_OLDPWD_FAIL:
				// 输入密码有误
				flag = 0;
				ivconfrimold.setBackgroundResource(R.drawable.red_delete);
				ivconfrimold.setVisibility(View.VISIBLE);
				Toast.makeText(UpdatePwdActivity.this, "旧密码输入错误，请从新输入~", Toast.LENGTH_SHORT).show();
				etNewpwd.setOnKeyListener(null);
				break;

			case MODIFY_NEWPWD_SUCCESS:
				dismissProgressDialog();
				
				App.PREFS.setPwd(etNewpwd.getText().toString().trim());
				
				finish();
				break;

			case MODIFY_NEWPWD_FAIL:
				// 修改密码失败
				Toast.makeText(mBaseApplication, "修改密码失败", Toast.LENGTH_SHORT).show();
				break;
		}
		return super.handleMessage(msg);
	}
}
