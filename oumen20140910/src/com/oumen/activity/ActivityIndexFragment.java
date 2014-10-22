package com.oumen.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.oumen.FloatWindowController;
import com.oumen.R;
import com.oumen.TitleBar;
import com.oumen.activity.widget.IndexViewPager;
import com.oumen.android.App;
import com.oumen.android.App.NetworkType;
import com.oumen.android.BaseFragment;
import com.oumen.home.HomeFragment;
import com.oumen.message.MessageService;
import com.oumen.tools.ELog;

public class ActivityIndexFragment extends BaseFragment {
	final String CACHE_KEY = "activities";

	final ActivityIndexController controller = new ActivityIndexController(this);

	TitleBar barTitle;
	IndexViewPager pager;
	
	View btnHuWai;
	View btnShiNei;
	View btnLvYou;
	View btnXianShang;
	
	ImageView fujin;

	FloatWindowController controllerFloat;

	Animation animIn;
	Animation animOut;

	HomeFragment host;

	private final IntentFilter receiverFilter = new IntentFilter(MessageService.RESPONSE_ACTION);

	private final BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			int type = intent.getIntExtra(MessageService.INTENT_KEY_TYPE, 0);
			if (type == MessageService.TYPE_CIRCLE_MESSAGE) {
				//偶们圈消息更新
				circleMsgTip();
			}
			else if (type == MessageService.TYPE_USERINFO) {
				circleMsgTip();
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		animIn = AnimationUtils.loadAnimation(getActivity(), R.anim.top_in);
		animOut = AnimationUtils.loadAnimation(getActivity(), R.anim.top_out);

		host = (HomeFragment) getParentFragment();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		getActivity().registerReceiver(receiver, receiverFilter);

		View root = inflater.inflate(R.layout.activity_index, container, false);
		
		FrameLayout layerLayout = (FrameLayout) root.findViewById(R.id.layer);
		layerLayout.setOnTouchListener(controller);
		
		barTitle = (TitleBar) root.findViewById(R.id.titlebar);
		barTitle.getTitle().setText(R.string.activity_title);
		barTitle.getLeftButton().setOnClickListener(controller);
		barTitle.getLeftButton().setBackgroundResource(R.drawable.nav_btnselector);
		
		barTitle.getRightButton().setText(R.string.search);
		barTitle.getRightButton().setVisibility(View.VISIBLE);
		barTitle.getRightButton().setOnClickListener(controller);

		circleMsgTip();

		pager = (IndexViewPager) root.findViewById(R.id.pager);
		pager.setPagerViewLayoutParams();
		pager.setVisibility(View.GONE);

		btnHuWai = root.findViewById(R.id.huwai);
		btnHuWai.setOnClickListener(controller);
		btnShiNei = root.findViewById(R.id.shinei);
		btnShiNei.setOnClickListener(controller);
//		btnLvYou = root.findViewById(R.id.lvyou);
//		btnLvYou.setOnClickListener(controller);
		btnXianShang = root.findViewById(R.id.xianshang);
		btnXianShang.setOnClickListener(controller);
		
		fujin = (ImageView) root.findViewById(R.id.fujin);
		fujin.setOnClickListener(controller);

		controllerFloat = new FloatWindowController(layerLayout);

		String res = App.CACHE.read(CACHE_KEY);
		if (!TextUtils.isEmpty(res)) {
			String today = res.substring(0, 8);
			res = res.substring(8);
			if (today.equals(controller.today.toString())) {
				try {
					pager.onSuccess(res);
					pager.notifyDataSetChanged();
				}
				catch (Exception e) {
					ELog.e("Exception:" + e.getMessage());
					e.printStackTrace();
				}
			}
		}
		return root;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		if (!NetworkType.NONE.equals(App.getNetworkType())) {
			pager.obtainBanner(App.PREFS.getUid(), controller.getObtainBannerDataListener());
		}
	}
	
	@Override
	public void onStart() {
		if (pager != null) {
			pager.stopPlay();
		}
		super.onStart();
	}

	public void circleMsgTip() {
		if (host.TitleBarMsgTipVisible()) {
			barTitle.setTipViewVisible(View.VISIBLE);
		}
		else {
			barTitle.setTipViewVisible(View.GONE);
		}
	}

	public boolean isPopupWindowShown() {
		if (controllerFloat == null)
			return false;
		
		return controllerFloat.isShown() || controllerFloat.isPlaying();
	}

	public boolean isPopupWindowPlaying() {
		return controllerFloat.isPlaying();
	}

	public void togglePopupWindow() {
		if (controllerFloat.isPlaying())
			return;

		controllerFloat.startAnimation(controllerFloat.isShown() ? animOut : animIn);
	}

	public void resetPopupWindow() {
		controllerFloat.reset();
	}

	@Override
	public boolean onBackPressed() {
		if (isPopupWindowShown()) {
			togglePopupWindow();
			return true;
		}
		return super.onBackPressed();
	}
	
	@Override
	public void onStop() {
		ELog.i("");
		pager.stopPlay();
		super.onStop();
	}

	@Override
	public void onDestroyView() {
		ELog.i("");
		getActivity().unregisterReceiver(receiver);
		pager.stopPlay();
		super.onDestroyView();
	}
}
