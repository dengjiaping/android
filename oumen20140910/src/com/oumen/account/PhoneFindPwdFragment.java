package com.oumen.account;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.oumen.R;
import com.oumen.TitleBar;
import com.oumen.android.App;
import com.oumen.android.BaseFragment;
import com.oumen.android.util.Constants;
import com.oumen.http.DefaultHttpCallback;
import com.oumen.http.ExceptionHttpResult;
import com.oumen.http.HttpRequest;
import com.oumen.http.HttpResult;
import com.oumen.http.DefaultHttpCallback.EventListener;
import com.oumen.tools.ELog;

/**
 * 手机找回密码
 *
 */
public class PhoneFindPwdFragment extends BaseFragment implements View.OnClickListener {
	private final int HANDLER_TIMER = 10;
	private final int HANDLER_MODIFY_PASSWORD_SUCCESS = 11;
	private final int HANDLER_MODIFY_PASSWORD_FAIL = 12;

	private TitleBar titleBar;
	private Button btnLeft;
	private TextView tip;
	private RelativeLayout codeContainer, PwdContainer;
	private EditText inputPhone, inputCode;
	private Button btnCode;
	private EditText inputPwd, inputConfirmPwd;

	private Button btnNext;

	private boolean FirstStepFlag = true;// 第一步标记

	private PhoneCodeController phoneController;

	private Timer downTimer;
	private int downtime = 60;
	private String phoneCode = null;
	private String phoneNum = null;

