package com.oumen.account;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.oumen.R;
import com.oumen.TitleBar;
import com.oumen.android.App;
import com.oumen.android.BaseFragment;
import com.oumen.android.util.Constants;
import com.oumen.auth.AuthAdapter;
import com.oumen.auth.AuthListener;
import com.oumen.home.FloatViewController;
import com.oumen.home.FloatViewHostController;
import com.oumen.home.SoftKeyboardController;
import com.oumen.message.MessageService;
import com.oumen.tools.ELog;
import com.oumen.widget.dialog.PopupButtom;
import com.oumen.widget.dialog.ProgressDialog;
/**
 * 登录界面
 *（邮箱登录，新浪登录，QQ登录）
 */
public class LoginFragment extends BaseFragment implements View.OnTouchListener, FloatViewHostController{
	private String email = null;
	private String password = null;
	
	//浮层
	private RelativeLayout rootContainer;
	private FrameLayout popupLayer;
	//标题行
	private TitleBar titlebar;
	private Button btnRight;
	private Button btnLeft;

	private ImageView sina;
	private ImageView qq;
	private ImageView weixin;
	
	private EditText etEmail, etPwd;
	private TextView tvFindPwd;
	
	private ProgressDialog dialogProgress;
	
	private PopupButtom findPasswordPop;
	
	private boolean PhoneLoginFlag = false;

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
			else if (type == MessageService.TYPE_WEIXIN_AUTH) {
				int result = intent.getIntExtra(MessageService.INTENT_KEY_DATA, 0);
				if (result == MessageService.RESULT_SUCCESS) {//微信授权成功返回
					if (!dialogProgress.isShowing()) {
						dialogProgress.show();
					}
					
					//第三方登录
					Intent LoginIntent = MessageService.createRequestNotify(MessageService.TYPE_LOGIN);
					LoginIntent.putExtra(LoginTask.KEY_FROM, LoginTask.FROM_INPUT);
					LoginIntent.putExtra(LoginTask.KEY_THIRDPARTY, auth.getType());
					LoginIntent.putExtra(LoginTask.KEY_GUEST_ID, String.valueOf(App.PREFS.getUid()));
					getActivity().sendBroadcast(LoginIntent);
				}
				else {// 微信授权失败返回
					auth = null;
				}
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		initAnimation();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		getActivity().registerReceiver(loginReceiver, receiverFilter);
		View view = inflater.inflate(R.layout.login, container, false);
		
		findPasswordPop = new PopupButtom(getActivity());
		findPasswordPop.setHost(this);
		
		view.setOnTouchListener(this);
		
		rootContainer = (RelativeLayout) view.findViewById(R.id.root);
		popupLayer = (FrameLayout) view.findViewById(R.id.layer);
		popupLayer.setOnTouchListener(this);

		titlebar = (TitleBar) view.findViewById(R.id.titlebar);
		titlebar.getTitle().setText(R.string.nav_login_title);
		titlebar.getRightButton().setVisibility(View.GONE);

		btnLeft = titlebar.getLeftButton();
		btnLeft.setBackgroundResource(R.drawable.back_btnselector);
		btnLeft.setOnClickListener(clickListener);

		btnRight = (Button) view.findViewById(R.id.login);
		btnRight.setOnClickListener(clickListener);

		sina = (ImageView) view.findViewById(R.id.iv_newlogin_sina);
		qq = (ImageView) view.findViewById(R.id.iv_newlogin_qq);
		weixin = (ImageView) view.findViewById(R.id.iv_newlogin_weixin);

		etEmail = (EditText) view.findViewById(R.id.et_newlogin_email);
		etPwd = (EditText) view.findViewById(R.id.et_newlogin_pwd);
		tvFindPwd = (TextView) view.findViewById(R.id.tv_login_findpwd);

		sina.setOnClickListener(clickListener);
		qq.setOnClickListener(clickListener);
		weixin.setOnClickListener(clickListener);
		tvFindPwd.setOnClickListener(clickListener);
		findPasswordPop.setOnClickListener(clickListener);
		
		dialogProgress = new ProgressDialog(getActivity());
		dialogProgress.getMessageView().setText("正在登录，请稍后...");
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
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		String[] email = App.PREFS.getEmail();

		if (email != null && !TextUtils.isEmpty(email[0]) && !TextUtils.isEmpty(email[1])) {
			if (Constants.isDebug) {
				byte type = Byte.parseByte(email[0]);
				if (email != null && type == LoginTask.LOGIN_TYPE_OUMEN) {
					if (App.PREFS.isPhoneLogin()) {
						etEmail.setText(email[1].substring(1, 12));
					}
					else {
						etEmail.setText(email[1]);
					}
				}
				else {
					etEmail.setText("78@oumen.com");
				}
				etPwd.setText("aaa");
			}
			else {
				byte type = Byte.parseByte(email[0]);
				if (email != null && type == LoginTask.LOGIN_TYPE_OUMEN) {
					if (App.PREFS.isPhoneLogin()) {
						etEmail.setText(email[1].substring(1, 12));
					}
					else {
						etEmail.setText(email[1]);
					}
				}
				else {
					etEmail.setText("");
					etPwd.setText("");
				}
			}
		}
	}

