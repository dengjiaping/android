package com.oumen.account;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.oumen.R;
import com.oumen.TitleBar;
import com.oumen.android.App;
import com.oumen.android.BaseFragment;
import com.oumen.android.util.Constants;
import com.oumen.message.MessageService;
import com.oumen.widget.dialog.ProgressDialog;
/**
 * 注册界面
 *
 */
public class RegisterFragment extends BaseFragment implements OnTouchListener {
	private View view ;
	private TitleBar titlebar;
	private Button btnBack, btnSubmit;
	private EditText etNickname, etEmail, etPwd, etPwdAgain;
	private TextView tvPirvateitem, tvServeritem;
	
	private ProgressDialog dialogProgress;

	private String nickname = null;
	private String email = null;
	private String pwd = null;
	private String pwdagain = null;

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
	public void onDestroyView() {
		if (dialogProgress.isShowing()) {
			dialogProgress.cancel();
		}
		getActivity().unregisterReceiver(loginReceiver);
		super.onDestroyView();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		getActivity().registerReceiver(loginReceiver, receiverFilter);
		view = inflater.inflate(R.layout.newregister, container, false);
		view.setOnTouchListener(this);
		titlebar = (TitleBar) view.findViewById(R.id.titlebar);
		titlebar.getTitle().setText(R.string.nav_register_title);
		titlebar.getRightButton().setVisibility(View.GONE);
		btnBack = titlebar.getLeftButton();
		btnSubmit = (Button)view.findViewById(R.id.register);
		
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

	final OnClickListener clickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (inputManager.isActive()){
				inputManager.hideSoftInputFromWindow(etPwdAgain.getWindowToken(), 0);
			}
			
			if (v == btnBack) {
				onBackPressed();
			}
			else if (v == btnSubmit) {
				nickname = etNickname.getText().toString();
				email = etEmail.getText().toString();
				pwd = etPwd.getText().toString();
				pwdagain = etPwdAgain.getText().toString();
				if (TextUtils.isEmpty(nickname)) {
					Toast.makeText(getActivity(), "昵称不能为空", Toast.LENGTH_SHORT).show();
					return;
				}
				if (!nickname.matches("^[\u4e00-\u9fa5A-Za-z0-9]{1,8}$")) {
					Toast.makeText(getActivity(), "昵称由汉字、英文、数字组成，不能超过6个字符", Toast.LENGTH_SHORT).show();
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
				if(!App.NetworkType.NONE.equals(App.getNetworkType())){
					if (!dialogProgress.isShowing()) {
						dialogProgress.show();
					}
					
					// 注册联网请求
					Intent req = MessageService.createRequestNotify(MessageService.TYPE_LOGIN);
					req.putExtra(LoginTask.KEY_FROM, LoginTask.FROM_INPUT);
					req.putExtra(LoginTask.KEY_INFO_ARRAY, new String[]{email, pwd, nickname});
					getActivity().sendBroadcast(req);
				}else{
					Toast.makeText(getActivity(), "网络不给力~，请检查网络是否连接", Toast.LENGTH_SHORT).show();
				}
			}
			else if (v == tvPirvateitem) {
				AccountFragment host = (AccountFragment) getFragmentManager().findFragmentById(R.id.container);
				host.switchFragment(AccountFragment.FRAGMENT_DOCUMENT, R.string.serveritem);
			}
			else if (v == tvServeritem) {
				AccountFragment host = (AccountFragment) getFragmentManager().findFragmentById(R.id.container);
				host.switchFragment(AccountFragment.FRAGMENT_DOCUMENT, R.string.notice);
			}
		}
	};

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (inputManager.isActive()){
			inputManager.hideSoftInputFromWindow(etPwdAgain.getWindowToken(), 0);
		}
		return false;
	}

	@Override
	public boolean onBackPressed() {
		getFragmentManager().popBackStack();
		return true;
	}
}
