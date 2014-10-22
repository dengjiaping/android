package com.oumen.home;

import org.json.JSONObject;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.oumen.MainActivity;
import com.oumen.MainActivity.Frag;
import com.oumen.R;
import com.oumen.activity.ActivityIndexFragment;
import com.oumen.android.App;
import com.oumen.android.BaseFragment;
import com.oumen.android.UserProfile;
import com.oumen.android.util.Constants;
import com.oumen.message.MessageFragment;
import com.oumen.message.MessageService;
import com.oumen.message.NearMessage;
import com.oumen.message.NotificationObserver;
import com.oumen.tools.ELog;
import com.oumen.usercenter.UserCenterActivity;
import com.oumen.widget.SlidingPaneLayout;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

/**
 * 主界面
 */
public class HomeFragment extends BaseFragment implements Handler.Callback, View.OnTouchListener, NotificationObserver, FloatViewHostController, SlidingPaneLayout.PanelSlideListener {
	public static final String INTENT_KEY_LOGIN = "login";
	public static final String STATE_CURRENT_FRAGMENT = "current_fragment";

	public static final int REQUEST_CODE_USER_CENTER = 1;
	public static final int REQUEST_CODE_MESSAGE_CENTER = 2;

	private final int HANDLER_SWITCH_FRAGMENT = 0;

	// 主界面
	public static final int TYPE_ACTIVITY = 0;
	public static final int TYPE_MESSAGE = 1;

	private ActivityIndexFragment fragActivity;
	private MessageFragment fragMessage;
	
	private SlidingPaneLayout edgeView;
	private LeftView leftView;// 左侧导航栏的控件信息

	private RelativeLayout rootContainer;//整个布局
	private FrameLayout fragContainer;

	private View btnActivity;
	private View btnMessageCenter;
	private TextView txtMessageTip;//消息中心提醒
	
	private View popupLayer;

	private static int currentFragment = TYPE_ACTIVITY;

	private boolean isHomeInited;//是否初始化标记

	private final IntentFilter receiverFilter = new IntentFilter(MessageService.RESPONSE_ACTION);