	private final View.OnClickListener clickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			if (inputManager.isActive()) {
				inputManager.hideSoftInputFromWindow(etEmail.getWindowToken(), 0);
			}
			if (v == sina) {
				if (!App.NetworkType.NONE.equals(App.getNetworkType())) {
					auth = AuthAdapter.create(authListener, AuthAdapter.Type.SINA_WEIBO);
					auth.authorize(getActivity());
				}
				else {
					Toast.makeText(getActivity(), R.string.err_network_invalid, Toast.LENGTH_SHORT).show();
				}
			}
			else if (v == qq) {
				if (!App.NetworkType.NONE.equals(App.getNetworkType())) {
					auth = AuthAdapter.create(authListener, AuthAdapter.Type.QQ);
					auth.authorize(getActivity());
				}
				else {
					Toast.makeText(getActivity(), R.string.err_network_invalid, Toast.LENGTH_SHORT).show();
				}
			}
			else if (v == weixin) {// 微信登录
				if (!App.NetworkType.NONE.equals(App.getNetworkType())) {
					auth = AuthAdapter.create(authListener, AuthAdapter.Type.WEIXIN);
					auth.authorize(getActivity());
				}
				else {
					Toast.makeText(getActivity(), R.string.err_network_invalid, Toast.LENGTH_SHORT).show();
				}
			}
			else if (v == tvFindPwd) {
				showFloatView(findPasswordPop);
			}
			else if (v == btnRight) {//登录
				// 获取用户输入的邮箱和密码信息
				email = etEmail.getText().toString();
				password = etPwd.getText().toString();
				ELog.i("email=" + email + ",password=" + password);
				if (TextUtils.isEmpty(email) || !email.matches(Constants.PATTERN_EMAIL)) {
					if (!email.matches(Constants.PATTERN_TEL)) {
						Toast.makeText(getActivity(), "邮箱格式或者电话号码格式不正确", Toast.LENGTH_SHORT).show();
						return;
					}
					PhoneLoginFlag = true;
				}
				if (TextUtils.isEmpty(password)) {
					Toast.makeText(getActivity(), "密码不能为空，请重新输入~", Toast.LENGTH_SHORT).show();
					return;
				}
				
				if (!App.NetworkType.NONE.equals(App.getNetworkType())) {
					
					login();
				}
				else {
					Toast.makeText(getActivity(), R.string.err_network_invalid, Toast.LENGTH_SHORT).show();
				}
			}
			else if (v == btnLeft) {//返回
				onBackPressed();
			}
			else if (v.getId() == R.id.top_button) {//手机找回密码
				hideFloatView();
				AccountFragment host = (AccountFragment) getFragmentManager().findFragmentById(R.id.container);
				host.switchFragment(AccountFragment.FRAGMENT_PHONE_FIND_PASSWORD);
			}
			else if (v.getId() == R.id.buttom_button) {//邮箱找回密码
				hideFloatView();
//				// 查找密码
				AccountFragment host = (AccountFragment) getFragmentManager().findFragmentById(R.id.container);
				host.switchFragment(AccountFragment.FRAGMENT_FIND_PASSWORD);
			}
		}
	};

	/**
	 * 联网登录
	 */
	private void login() {
		ELog.i("");

		if (!dialogProgress.isShowing()) {
			dialogProgress.show();
		}
		// 设置手机或者邮箱登录标记
		App.PREFS.setPhoneLogin(PhoneLoginFlag);
		
		Intent intent = MessageService.createRequestNotify(MessageService.TYPE_LOGIN);
		intent.putExtra(LoginTask.KEY_FROM, LoginTask.FROM_INPUT);
		intent.putExtra(LoginTask.KEY_INFO_ARRAY, new String[]{email, password});
		intent.putExtra(LoginTask.KEY_GUEST_ID, String.valueOf(App.PREFS.getUid()));
		getActivity().sendBroadcast(intent);
	}

	// ----------第三方登陆------------//
	private AuthAdapter auth;

	private final AuthListener authListener = new AuthListener() {

		@Override
		public void onFailed(Object obj) {
			ELog.e("Auth failed:" + obj);
			auth = null;
		}

		@Override
		public void onComplete() {
			ELog.i("");
			String expires = String.valueOf(auth.getExpires());
			// 将第三方的值保存到prefs里
			if (expires.length() > 10) {
				expires = expires.substring(0, 10);
			}
			if (auth.getType() == AuthAdapter.Type.SINA_WEIBO) {
				App.PREFS.setSinaId(auth.getUid());
				App.PREFS.setSinaToken(auth.getAccessToken());
				App.PREFS.setSinaExprise(Long.valueOf(expires));
			}
			else if (auth.getType() == AuthAdapter.Type.QQ) {
				App.PREFS.setQQId(auth.getUid());
				App.PREFS.setQQToken(auth.getAccessToken());
				App.PREFS.setQQExprise(Long.valueOf(expires));
			}

			if (!dialogProgress.isShowing()) {
				dialogProgress.show();
			}
			
			//第三方登录
			Intent intent = MessageService.createRequestNotify(MessageService.TYPE_LOGIN);
			intent.putExtra(LoginTask.KEY_FROM, LoginTask.FROM_INPUT);
			intent.putExtra(LoginTask.KEY_THIRDPARTY, auth.getType());
			intent.putExtra(LoginTask.KEY_GUEST_ID, String.valueOf(App.PREFS.getUid()));
			getActivity().sendBroadcast(intent);
		}

		@Override
		public void onCancel() {
			ELog.i("Auth cancel:" + auth.getType());
		}
	};

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (auth != null) {
			auth.onActivityResult(requestCode, resultCode, data);
		}
	}

	@Override
	public boolean onBackPressed() {
		if (isFloatViewShowing()) {
			hideFloatView();
		}else {
			getFragmentManager().popBackStack();
		}
		return true;
	}

	@Override
	public void onDestroyView() {
		if (dialogProgress.isShowing()) {
			dialogProgress.dismiss();
		}
		getActivity().unregisterReceiver(loginReceiver);
		super.onDestroyView();
	}
	// ---------- Animation ----------//
	private FloatViewController floatViewController;
	private Animation animBottomIn;
	private Animation animBottomOut;

	private void initAnimation() {
		animBottomIn = AnimationUtils.loadAnimation(getActivity(), R.anim.bottom_in);
		animBottomIn.setAnimationListener(animListener);
		animBottomOut = AnimationUtils.loadAnimation(getActivity(), R.anim.bottom_out);
		animBottomOut.setAnimationListener(animListener);
	}

	private final Animation.AnimationListener animListener = new Animation.AnimationListener() {

		@Override
		public void onAnimationStart(Animation animation) {
		}

		@Override
		public void onAnimationEnd(Animation animation) {
			floatViewController.setPlaying(false);
			if (animation == animBottomOut) {
				rootContainer.removeView(floatViewController.getRoot());
				floatViewController = null;

				popupLayer.setVisibility(View.GONE);
				popupLayer.setOnTouchListener(null);
			}
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
		}
	};

