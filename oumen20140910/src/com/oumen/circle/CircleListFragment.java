package com.oumen.circle;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.ab.view.pullview.AbPullListView;
import com.oumen.R;
import com.oumen.TitleBar;
import com.oumen.android.App;
import com.oumen.android.BaseFragment;
import com.oumen.android.peers.OumenShareActivity;
import com.oumen.auth.ShareView;
import com.oumen.home.LoginConfrim;
import com.oumen.home.SendMessageView;
import com.oumen.message.MessageService;
import com.oumen.tools.ELog;
import com.oumen.widget.dialog.TwoButtonDialog;

public class CircleListFragment extends BaseFragment {
	public static final int REQURST_NOTICE_MESSAGE = 991;
	public static final int REQUEST_SHARE = 990;
//	protected HomeFragment host;
	protected CircleActivity host;
	//标题行控件
	private TitleBar titlebar;
	private Button btnLeft;
	private Button btnRight;

	protected AbPullListView lstView;
	protected CircleListHeader headerView;
	protected CircleController controller;

	protected ShareView viewShare;
	protected SendMessageView viewSend;
	protected TwoButtonDialog dialogDeleteComment;
	protected TwoButtonDialog dialogDeleteContent;

	public static ImageSpan SPAN_ENJOY_ICON;
	public static ForegroundColorSpan SPAN_ENJOY_TEXT;
	public static ForegroundColorSpan SPAN_NICKNAME;
	
	private LoginConfrim loginConfrim;

	private final IntentFilter receiverFilter = new IntentFilter(MessageService.RESPONSE_ACTION);

	private final BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			int type = intent.getIntExtra(MessageService.INTENT_KEY_TYPE, 0);
			if (type == MessageService.TYPE_CIRCLE_MESSAGE) {// 接收偶们圈消息
				headerView.updateMessage();
			}
			else if (type == MessageService.TYPE_USERINFO) {//更新头部消息
				headerView.update();
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ELog.i("");

//		host = (HomeFragment) getParentFragment();
		host = (CircleActivity) getActivity();
		controller = new CircleController(this);

		viewShare = new ShareView(getActivity());
		viewShare.setHost(host);

		viewSend = new SendMessageView(getActivity());
		viewSend.setButtonClickListener(controller);

		SPAN_ENJOY_ICON = new ImageSpan(getActivity(), R.drawable.oumen_circle_zan_list, DynamicDrawableSpan.ALIGN_BOTTOM);
		SPAN_ENJOY_TEXT = new ForegroundColorSpan(getResources().getColor(R.color.oumen_name));
		SPAN_NICKNAME = new ForegroundColorSpan(getResources().getColor(R.color.text_highlight));
		
		loginConfrim = new LoginConfrim(getActivity());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		getActivity().registerReceiver(receiver, receiverFilter);
		View view = inflater.inflate(R.layout.fragment_peer, container, false);
		titlebar = (TitleBar) view.findViewById(R.id.titlebar);

		btnLeft = titlebar.getLeftButton();
		titlebar.getTitle().setText(getResources().getString(R.string.module_title_quan));
		btnRight = titlebar.getRightButton();

		btnRight.setVisibility(View.VISIBLE);
		btnRight.setText(getResources().getString(R.string.publish));

		btnLeft.setOnClickListener(clickListener);
		btnRight.setOnClickListener(clickListener);

		headerView = new CircleListHeader(container.getContext());
		headerView.setClickListener(controller);
		headerView.update();

		lstView = (AbPullListView) view.findViewById(R.id.list);
		lstView.addHeaderView(headerView);
		lstView.setDivider(new ColorDrawable(getResources().getColor(R.color.oumen_line)));
		lstView.setDividerHeight(1);
		lstView.getFooterView().setFooterProgressBarDrawable(this.getResources().getDrawable(R.drawable.progress_circular));
		lstView.getHeaderView().setVisibility(View.GONE);
		lstView.setAdapter(controller.adapter);
		lstView.setAbOnListViewListener(controller);
		lstView.setSelector(android.R.color.transparent);
		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		controller.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onDestroyView() {
		getActivity().unregisterReceiver(receiver);
		super.onDestroyView();
	}

	@Override
	public void onDestroy() {
		SPAN_ENJOY_ICON = null;
		SPAN_ENJOY_TEXT = null;
		SPAN_NICKNAME = null;
		super.onDestroy();
	}

	public void updateHeaderView() {
		headerView.update();
	}

	private final View.OnClickListener clickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			if (v == btnLeft) {
				getActivity().finish();
			}
			else if (v == btnRight) {
				if (TextUtils.isEmpty(App.PREFS.getUserProfile())) {
					//TODO 跳转到登录界面
					loginConfrim.openDialog();
					return;
				}
				
				Intent intent = new Intent(getActivity(), OumenShareActivity.class);
				getActivity().startActivityForResult(intent, REQUEST_SHARE);
			}

		}
	};

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (viewShare != null) {
			viewShare.onActivityResult(requestCode, resultCode, data);
		}
		if (controller != null) {
			controller.onActivityResult(requestCode, resultCode, data);
		}
		if (requestCode == REQURST_NOTICE_MESSAGE) {// 偶们圈消息提醒
			headerView.updateMessage();
		}
		else if (requestCode == REQUEST_SHARE) {
//			lstView.headerLoad();
			controller.onRefresh();
		}
	}

	public void showDeleteCommentDialog(CircleItemData data) {
		if (dialogDeleteComment == null) {
			dialogDeleteComment = new TwoButtonDialog(getActivity());
			dialogDeleteComment.getTitleView().setText(R.string.default_dialog_title);
			dialogDeleteComment.getMessageView().setText(R.string.circle_confrim_delete_comment);

			dialogDeleteComment.getLeftButton().setOnClickListener(controller);
			dialogDeleteComment.getRightButton().setOnClickListener(controller);
			dialogDeleteComment.setTag(data);
			dialogDeleteComment.show();
		}
	}

	public void hideDeleteCommentDialog() {
		dialogDeleteComment.dismiss();
		dialogDeleteComment = null;
	}

	public void showDeleteContentDialog(CircleItemData data) {
		if (dialogDeleteContent == null) {
			dialogDeleteContent = new TwoButtonDialog(getActivity());
			dialogDeleteContent.getTitleView().setText(R.string.default_dialog_title);
			dialogDeleteContent.getMessageView().setText(R.string.circle_confrim_delete_content);

			dialogDeleteContent.getLeftButton().setOnClickListener(controller);
			dialogDeleteContent.getRightButton().setOnClickListener(controller);
			dialogDeleteContent.setTag(data);
			dialogDeleteContent.show();
		}
	}

	public void hideDeleteContentDialog() {
		dialogDeleteContent.dismiss();
		dialogDeleteContent = null;
	}

	public void showShareView() {
		host.showFloatView(viewShare);
	}

	public void showSendView() {
		host.showFloatView(viewSend);
	}

	public void hideFloatView() {
		host.hideFloatView();
	}

	@Override
	public boolean onBackPressed() {
		boolean processed = false;
		if (dialogDeleteComment != null && dialogDeleteComment.isShowing()) {
			hideDeleteCommentDialog();
			processed = true;
		}
		else if (viewSend != null && viewSend.isShowing()) {
			if (viewSend.biaoQingShow()) {
				viewSend.hiddenBiaoqingView();
			}
			else {
				hideFloatView();
			}
			processed = true;
		}
		return processed;
	}
}
