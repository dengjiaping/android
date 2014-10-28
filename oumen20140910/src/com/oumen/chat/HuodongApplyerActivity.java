package com.oumen.chat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.oumen.R;
import com.oumen.TitleBar;
import com.oumen.activity.detail.HuodongHttpController;
import com.oumen.activity.message.ActivityBean;
import com.oumen.android.App;
import com.oumen.android.BaseActivity;
import com.oumen.home.LoginConfrim;
import com.oumen.message.ActionType;
import com.oumen.message.ChatMessage;
import com.oumen.message.MessageService;
import com.oumen.message.MultiChatMessage;
import com.oumen.message.SendType;
import com.oumen.message.Type;
import com.oumen.widget.dialog.TwoButtonDialog;
import com.oumen.widget.image.shape.RoundRectangleImageView;

/**
 * 活动报名列表界面
 * 
 */
public class HuodongApplyerActivity extends BaseActivity {
	public static final int HANDLER_UPDATE_MESSAGE = 1;
	public static final String BACK_FROM_APPLYER_LIST = "back_applyers";
	public static final String HUODONG_MSG = "HuodongApplyerActivity";

	public static final int BACK_CLOSE = 1;
	public static final int BACK_UPDATE = 2;
	/*
	 * 增加此字段是为了区分从哪个界面过来
	 * 1.从聊天界面过来，清空数据返回后要刷新聊天列表，退出以后要返回到消息中心界面
	 * 2.从活动详情界面过来，清空数据返回没有什么变化，退出以后也要刷新数据
	 */
	public static final String FROM_ACTIVITY_TAG = "from_activity";
	public static final int FROM_HUODONG_DETAIL = 1;
	public static final int FROM_CHAT = 2;

	private ScrollView scrollview;
	//标题行
	private TitleBar titlebar;
	private Button btnLeft;

	private TextView sendNickName, senderChat;//群主信息
	private RoundRectangleImageView senderPhoto;

	private MeasureGridView gvApplyers;//报名列表

	private LinearLayout msgContainer;

	private Button btnAcceptMsg;
	private FrameLayout exitContainer;
	private FrameLayout clearContainer;
	private Button btnClearMsg;

	private Button btnExit;

	private ApplyerGridAdapter adapter;

	private ActivityBean bean;

	private HuodongHttpController controller;

	private boolean openNotice = true;
	private TwoButtonDialog noticeDialog;
	private int from = FROM_HUODONG_DETAIL;