	private InputMethodManager inputManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.phone_find_password, container, false);

		phoneController = new PhoneCodeController(handler);

		titleBar = (TitleBar) view.findViewById(R.id.titlebar);
		titleBar.getRightButton().setVisibility(View.GONE);
		titleBar.getTitle().setText(getResources().getString(R.string.find_password));
		btnLeft = titleBar.getLeftButton();

		tip = (TextView) view.findViewById(R.id.tip);
		tip.setText("");

		codeContainer = (RelativeLayout) view.findViewById(R.id.code_container);
		codeContainer.setVisibility(View.VISIBLE);
		PwdContainer = (RelativeLayout) view.findViewById(R.id.pwd_container);
		PwdContainer.setVisibility(View.GONE);

		inputPhone = (EditText) view.findViewById(R.id.phone);
		inputPhone.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				// 如果输入的长度大于或者小于手机号的长度，验证码都不可以点击
				if (s.length() == 11) {
					btnCode.setEnabled(true);
				}
				else {
					btnCode.setEnabled(false);
				}
			}
		});

		inputCode = (EditText) view.findViewById(R.id.code);
		inputCode.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO 如果输入的长度大于或者小于手机号的长度，验证码都不可以点击
				String temp = s.toString();
				ELog.i("phoneCode = " + phoneCode + ",Editable = " + temp);
				if (temp.equals(phoneCode)) {
					inputCode.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.confirm), null);
					// 向服务器发送验证成功的请求
					phoneController.sendCode(phoneNum, phoneCode);
					btnNext.setEnabled(true);
				}
				else {
					btnNext.setEnabled(false);
					inputCode.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.confirm_default), null);
				}
			}
		});
		btnCode = (Button) view.findViewById(R.id.request_code);
		btnCode.setEnabled(false);

		inputPwd = (EditText) view.findViewById(R.id.new_pwd);
		inputConfirmPwd = (EditText) view.findViewById(R.id.confrim_new_pwd);

		btnNext = (Button) view.findViewById(R.id.next);
		btnNext.setEnabled(false);

		btnLeft.setOnClickListener(this);
		btnCode.setOnClickListener(this);
		btnNext.setOnClickListener(this);
		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onDestroyView() {
		stopTimer();
		super.onDestroyView();
	}

	/*
	 * 返回
	 * 有两种情况，1，从设置密码界面返回到手机验证码界面
	 * 2.退出此fragment
	 */
	private void back() {
		if (FirstStepFlag) {
			getFragmentManager().popBackStack();
		}
		else {
			PwdContainer.setVisibility(View.GONE);
			codeContainer.setVisibility(View.VISIBLE);

			btnNext.setText(getResources().getString(R.string.next));
			FirstStepFlag = true;
		}
	}

	@Override
	public void onClick(View v) {
		if (inputManager.isActive()) {
			inputManager.hideSoftInputFromWindow(inputPhone.getWindowToken(), 0);
		}

		if (v == btnLeft) {//返回
			back();
		}
		else if (v == btnCode) {// 获取验证码
			//1.验证手机号码格式是否正确
			String temp = inputPhone.getText().toString().trim();
			if (!temp.matches(Constants.PATTERN_TEL)) {
				tip.setText("手机号码格式不正确");
				return;
			}

			tip.setText("");
			//1.保存电话号码
			phoneNum = temp;
			//1.向服务器发送请求，获取验证码
			phoneController.getPhoneCode(temp, PhoneCodeController.REQUEST_PHONE_CODE_FIND_PWD);
			tip.setText("验证短信已发送至" + temp + ",请稍等!");
			//2.开始倒计时（60s）,btn不可以点击
			downTimer = new Timer();
			downTimer.schedule(new TimerTask() {

				@Override
				public void run() {
					handler.sendEmptyMessage(HANDLER_TIMER);
				}
			}, 0, 1000);
		}
		else if (v == btnNext) {// 下一步，或者重置密码
			if (FirstStepFlag) {
				PwdContainer.setVisibility(View.VISIBLE);
				codeContainer.setVisibility(View.GONE);

				btnNext.setText(getResources().getString(R.string.modify_password));
				FirstStepFlag = false;
			}
			else {
				// 判断两次输入的密码是否相同
				String pwd = inputPwd.getText().toString();
				String confirmPwd = inputConfirmPwd.getText().toString();
				if (TextUtils.isEmpty(pwd) || pwd.length() < 6 || pwd.length() > 10) {
					Toast.makeText(getActivity(), "请输入6—10位由字母和数字组成的密码", Toast.LENGTH_SHORT).show();
					return;
				}
				if (TextUtils.isEmpty(confirmPwd) || !confirmPwd.equals(pwd)) {
					Toast.makeText(getActivity(), "两次输入密码不相同", Toast.LENGTH_SHORT).show();
					return;
				}
				if (!App.NetworkType.NONE.equals(App.getNetworkType())) {
					// TODO　dialogProgress
					// TODO 向服务器发送请求修改密码的请求
					ModifyPassword(phoneNum, pwd);
				}
				else {
					Toast.makeText(getActivity(), "网络不给力~，请检查网络是否连接", Toast.LENGTH_SHORT).show();
				}
			}
		}
	}

	@Override
	public boolean onBackPressed() {
		back();
		return true;
	}

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
			case PhoneCodeController.HANDLER_GET_CODE_SUCCESS:
				String temp = (String) msg.obj;

				if (temp != null && temp.length() == 6) {
					phoneCode = temp;
				}
				break;
			case PhoneCodeController.HANDLER_GET_CODE_FAIL:
				tip.setText((String) msg.obj);
				break;
			case PhoneCodeController.HANDLER_CONFRIM_CODE_SUCCESS:
				tip.setText((String) msg.obj);
				break;
			case PhoneCodeController.HANDLER_CONFRIM_CODE_FAIL:
				tip.setText((String) msg.obj);
				break;
			case HANDLER_TIMER:
				if (downtime > 0) {
					//倒计时开始
					btnCode.setEnabled(false);
					btnCode.setText(downtime + "s后\n重新获取");
					int size = getResources().getDimensionPixelSize(R.dimen.padding_micro);
					btnCode.setPadding(size, 0, size, 0);
					downtime--;
				}
				else {
					btnCode.setEnabled(true);
					btnCode.setText("重新获取");
					int size = getResources().getDimensionPixelSize(R.dimen.padding_small);
					btnCode.setBackgroundResource(R.drawable.new_register_btnselector);
					btnCode.setPadding(size, size, size, size);
					// 重新获取验证码
					stopTimer();
					downtime = 60;
					tip.setText("");
				}
				break;
			case HANDLER_MODIFY_PASSWORD_SUCCESS:
				getFragmentManager().popBackStack();
				break;
			case HANDLER_MODIFY_PASSWORD_FAIL:
				tip.setText((String) msg.obj);
				break;
		}
		return super.handleMessage(msg);
	}

	public void stopTimer() {
		if (downTimer != null) {
			downTimer.cancel();
			downTimer = null;
		}
	}

	private void ModifyPassword(String phone, String password) {
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("mobile", phone));
		params.add(new BasicNameValuePair("password", password));
		HttpRequest req = new HttpRequest(Constants.PHONE_MODIFY_PASSWORD_URL, params, HttpRequest.Method.GET, modifyCodecallback);
		App.THREAD.execute(req);
	}

	private final DefaultHttpCallback modifyCodecallback = new DefaultHttpCallback(new EventListener() {

		@Override
		public void onSuccess(HttpResult result) {
			try {
				String str = result.getResult();
				ELog.i(str);
				JSONObject obj = new JSONObject(str);
				if (obj.getInt("success") == 1) {// 设置密码成功
					handler.sendMessage(handler.obtainMessage(HANDLER_MODIFY_PASSWORD_SUCCESS));
				}
				else {
					handler.sendMessage(handler.obtainMessage(HANDLER_MODIFY_PASSWORD_FAIL, obj.getString("tip")));
				}
				
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onForceClose(ExceptionHttpResult result) {
			handler.sendMessage(handler.obtainMessage(HANDLER_MODIFY_PASSWORD_FAIL, "网络请求异常"));
		}

		@Override
		public void onException(ExceptionHttpResult result) {
			handler.sendMessage(handler.obtainMessage(HANDLER_MODIFY_PASSWORD_FAIL, "网络请求异常"));
		}
	});
}
