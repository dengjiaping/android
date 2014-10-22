package com.oumen.account;

import java.util.Timer;
import java.util.TimerTask;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
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
import com.oumen.message.MessageService;
import com.oumen.tools.ELog;
import com.oumen.widget.dialog.ProgressDialog;

/**
 * 注册界面(邮箱注册和手机注册)
 *
 */
public class TwoTypeRegisterFragment extends BaseFragment implements OnTouchListener {
	private final int HANDLER_TIMER = 10;
	private TitleBar titlebar;//标题行
	private Button btnBack, btnSubmit;

	private TextView tabEmail, tabPhone;
	private TextView tip;//提示
	//邮箱注册和手机注册对应的容器
	private RelativeLayout containerEmail, containerPhone;
	//邮箱注册组件
	private EditText etNickname, etEmail, etPwd, etPwdAgain;

	// 新增手机注册组件
	private EditText etPhone, etCode, etPhonePwd;
	private Button codeRequest;

	//服务条款
	private TextView tvPirvateitem, tvServeritem;

	private ProgressDialog dialogProgress;

	private String nickname = null;
	private String email = null;
	private String pwd = null;
	private String pwdagain = null;
	private String phoneCode = null;
	private String phoneNum = null;
	private String phonePwd = null;

	private boolean isPhoneRegister = true;

	private PhoneCodeController phoneController;

	private Timer downTimer;
	private int downtime = 60;

	private InputMethodManager inputManager;