	private final BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			int type = intent.getIntExtra(MessageService.INTENT_KEY_TYPE, 0);
			ELog.e("Type:" + type);
			if (type == MessageService.TYPE_REFRESH_NEWS) {//消息中心消息更新
				updateNewCount();
			}
			else if (type == MessageService.TYPE_CIRCLE_MESSAGE) {//偶们圈消息更新
				leftView.updateMessageTip();
			}
			else if (type == MessageService.TYPE_USERINFO) {//更新左侧导航
				initMenuData();
			}
			else if (type == MessageService.TYPE_NOTIFY_LOCATION) {
				leftView.updateLocationCity();
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 将该app注册到微信
		final IWXAPI api = WXAPIFactory.createWXAPI(getActivity(), null);
		api.registerApp(Constants.WEIXIN_APP_ID);

		//取用户信息
		try {
			String res = App.PREFS.getUserProfile();
			if (!TextUtils.isEmpty(res)) {
				JSONObject obj = new JSONObject(res);
				UserProfile profile = new UserProfile(obj);

//				App.PREFS.setLatitude(profile.getLatitude());
//				App.PREFS.setLongitude(profile.getLongitude());

				App.USER.copyFrom(profile);
			}
			else {
				// 向服务器发送请求，获取用户个人信息
				Intent req = MessageService.createRequestNotify(MessageService.TYPE_USERINFO);
				getActivity().sendBroadcast(req);
			}
		}
		catch (Exception e) {
			ELog.e("Exception:" + e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		getActivity().registerReceiver(receiver, receiverFilter);
		
		edgeView = (SlidingPaneLayout) inflater.inflate(R.layout.main, container, false);
		edgeView.setPanelSlideListener(this);
		
		leftView = (LeftView) edgeView.findViewById(R.id.left_view);
		leftView.host = this;
		
		popupLayer = edgeView.findViewById(R.id.layer);
		
		rootContainer = (RelativeLayout) edgeView.findViewById(R.id.root);
		
		fragContainer = (FrameLayout) edgeView.findViewById(R.id.fragment_container);

		btnActivity = edgeView.findViewById(R.id.activity);
		btnActivity.setOnClickListener(clickListener);

		btnMessageCenter = edgeView.findViewById(R.id.message);
		btnMessageCenter.setOnClickListener(clickListener);

		txtMessageTip = (TextView) edgeView.findViewById(R.id.message_tip);
		txtMessageTip.setVisibility(View.GONE);

		//初始化动画
		initAnimation();
		return edgeView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		if (savedInstanceState == null) {
			Bundle args = getArguments();
			if (args != null && args.containsKey(STATE_CURRENT_FRAGMENT)) {
				currentFragment = getArguments().getInt(STATE_CURRENT_FRAGMENT);
			}
		}
		else {
			currentFragment = savedInstanceState.getInt(STATE_CURRENT_FRAGMENT);
		}
		switchFragments();
		
		NearMessage near = new NearMessage();
		near.setListener(new NearMessage.NearListener() {
			
			@Override
			public void onUpdate() {
				if (getActivity() == null) {
					return ;
				}
				// TODO 空指针
				getActivity().sendBroadcast(MessageService.createRequestNotify(MessageService.TYPE_REFRESH_NEWS));
			}
		});
		near.obtainNearData();
	}
	
	@Override
	public void onStart() {
		super.onStart();
		//消息中心提醒
		updateNewCount();
		initMenuData();
		// 偶们圈消息提醒
		leftView.updateMessageTip();
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt(STATE_CURRENT_FRAGMENT, currentFragment);
		super.onSaveInstanceState(outState);
	}

	private final View.OnClickListener clickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			if (v == btnMessageCenter) {
				currentFragment = TYPE_MESSAGE;
				switchFragments();
			}
			else if (v == btnActivity) {
				currentFragment = TYPE_ACTIVITY;
				switchFragments();
			}
		}
	};

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
			case HANDLER_SWITCH_FRAGMENT:
				switchFragments();
				break;
		}
		return false;
	};
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == REQUEST_CODE_USER_CENTER) {
				if (data != null && data.hasExtra(UserCenterActivity.KEY_RESULT_LOGOUT)) {
					MainActivity host = (MainActivity) getActivity();
					host.switchFragment(Frag.ACCOUNT);
				}
			}
			else if (requestCode == REQUEST_CODE_MESSAGE_CENTER) {
				currentFragment = TYPE_MESSAGE;
				switchFragments();
			}
		}
	}

	/**
	 * 初始化Fragment
	 */
	public void switchFragments() {
		FragmentTransaction manager = getChildFragmentManager().beginTransaction(); 
		switch (currentFragment) {
			case TYPE_MESSAGE:// 聊天界面
				btnMessageCenter.setBackgroundResource(R.color.home_tab_pressed);
				btnActivity.setBackgroundResource(R.color.home_tab_normal);
				if (fragMessage == null) {
					fragMessage = new MessageFragment();
				}
				
				fragContainer.setBackgroundColor(getResources().getColor(R.color.white));
				manager.replace(R.id.fragment_container, fragMessage).commitAllowingStateLoss();
				
				edgeView.setIntercept(SlidingPaneLayout.Intercept.SUPER);
				break;

			case TYPE_ACTIVITY:// 活动界面
				btnMessageCenter.setBackgroundResource(R.color.home_tab_normal);
				btnActivity.setBackgroundResource(R.color.home_tab_pressed);

				if (fragActivity == null) {
					fragActivity = new ActivityIndexFragment();
				}
				fragContainer.setBackgroundColor(getResources().getColor(R.color.white));
				manager.replace(R.id.fragment_container, fragActivity).commitAllowingStateLoss();
				
				edgeView.setIntercept(SlidingPaneLayout.Intercept.FALSE);
				break;
		}
	}

	public void setCurrentFragment(int type) {
		currentFragment = type;
	}

	/**
	 * 初始化一些左侧menu信息
	 */
	private void initMenuData() {
		//第一次，请求消息提醒
		if (!isHomeInited) {
			Intent req = MessageService.createRequestNotify(MessageService.TYPE_REFRESH_NEWS);
			getActivity().sendBroadcast(req);
			isHomeInited = true;
		}

		leftView.update();
	}
	
	private int getOumenCircleMsg() {
		return leftView.updateMessageTip();
	}
	
	/**
	 * 是否显示导航栏消息提醒
	 * @return
	 */
	public boolean TitleBarMsgTipVisible() {
		int len = getOumenCircleMsg();
		if (len > 0) {
			return true;
		}
		else {
			if (App.PREFS.isFirstToUserCenter() || App.PREFS.isFristToMV() || App.PREFS.isFirstToCircle() || App.PREFS.isFirstToSetting()) {
				return true;
			}
			else {
				return false;
			}
		}
	}

	/**
	 * 更新消息中心消息提醒
	 */
	private void updateNewCount() {
		int count = MessageService.NEWS.get();
		if (count > 0) {
			txtMessageTip.setVisibility(View.VISIBLE);
			if (count > 99) {
				txtMessageTip.setText("99+");
			}
			else {
				txtMessageTip.setText(String.valueOf(count));
			}
		}
		else {
			txtMessageTip.setVisibility(View.GONE);
		}
	}

	/**
	 * 左侧导航关闭与打开
	 */
	public void menuToggle() {
		if (edgeView.isOpen()) {
			edgeView.closePane();
		}
		else {
			edgeView.openPane();
		}
	}
	
	@Override
	public void onPanelSlide(View panel, float slideOffset) {}
	
	@Override
	public void onPanelOpened(View panel) {
		popupLayer.setVisibility(View.VISIBLE);
		popupLayer.setOnTouchListener(this);
	}
	
	@Override
	public void onPanelClosed(View panel) {
		popupLayer.setVisibility(View.GONE);
		popupLayer.setOnTouchListener(null);
	}

	@Override
	public void onDestroyView() {
		ELog.i("");
		super.onDestroyView();
	}
	@Override
	public void onDestroy() {
		getActivity().unregisterReceiver(receiver);
		super.onDestroy();
	}

	@Override
	public boolean onBackPressed() {
		BaseFragment current = (BaseFragment) getChildFragmentManager().findFragmentById(R.id.fragment_container);
		if (current != null) {
			boolean processed = current.onBackPressed();
			if (processed) {
				return true;
			}
		}
		if (isFloatViewShowing()) {
			hideFloatView();
			return true;
		}
		if (edgeView.isOpen()) {
			edgeView.closePane();
			return true;
		}
		return false;
	}

	//----------------------- NotificationObserver -----------------------//
	@Override
	public int getTargetId() {
		return 0;
	}

	@Override
	public Page getCurrentPage() {
		FragmentManager manager = getChildFragmentManager();
		Fragment current = manager.findFragmentById(R.id.container);
		return current instanceof MessageFragment ? Page.MESSAGE : Page.OTHER;
	}

	@Override
	public void receiveNotification(Object... params) {}

	//----------------------- Animation -----------------------//
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
		public void onAnimationStart(Animation animation) {}

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
		public void onAnimationRepeat(Animation animation) {}
	};

	//----------------------- FloatViewHostController -----------------------//
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
			if (edgeView.isOpen()) {
				edgeView.closePane();
			}
			else if (floatViewController != null && !floatViewController.isPlaying() && floatViewController.isShowing()) {
				hideFloatView();
			}
		}
		return true;
	}

	public View getPopupWindowLayer() {
		return popupLayer;
	}

	public void addViewToRoot(View view, LayoutParams params) {
		rootContainer.addView(view, params);
	}

	public void removeViewFromRoot(View view) {
		rootContainer.removeView(view);
	}
}
