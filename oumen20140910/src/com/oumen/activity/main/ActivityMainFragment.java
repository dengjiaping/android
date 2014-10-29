package com.oumen.activity.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.oumen.R;
import com.oumen.TitleBar;
import com.oumen.android.App;
import com.oumen.android.BaseFragment;
import com.oumen.android.App.NetworkType;
import com.oumen.base.Cache;
import com.oumen.home.HomeFragment;
import com.oumen.message.MessageService;
import com.oumen.tools.ELog;
import com.oumen.widget.refushlist.AbPullListView;

public class ActivityMainFragment extends BaseFragment {
	final String CACHE_KEY = "activities";
	
	// 标题行控件
	TitleBar titlebar;
	Button btnleft, btnRight;
	TextView title;
	ActivityMainListHeader header;
	AbPullListView listview;
	HomeFragment host;
	
	ActivityMainController controller;

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
		host = (HomeFragment) getParentFragment();
		controller = new ActivityMainController(this);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		getActivity().registerReceiver(receiver, receiverFilter);
		
		View view = inflater.inflate(R.layout.fragment_activity_main, container, false);
		
		titlebar = (TitleBar) view.findViewById(R.id.titlebar);
		title = titlebar.getTitle();
		title.setBackgroundResource(R.drawable.login_edit_normal);
		title.setCompoundDrawablesRelative(getResources().getDrawable(R.drawable.search_bar_icon_normal), null, null, null);
		title.setText("搜索活动");
		title.setPadding(20, 10, 20, 10);
		btnleft = titlebar.getLeftButton();
		btnleft.setBackgroundResource(R.drawable.nav_btnselector);
		btnRight = titlebar.getRightButton();
		btnRight.setCompoundDrawablesRelative(null, null, getResources().getDrawable(R.drawable.title_arrow), null);
		
		header = new ActivityMainListHeader(host.getActivity());
		
		listview = (AbPullListView) view.findViewById(R.id.listview);
		listview.setBackgroundColor(getResources().getColor(R.color.white));
		listview.setDivider(new ColorDrawable(getResources().getColor(R.color.transparent)));
		listview.setDividerHeight(0);
		listview.setSelector(android.R.color.transparent);
		listview.addHeaderView(header);
		listview.setAdapter(controller.adapter);
		listview.setAbOnListViewListener(controller);
		
		title.setOnClickListener(controller);
		btnleft.setOnClickListener(controller);
		btnRight.setOnClickListener(controller);
		
		// 读取活动轮播图缓存
		String res = App.CACHE.read(CACHE_KEY);
		if (!TextUtils.isEmpty(res)) {
			String today = res.substring(0, 8);
			res = res.substring(8);
			if (today.equals(controller.today.toString())) {
				try {
					header.getImageSwitchView().onSuccess(res);
					header.getImageSwitchView().notifyDataSetChanged();
				}
				catch (Exception e) {
					ELog.e("Exception:" + e.getMessage());
					e.printStackTrace();
				}
			}
		}
		return view;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		controller.onViewCreated(view, savedInstanceState);
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	public void circleMsgTip() {
		if (host.TitleBarMsgTipVisible()) {
			titlebar.setTipViewVisible(View.VISIBLE);
		}
		else {
			titlebar.setTipViewVisible(View.GONE);
		}
	}
}