	private LoginConfrim loginConfirm;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.huodong_applyer_list);

		init();

		controller = new HuodongHttpController(handler);
		loginConfirm = new LoginConfrim(this);
		bean = getIntent().getParcelableExtra(HUODONG_MSG);
		from = getIntent().getIntExtra(FROM_ACTIVITY_TAG, FROM_HUODONG_DETAIL);
		if (bean != null) {
			handler.sendEmptyMessage(HANDLER_UPDATE_MESSAGE);
		}
	}

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
			case HANDLER_UPDATE_MESSAGE:
				update(bean);
				break;
		}
		return super.handleMessage(msg);
	}

	private void init() {
		scrollview = (ScrollView) findViewById(R.id.scrollview);
		titlebar = (TitleBar) findViewById(R.id.titlebar);
		titlebar.getRightButton().setVisibility(View.GONE);
		btnLeft = titlebar.getLeftButton();

		sendNickName = (TextView) findViewById(R.id.nickname);
		senderPhoto = (RoundRectangleImageView) findViewById(R.id.sender_photo);
		senderPhoto.setRadius(5);
		senderChat = (TextView) findViewById(R.id.chat);

		gvApplyers = (MeasureGridView) findViewById(R.id.baoming_gridview);
		gvApplyers.setClickable(false);
		adapter = new ApplyerGridAdapter();
		gvApplyers.setAdapter(adapter);

		msgContainer = (LinearLayout) findViewById(R.id.sender_msg_container);
		msgContainer.setVisibility(View.GONE);

		btnAcceptMsg = (Button) findViewById(R.id.msg_switch);

		btnClearMsg = (Button) findViewById(R.id.clear_msg);
		clearContainer = (FrameLayout) findViewById(R.id.framlayout_clear_msg);
		exitContainer = (FrameLayout) findViewById(R.id.exit_container);
		btnExit = (Button) findViewById(R.id.exit_huodong);

		btnLeft.setOnClickListener(clickListener);
		btnAcceptMsg.setOnClickListener(clickListener);

		clearContainer.setOnClickListener(clickListener);
		btnClearMsg.setOnClickListener(clickListener);
		btnExit.setOnClickListener(clickListener);
		senderChat.setOnClickListener(clickListener);
	}

	private final OnClickListener clickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (v == btnLeft) {//返回
				if (from == FROM_CHAT) {
					Intent intent = new Intent();
					intent.putExtra(HuodongApplyerActivity.BACK_FROM_APPLYER_LIST, BACK_UPDATE);
					setResult(Activity.RESULT_OK, intent);
				}
				finish();
			}
			else if (v == btnAcceptMsg) {
				// TODO  消息提醒打开，关闭
				if (openNotice) {
					btnAcceptMsg.setBackgroundResource(R.drawable.apply_msg_off);
					openNotice = false;
				}
				else {
					btnAcceptMsg.setBackgroundResource(R.drawable.apply_msg_on);
					openNotice = true;
				}
				controller.CloseOrOpenHuodongMsgNotice(bean.getId(), openNotice);
			}
			else if (v == btnClearMsg || v == clearContainer) {
				// TODO 
				noticeDialog = new TwoButtonDialog(HuodongApplyerActivity.this);
				noticeDialog.setCancelable(true);
				noticeDialog.getMessageView().setText("是否删除和" + bean.getHuodongTitle() + "活动相关的所有的聊天记录？");
				noticeDialog.getLeftButton().setOnClickListener(new OnClickListener() {// 确定

							@Override
							public void onClick(View v) {
								//1.清除数据库对应聊天数据
								MultiChatMessage.delete(App.USER.getUid(), bean.getTeamId(), App.DB);
								// 2.发广播，通知消息列表进行数据更新
								Intent notify = MessageService.createResponseNotify(MessageService.TYPE_REFRESH_NEWS);
								sendBroadcast(notify);

								// 3.返回到聊天界面
								if (from == FROM_CHAT) {
									Intent intent = new Intent();
									intent.putExtra(HuodongApplyerActivity.BACK_FROM_APPLYER_LIST, BACK_UPDATE);
									setResult(Activity.RESULT_OK, intent);
									finish();
								}

								// 4.隐藏Dialog
								if (noticeDialog != null) {
									noticeDialog.dismiss();
								}
							}
						});
				noticeDialog.getRightButton().setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						if (noticeDialog != null) {
							noticeDialog.dismiss();
						}
					}
				});
				noticeDialog.show();

			}
			else if (v == btnExit) {
				// TODO 
				noticeDialog = new TwoButtonDialog(HuodongApplyerActivity.this);
				noticeDialog.setCancelable(true);
				noticeDialog.getMessageView().setText(R.string.exit_huodong_tip);
				noticeDialog.getLeftButton().setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO 清空消息记录 
						// 1.向服务器发送请求
						controller.ExitHuodong(bean.getId());
						// 2.清除数据库对应聊天数据
						MultiChatMessage.deleteAll(App.USER.getUid(), bean.getTeamId(), App.DB);
						// 3.发广播，通知消息列表更新数据
						Intent notify = MessageService.createResponseNotify(MessageService.TYPE_REFRESH_NEWS);
						sendBroadcast(notify);
						// 4.跳转界面
						Intent intent = new Intent();
						if (from == FROM_CHAT) {
							intent.putExtra(HuodongApplyerActivity.BACK_FROM_APPLYER_LIST, BACK_CLOSE);
							setResult(Activity.RESULT_OK, intent);
						}
						else {
							intent.putExtra(HuodongApplyerActivity.BACK_FROM_APPLYER_LIST, BACK_UPDATE);
							setResult(Activity.RESULT_OK, intent);
						}
						finish();

						if (noticeDialog != null) {
							noticeDialog.dismiss();
						}
					}
				});
				noticeDialog.getRightButton().setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						if (noticeDialog != null) {
							noticeDialog.dismiss();
						}
					}
				});
				noticeDialog.show();
			}
			else if (v == senderChat) {
				if (TextUtils.isEmpty(App.PREFS.getUserProfile())) {
					loginConfirm.openDialog();
					return;
				}
				// TODO 和群主聊天
				if (bean.getSenderUid() == App.USER.getUid()) {// 如果为本人
					return;
				}
				ChatMessage msg = new ChatMessage();
				msg.setTargetId(bean.getSenderUid());
				msg.setTargetNickname(bean.getSenderName());
				msg.setTargetPhotoUrl(bean.getSenderPic());
				msg.setActionType(ActionType.CHAT);
				msg.setType(Type.OTHER);
				msg.setSendType(SendType.READ);

				Intent intent = new Intent(HuodongApplyerActivity.this, ChatActivity.class);
				intent.putExtra(ChatActivity.REQUEST_MESSAGE, msg);
				startActivity(intent);
			}
		}
	};

	private void switchMsgNotice(boolean open) {
		if (open) {
			btnAcceptMsg.setBackgroundResource(R.drawable.apply_msg_on);
		}
		else {
			btnAcceptMsg.setBackgroundResource(R.drawable.apply_msg_off);
		}
	}

	private void update(ActivityBean bean) {
		if (bean == null)
			return;
		synchronized (adapter) {
			adapter.data.clear();
			adapter.data.addAll(bean.applyers);
			adapter.notifyDataSetChanged();
		}
		titlebar.getTitle().setText("报名列表(" + bean.applyers.size() + "人)");
		sendNickName.setText(bean.getSenderName());
		ImageLoader.getInstance().displayImage(bean.getSenderPic(), senderPhoto, App.OPTIONS_HEAD_ROUND);

//		//TODO 如果为游客，就不显示下面的信息
//		if (TextUtils.isEmpty(App.PREFS.getUserProfile()) || !bean.isApply()) {
//			msgContainer.setVisibility(View.GONE);
//			return;
//		}
//		else {
//			msgContainer.setVisibility(View.VISIBLE);
//			openNotice = bean.isOpenNotice();
//			switchMsgNotice(openNotice);
//		}
//
//		// TODO 如果为发起者，不能退出群
//		if (bean.getSenderUid() == App.USER.getUid()) {
//			exitContainer.setVisibility(View.GONE);
//		}
//		else {
//			exitContainer.setVisibility(View.VISIBLE);
//		}
	}

}