	private final IntentFilter receiverFilter = new IntentFilter(MessageService.RESPONSE_ACTION);
	private final BroadcastReceiver loginReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			int type = intent.getIntExtra(MessageService.INTENT_KEY_TYPE, 0);
			if (type == MessageService.TYPE_LOGIN) {
				if (dialogProgress.isShowing()) {
					dialogProgress.dismiss();
				}
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		getActivity().registerReceiver(loginReceiver, receiverFilter);
		View view = inflater.inflate(R.layout.twotype_register, container, false);

		phoneController = new PhoneCodeController(handler);
		view.setOnTouchListener(this);

		titlebar = (TitleBar) view.findViewById(R.id.titlebar);
		titlebar.getTitle().setText(R.string.nav_register_title);
		titlebar.getRightButton().setVisibility(View.GONE);
		btnBack = titlebar.getLeftButton();
		btnSubmit = (Button) view.findViewById(R.id.register);
		btnSubmit.setEnabled(false);

		tabEmail = (TextView) view.findViewById(R.id.tab_email);
		tabPhone = (TextView) view.findViewById(R.id.tab_phone);

		tip = (TextView) view.findViewById(R.id.tip);
		tip.setText("");

		containerEmail = (RelativeLayout) view.findViewById(R.id.container_email_register);
		containerEmail.setVisibility(View.GONE);
		containerPhone = (RelativeLayout) view.findViewById(R.id.container_phone_register);
		containerPhone.setVisibility(View.VISIBLE);

		etPhone = (EditText) view.findViewById(R.id.newregister_phone);
		etPhone.addTextChangedListener(new TextWatcher() {

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
					codeRequest.setEnabled(true);
				}
				else {
					codeRequest.setEnabled(false);
				}
			}
		});
		codeRequest = (Button) view.findViewById(R.id.request_code);
		codeRequest.setEnabled(false);
		etCode = (EditText) view.findViewById(R.id.newregister_code);
		etCode.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				String temp = s.toString();
				ELog.i("phoneCode = " + phoneCode + ",Editable = " + temp);
				if (temp.equals(phoneCode)) {
					etCode.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.confirm), null);
					// 向服务器发送验证成功的请求
					phoneController.sendCode(phoneNum, phoneCode);
					btnSubmit.setEnabled(true);
				}
				else {
					etCode.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.confirm_default), null);
					btnSubmit.setEnabled(false);
				}
			}
		});

		etPhonePwd = (EditText) view.findViewById(R.id.phone_password);

		etNickname = (EditText) view.findViewById(R.id.register_nickname);
		etEmail = (EditText) view.findViewById(R.id.newregister_email);
		etPwd = (EditText) view.findViewById(R.id.et_newregister_pwd);
		etPwdAgain = (EditText) view.findViewById(R.id.et_newregister_pwdagain);
		tvPirvateitem = (TextView) view.findViewById(R.id.tv_newregister_privateitem);
		tvPirvateitem.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);// 下划线
		tvServeritem = (TextView) view.findViewById(R.id.tv_newregister_serveitem);
		tvServeritem.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);// 下划线

		btnBack.setOnClickListener(clickListener);
		btnSubmit.setOnClickListener(clickListener);
		tvPirvateitem.setOnClickListener(clickListener);
		tvServeritem.setOnClickListener(clickListener);
		tabEmail.setOnClickListener(clickListener);
		tabPhone.setOnClickListener(clickListener);
		codeRequest.setOnClickListener(clickListener);

		dialogProgress = new ProgressDialog(getActivity());
		dialogProgress.getMessageView().setText("正在注册，请稍后...");
		dialogProgress.setCancelable(true);
		dialogProgress.setOnCancelListener(new DialogInterface.OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				getActivity().sendBroadcast(MessageService.createRequestNotify(MessageService.TYPE_LOGIN_INTERRUPT));
			}
		});
		return view;
	}

	@Override
	public void onDestroyView() {
		if (dialogProgress.isShowing()) {
			dialogProgress.cancel();
		}
		getActivity().unregisterReceiver(loginReceiver);

		stopTimer();
		super.onDestroyView();
	}

	@Override
	public void onDestroy() {
		stopTimer();
		super.onDestroy();
	}

	final OnClickListener clickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (inputManager.isActive()) {
				inputManager.hideSoftInputFromWindow(etPwdAgain.getWindowToken(), 0);
			}

			if (v == btnBack) {
				onBackPressed();
			}
			else if (v == btnSubmit) {
				register();
			}
			else if (v == tvPirvateitem) {
				AccountFragment host = (AccountFragment) getFragmentManager().findFragmentById(R.id.container);
				host.switchFragment(AccountFragment.FRAGMENT_DOCUMENT, R.string.serveritem);
			}
			else if (v == tvServeritem) {
				AccountFragment host = (AccountFragment) getFragmentManager().findFragmentById(R.id.container);
				host.switchFragment(AccountFragment.FRAGMENT_DOCUMENT, R.string.notice);
			}
			else if (v == tabEmail) {
				if (containerEmail.getVisibility() == View.VISIBLE) {
					return;
				}
				tabEmail.setTextColor(getResources().getColor(R.color.default_bg));
				tabEmail.setBackgroundResource(R.drawable.register_tab_on_btnselector);
				tabPhone.setTextColor(getResources().getColor(R.color.register_grey_text));
				tabPhone.setBackgroundResource(R.drawable.register_tab_btnselector);

				containerEmail.setVisibility(View.VISIBLE);
				containerPhone.setVisibility(View.GONE);

				isPhoneRegister = false;
				tip.setText("");
				btnSubmit.setEnabled(true);
			}
			else if (v == tabPhone) {
				if (containerPhone.getVisibility() == View.VISIBLE) {
					return;
				}
				tabPhone.setTextColor(getResources().getColor(R.color.default_bg));
				tabPhone.setBackgroundResource(R.drawable.register_tab_on_btnselector);
				tabEmail.setTextColor(getResources().getColor(R.color.register_grey_text));
				tabEmail.setBackgroundResource(R.drawable.register_tab_btnselector);

				containerPhone.setVisibility(View.VISIBLE);
				containerEmail.setVisibility(View.GONE);

				isPhoneRegister = true;
				tip.setText("");
			}
			else if (v == codeRequest) {// 获取验证码
				//1.验证手机号码格式是否正确
				String temp = etPhone.getText().toString().trim();
				if (!temp.matches(Constants.PATTERN_TEL)) {
					tip.setText("手机号码格式不正确");
					return;
				}

				tip.setText("");
				//1.保存电话号码
				phoneNum = temp;
				//1.向服务器发送请求，获取验证码
				phoneController.getPhoneCode(temp, PhoneCodeController.REQUEST_PHONE_CODE_REGISTER);
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
		}
	};

	/**
	 * 注册
	 */
	private void register() {
		tip.setText("");
		if (isPhoneRegister) {// 手机注册
			phonePwd = etPhonePwd.getText().toString();
			if (TextUtils.isEmpty(phonePwd) || phonePwd.length() < 6 || phonePwd.length() > 10) {
				Toast.makeText(getActivity(), "请输入6—10位由字母和数字组成的密码", Toast.LENGTH_SHORT).show();
				return;
			}

			if (!App.NetworkType.NONE.equals(App.getNetworkType())) {
				if (!dialogProgress.isShowing()) {
					dialogProgress.show();
				}

				// 注册联网请求
				Intent req = MessageService.createRequestNotify(MessageService.TYPE_LOGIN);
				req.putExtra(LoginTask.KEY_FROM, LoginTask.FROM_INPUT);
				req.putExtra(LoginTask.KEY_INFO_ARRAY, new String[] { phoneNum, phonePwd, "phone", LoginTask.FLAG_REGISTER_PHONE });
				getActivity().sendBroadcast(req);
			}
			else {
				Toast.makeText(getActivity(), "网络不给力~，请检查网络是否连接", Toast.LENGTH_SHORT).show();
			}
		}
		else {
			nickname = etNickname.getText().toString();
			email = etEmail.getText().toString();
			pwd = etPwd.getText().toString();
			pwdagain = etPwdAgain.getText().toString();
			if (TextUtils.isEmpty(nickname)) {
				Toast.makeText(getActivity(), "昵称不能为空", Toast.LENGTH_SHORT).show();
				return;
			}
			if (!nickname.matches("^[\u4e00-\u9fa5A-Za-z0-9]{1,8}$")) {
				Toast.makeText(getActivity(), "昵称由汉字、英文、数字组成，不能超过8个字符", Toast.LENGTH_SHORT).show();
				return;
			}
			if (!TextUtils.isEmpty(email) && !email.matches(Constants.PATTERN_EMAIL)) {
				Toast.makeText(getActivity(), "邮箱格式不正确", Toast.LENGTH_SHORT).show();
				return;
			}
			if (TextUtils.isEmpty(pwd) || pwd.length() < 6 || pwd.length() > 10) {
				Toast.makeText(getActivity(), "请输入6—10位由字母和数字组成的密码", Toast.LENGTH_SHORT).show();
				return;
			}
			if (TextUtils.isEmpty(pwdagain) || !pwdagain.equals(pwd)) {
				Toast.makeText(getActivity(), "两次输入密码不相同", Toast.LENGTH_SHORT).show();
				return;
			}
			if (!App.NetworkType.NONE.equals(App.getNetworkType())) {
				if (!dialogProgress.isShowing()) {
					dialogProgress.show();
				}

				// 注册联网请求
				Intent req = MessageService.createRequestNotify(MessageService.TYPE_LOGIN);
				req.putExtra(LoginTask.KEY_FROM, LoginTask.FROM_INPUT);
				req.putExtra(LoginTask.KEY_INFO_ARRAY, new String[] { email, pwd, nickname, LoginTask.FLAG_REGISTER_EMAIL });
				getActivity().sendBroadcast(req);
			}
			else {
				Toast.makeText(getActivity(), "网络不给力~，请检查网络是否连接", Toast.LENGTH_SHORT).show();
			}
		}

	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (inputManager.isActive()) {
			inputManager.hideSoftInputFromWindow(etPwdAgain.getWindowToken(), 0);
		}
		return false;
	}

	@Override
	public boolean onBackPressed() {
		getFragmentManager().popBackStack();
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
					codeRequest.setEnabled(false);
					codeRequest.setText(downtime + "s后\n重新获取");
					int size = getResources().getDimensionPixelSize(R.dimen.padding_micro);
					codeRequest.setPadding(size, 0, size, 0);
					downtime--;
				}
				else {
					codeRequest.setEnabled(true);
					codeRequest.setBackgroundResource(R.drawable.new_register_btnselector);
					codeRequest.setText("重新获取");
					int size = getResources().getDimensionPixelSize(R.dimen.padding_small);
					codeRequest.setPadding(size, size, size, size);
					// 重新获取验证码
					stopTimer();
					downtime = 60;
					tip.setText("");
				}
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

}