//	@Override
//	public Activity getActivity() {
//		return getActivity();
//	}

	@Override
	public boolean isFloatViewShowing() {
		return floatViewController != null && (floatViewController.isPlaying() || floatViewController.isShowing());
	}

	@Override
	public void showFloatView(FloatViewController controller) {
		if (isFloatViewShowing())
			return;

		floatViewController = controller;

		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

		View container = floatViewController.show();
		rootContainer.addView(container, params);
		container.startAnimation(animBottomIn);

		if (controller instanceof SoftKeyboardController) {
			SoftKeyboardController kb = (SoftKeyboardController) controller;
			kb.showSoftKeyboard();
		}

		popupLayer.setVisibility(View.VISIBLE);
		popupLayer.setOnTouchListener(this);
	}

	@Override
	public void hideFloatView() {
		if (!isFloatViewShowing())
			return;

		View container = floatViewController.hide();
		container.startAnimation(animBottomOut);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (v == popupLayer && event.getAction() == MotionEvent.ACTION_UP) {
			if (floatViewController != null && !floatViewController.isPlaying() && floatViewController.isShowing()) {
				hideFloatView();
			}
		}
		else {
			if (inputManager.isActive()) {
				inputManager.hideSoftInputFromWindow(etEmail.getWindowToken(), 0);
			}
		}
		return true;
	};
}
