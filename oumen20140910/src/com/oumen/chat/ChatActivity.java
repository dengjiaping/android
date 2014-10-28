package com.oumen.chat;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Message;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.oumen.R;
import com.oumen.TitleBar;
import com.oumen.activity.detail.HuodongHttpController;
import com.oumen.activity.detail.HuoDongDetailActivity;
import com.oumen.activity.detail.cell.CircleCornerImageHasDownloadView;
import com.oumen.activity.message.ActivityBean;
import com.oumen.android.App;
import com.oumen.android.App.NetworkType;
import com.oumen.android.BaseActivity;
import com.oumen.android.UserProfile;
import com.oumen.biaoqing.BiaoQing;
import com.oumen.biaoqing.EmojiView;
import com.oumen.message.ActionType;
import com.oumen.message.ActivityMessage;
import com.oumen.message.BaseMessage;
import com.oumen.message.ChatMessage;
import com.oumen.message.MessageService;
import com.oumen.message.MultiChatMessage;
import com.oumen.message.NotificationObserver;
import com.oumen.message.SendGroupMessage;
import com.oumen.message.SendMessage;
import com.oumen.message.SendType;
import com.oumen.message.Type;
import com.oumen.message.connection.MessageConnection;
import com.oumen.tools.ELog;
import com.oumen.widget.dialog.TwoButtonDialog;
import com.oumen.widget.image.shape.RoundRectangleImageView;
import com.oumen.widget.list.HSZHeaderView;
import com.oumen.widget.list.HSZListView;
import com.oumen.widget.list.HSZListViewAdapter;

/**
 * 新的聊天界面
 * 
 */
public class ChatActivity extends BaseActivity implements NotificationObserver {// OnTouchListener, OnGestureListener, 
	public static final String REQUEST_MESSAGE = "chat_activity_message";

	private final int RESULT_FROM_APPLYER_LIST = 1;

	private final int HANDLER_NOTIFY = 1;
	private final int HANDLER_MSG = 2;
	private final int HANDLER_NOTIFY_MSSSAGE = 3;
	private final int HANDLER_OLD_MESSAGE = 4;
	
	//标题行
	private TitleBar titlebar;
	private EditText edtInput;
	private Button btnSend;// 发送
	private ImageView imgBiaoqing;// 表情按钮

	private EmojiView biaoqingView;

	private HSZListView<BaseMessage> list;
	private HSZHeaderView viewHeader;

	// 报名成功以后的活动信息(listview头部信息)
	private MultiChatHeaderView headerView;

	private BaseMessage baseMessage;// 接收别的界面带过来的消息
	// 接收参数
	private ActionType actionType;// 类型（群聊，私聊）
	private int targetId;
	// 群聊的参数
	private int multiId = 0;
	private int activityId = 0;

	private ActivityMessage activityMsg;
	private ActivityBean activityBean = null;

	private String tempTime = null;

	private boolean hasAttender = false;

	private boolean isInitialized;

	private final AdapterImpl<BaseMessage> adapter = new AdapterImpl<BaseMessage>();

	private HuodongHttpController controller;

	private TwoButtonDialog tipDialog;

	private InputMethodManager inputManager;

	private final IntentFilter receiverFilter = new IntentFilter(MessageService.RESPONSE_ACTION);

	private final BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			int type = intent.getIntExtra(MessageService.INTENT_KEY_TYPE, 0);
			if (type == MessageService.TYPE_CHAT_MESSAGE) {// 单聊
				// TODO 如果过来的是单聊消息，进行如下判断再进行更新界面操作
				int tId = intent.getIntExtra(BaseMessage.KEY_TARGET_ID, App.INT_UNSET);

				if (ActionType.CHAT.equals(actionType) && targetId != App.INT_UNSET && targetId == tId) {

					boolean FromReceipt = false;
					if (intent.hasExtra(MessageService.INTENT_KEY_DATA)) {
						FromReceipt = intent.getBooleanExtra(MessageService.INTENT_KEY_DATA, false);
					}

					if (FromReceipt) {// 发送消息回执返回
						handler.sendEmptyMessage(HANDLER_NOTIFY);
					}
				}
			}
			else if (type == MessageService.TYPE_ACTIVITY_MULTI_CHAT_MESSAGE || type == MessageService.TYPE_ACTIVITY_MULTI_CHAT_STATUS_MESSAGE) {
				if (ActionType.CHAT.equals(actionType)) {
					return;
				}
				int mulId = intent.getIntExtra(MultiChatMessage.KEY_MULTI_ID, App.INT_UNSET);
				if (mulId != App.INT_UNSET && mulId == multiId) {

					boolean FromReceipt = false;
					if (intent.hasExtra(MessageService.INTENT_KEY_DATA)) {
						FromReceipt = intent.getBooleanExtra(MessageService.INTENT_KEY_DATA, false);
					}
					if (FromReceipt) {
						ELog.i("");
						handler.sendEmptyMessage(HANDLER_NOTIFY);
					}
				}
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chat_content1);
		controller = new HuodongHttpController(handler);

		inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

		if (savedInstanceState == null) {
			baseMessage = getIntent().getParcelableExtra(REQUEST_MESSAGE);
			actionType = baseMessage.getActionType();
			targetId = baseMessage.getTargetUid();
		}
		else {
			baseMessage = savedInstanceState.getParcelable(REQUEST_MESSAGE);
			actionType = baseMessage.getActionType();
			targetId = baseMessage.getTargetUid();
		}

		init();
		initData();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);//加上此句，是避免去取旧的intent
		baseMessage = intent.getParcelableExtra(REQUEST_MESSAGE);
		actionType = baseMessage.getActionType();
		targetId = baseMessage.getTargetUid();
		initData();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		ELog.i("");
		outState.putParcelable(REQUEST_MESSAGE, baseMessage);
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onStart() {
		super.onStart();
		registerReceiver(receiver, receiverFilter);

		if (!isInitialized) {
			isInitialized = true;
			return;
		}

		footerLoad(999, false);
	}

	@Override
	protected void onStop() {
		super.onStop();
		unregisterReceiver(receiver);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK && requestCode == RESULT_FROM_APPLYER_LIST) {
			int flag = data.getIntExtra(HuodongApplyerActivity.BACK_FROM_APPLYER_LIST, App.INT_UNSET);
			if (flag != App.INT_UNSET && flag == HuodongApplyerActivity.BACK_UPDATE) {
				controller.getHuodongDetail(activityId);
			}
			else {
				finish();
			}

		}
	}

	private void init() {
		titlebar = (TitleBar) findViewById(R.id.titlebar);
		titlebar.getLeftButton().setOnClickListener(clickListener);
		titlebar.getRightButton().setBackgroundResource(R.drawable.chat_applyer_btnselector);
		titlebar.getRightButton().setVisibility(View.GONE);
		titlebar.getRightButton().setOnClickListener(clickListener);

		btnSend = (Button) findViewById(R.id.send);

		// 表情图标
		imgBiaoqing = (ImageView) findViewById(R.id.biaoqing);
		edtInput = (EditText) findViewById(R.id.input);

		// 群聊的头部
		headerView = new MultiChatHeaderView(ChatActivity.this);

		headerView.getHuodongHeaderView().setImageHasClickListener(false, clickListener);

		viewHeader = new HSZHeaderView(this);

		list = (HSZListView<BaseMessage>) findViewById(R.id.list);
		list.setHeaderView(viewHeader, getResources().getDimensionPixelSize(R.dimen.list_header_height_default));
		list.setDivider(new ColorDrawable(getResources().getColor(android.R.color.transparent)));
		list.setDividerHeight(5);
		list.addHeaderView(headerView);
		list.setAdapter(adapter);

		biaoqingView = (EmojiView) findViewById(R.id.biaoqinglist);
		biaoqingView.setClickListener(clickListener);
		biaoqingView.addAllBiaoqing();// TODO 初始化表情界面
		biaoqingView.setVisibility(View.GONE);

		imgBiaoqing.setOnClickListener(clickListener);
		btnSend.setOnClickListener(clickListener);
	}

	private void initData() {
		if (baseMessage instanceof MultiChatMessage) {// 群聊
			headerView.setViewVisible(View.VISIBLE);
			MultiChatMessage multi = (MultiChatMessage) baseMessage;
			activityMsg = multi.getActivityMessage();
			multiId = multi.getMultiId();
			activityId = multi.getActivityId();

			controller.getHuodongDetail(multi.getActivityId());
			adapter.setMessage(activityMsg);

			titlebar.getTitle().setText(activityMsg.getTitle());
			titlebar.getRightButton().setVisibility(View.VISIBLE);
		}
		else {
			headerView.setViewVisible(View.GONE);
			titlebar.getRightButton().setVisibility(View.GONE);
			if (targetId == ChatMessage.OUMEN_TEAM_ID) {
				titlebar.getTitle().setText("偶们团队");
			}
			else {
				titlebar.getTitle().setText(baseMessage.getTargetNickname());
			}
		}

		footerLoad(App.DEFAULT_LIMIT, true);

		sendBroadcast(MessageService.createRequestNotify(MessageService.TYPE_REFRESH_NEWS));
	}

	/**
	 * 底部更新
	 * 
	 * @param limit
	 * @param isFirst
	 */
	private void footerLoad(final int limit, final boolean isFirst) {
		App.THREAD.execute(new Runnable() {

			@Override
			public void run() {
				synchronized (adapter) {
					long min = adapter.isEmpty() ? App.INT_UNSET : ((BaseMessage) adapter.get(adapter.getCount() - 1)).getDatetime().getTime();

					if (ActionType.CHAT.equals(actionType)) { // 私聊
						if (isFirst) {// TODO 此处增加是为了从参与界面返回刷新数据
							min = App.INT_UNSET;
							adapter.clear();
						}
						// 将正在发送的标签改成发送失败
						ChatMessage.updateSendTypeMsg(App.PREFS.getUid(), targetId, SendType.SENDING, SendType.SENDFAIL, App.DB);

						List<ChatMessage> data = ChatMessage.querySingleGroup(App.PREFS.getUid(), targetId, min, App.INT_UNSET, limit, App.DB);
						Collections.reverse(data);

						if (!data.isEmpty()) {
							// 把最大的时间赋值给minItemTime，便于取出最新的消息
							ChatMessage.update2Read(App.PREFS.getUid(), targetId, App.DB);
							synchronized (adapter) {
								adapter.addAll(data);
//								adapter.notifyDataSetChanged();
								// 通知界面更新
								handler.sendEmptyMessage(HANDLER_NOTIFY_MSSSAGE);
							}
						}
					}
					else {// 群聊
						if (isFirst) {// TODO 此处增加是为了从参与界面返回刷新数据
							min = App.INT_UNSET;
							adapter.clear();
						}
						MultiChatMessage.updateSendTypeMsg(App.USER.getUid(), multiId, SendType.SENDING, SendType.SENDFAIL, App.DB);
						List<MultiChatMessage> data = MultiChatMessage.querySingleGroup(App.USER.getUid(), multiId, min, App.INT_UNSET, limit, App.DB);
						Collections.reverse(data);

						if (!data.isEmpty()) {
							// 把最大的时间赋值给minItemTime，便于取出最新的消息
							tempTime = App.YYYY_MM_DD_HH_MM_FORMAT.format(new Date(data.get(0).getDatetime().getTime()));
							MultiChatMessage.updateAllRead(App.PREFS.getUid(), multiId, App.DB);
							synchronized (adapter) {
								adapter.addAll(data);
//								adapter.notifyDataSetChanged();
							}
						}
						else {
							tempTime = App.YYYY_MM_DD_HH_MM_FORMAT.format(new Date(App.getServerTime()));
						}
						// 通知界面更新
						handler.sendEmptyMessage(HANDLER_NOTIFY_MSSSAGE);
					}
				}
			}
		});

	}

	private void headerLoad() {
		viewHeader.setState(HSZListViewAdapter.STATE_LOADING);
		App.THREAD.execute(new Runnable() {

			@Override
			public void run() {

				synchronized (adapter) {
					long max = adapter.isEmpty() ? App.INT_UNSET : ((BaseMessage) adapter.get(0)).getDatetime().getTime();

					if (ActionType.CHAT.equals(actionType)) {// 私聊
						// 将正在发送的标签改成发送失败
						ChatMessage.updateSendTypeMsg(App.PREFS.getUid(), targetId, SendType.SENDING, SendType.SENDFAIL, App.DB);

						List<ChatMessage> data = ChatMessage.querySingleGroup(App.PREFS.getUid(), targetId, App.INT_UNSET, max, App.DEFAULT_LIMIT, App.DB);
						Collections.reverse(data);

						if (!data.isEmpty()) {
							// 把最大的时间赋值给minItemTime，便于取出最新的消息
							ChatMessage.update2Read(App.PREFS.getUid(), targetId, App.DB);
							adapter.addAll(0, data);
						}
						handler.sendEmptyMessage(HANDLER_OLD_MESSAGE);
					}
					else {// 群聊
						List<MultiChatMessage> data = MultiChatMessage.querySingleGroup(App.PREFS.getUid(), multiId, App.INT_UNSET, max, App.DEFAULT_LIMIT, App.DB);
						Collections.reverse(data);
						if (!data.isEmpty()) {
							// 把最大的时间赋值给minItemTime，便于取出最新的消息
							MultiChatMessage.updateAllRead(App.PREFS.getUid(), multiId, App.DB);
							synchronized (adapter) {
								adapter.addAll(0, data);
							}
						}
						handler.sendEmptyMessage(HANDLER_OLD_MESSAGE);
					}
				}
			}
		});
	}

	private final View.OnClickListener clickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {

			if (v == imgBiaoqing) {
				if (biaoqingView.getVisibility() == View.VISIBLE) {
					biaoqingView.setVisibility(View.GONE);
					imgBiaoqing.setImageResource(R.drawable.expression);
					
					getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

					inputManager.showSoftInput(edtInput, InputMethodManager.HIDE_NOT_ALWAYS);
				}
				else {
					imgBiaoqing.setImageResource(R.drawable.keyboard);
					biaoqingView.setVisibility(View.VISIBLE);
					
					getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
					
					list.setSelection(adapter.getCount() - 1);

					if (inputManager.isActive()) {
						inputManager.hideSoftInputFromWindow(edtInput.getWindowToken(), 0);
					}
				}
			}
			else if (v == titlebar.getLeftButton()) {// 返回
				setResult(Activity.RESULT_OK);
				finish();
				return;
			}
			else if (v == titlebar.getRightButton()) {// 群聊的用户列表
				if (hasAttender) {
					Intent i = new Intent(ChatActivity.this, HuodongApplyerActivity.class);
					i.putExtra(HuodongApplyerActivity.HUODONG_MSG, activityBean);
					i.putExtra(HuodongApplyerActivity.FROM_ACTIVITY_TAG, HuodongApplyerActivity.FROM_CHAT);
					startActivityForResult(i, RESULT_FROM_APPLYER_LIST);
				}
				else {
					Toast.makeText(mBaseApplication, "还在获取参与用户列表，请稍后...", Toast.LENGTH_SHORT).show();
				}
			}
			else if (v == btnSend) {
				// 发送文字消息
				if (NetworkType.NONE.equals(App.getNetworkType())) {
					Toast.makeText(mBaseApplication, R.string.err_network_invalid, Toast.LENGTH_SHORT).show();
					return;
				}
				String content = edtInput.getText().toString().trim();
				if (TextUtils.isEmpty(content)) {
					Toast.makeText(mBaseApplication, "请输入内容", Toast.LENGTH_SHORT).show();
					return;
				}
				// 发送聊天消息
				sendMessage(Type.TEXT, content);
				edtInput.setText(null);
				return;
			}
			else if (v instanceof RoundRectangleImageView || v instanceof CircleCornerImageHasDownloadView) {
				// TODO 点击活动头部跳转到活动详情界面
				Intent intent = new Intent(mBaseApplication, HuoDongDetailActivity.class);
				intent.putExtra(HuoDongDetailActivity.INTENT_KEY_ACTIVITY_ID, activityId);
				startActivity(intent);
			}
			else if (v.getId() == R.id.right_send_fail) {
				BaseMessage provider = (BaseMessage) v.getTag();
				ELog.i("");

				tipDialog = new TwoButtonDialog(ChatActivity.this);
				tipDialog.getMessageView().setText(R.string.chat_resend_tip);
				tipDialog.getRightButton().setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						if (tipDialog != null) {
							tipDialog.dismiss();
							tipDialog = null;
						}
					}
				});
				tipDialog.getLeftButton().setTag(provider);
				tipDialog.getLeftButton().setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						if (tipDialog != null) {
							tipDialog.dismiss();
							tipDialog = null;
						}
						BaseMessage provider = (BaseMessage) v.getTag();
						// 从新发送
						if (!MessageConnection.instance.isConnected()) {
							Toast.makeText(ChatActivity.this, R.string.err_socket, Toast.LENGTH_SHORT).show();
							return;
						}
						if (ActionType.CHAT.equals(provider.getActionType())) { // 私聊
							ELog.i("单聊失败重发");
							String content = provider.getContent();
							if (Type.CIWEI.equals(provider.getType()) || Type.OUBA.equals(provider.getType())) {
								content = content.substring(0, content.lastIndexOf(App.FILE_SUFFIX));
							}
							ELog.i(content);

							SendMessage mBean = new SendMessage(App.PREFS.getUid(), provider.getTargetNickname(), provider.getTargetPhotoSourceUrl(), provider.getTargetUid(), provider.getDatetime(), ActionType.CHAT, provider.getType(), content);
							MessageConnection.instance.send(mBean.toMessage());
							MessageService.CACHE_MESSAGE.put(provider.getDatetime().getTime(), (ChatMessage) provider);
							ChatMessage.updateSendTypeMsg(App.USER.getUid(), provider.getTargetUid(), SendType.SENDFAIL, SendType.SENDING, App.DB);
							provider.setSendType(SendType.SENDING);
						}
						else {// 群聊
							ELog.i("群聊失败重发");
							MultiChatMessage msg = (MultiChatMessage) provider;

							String content = msg.getContent();
							if (Type.CIWEI.equals(msg.getType()) || Type.OUBA.equals(msg.getType())) {
								content = content.substring(0, content.length() - content.lastIndexOf(App.FILE_SUFFIX));
							}
							ELog.i(content);

							SendGroupMessage mBean = new SendGroupMessage(App.PREFS.getUid(), App.USER.getNickname(), App.USER.getPhotoSourceUrl(), msg.getActivityId(), msg.getMultiId(), activityMsg, msg.getTimestamp(), ActionType.ACTIVITY_MULTI_CHAT, msg.getType(), content);
							MessageConnection.instance.send(mBean.toMessage());

							MultiChatMessage.updateSendTypeMsg(App.USER.getUid(), msg.getMultiId(), msg.getSendType(), SendType.SENDING, msg.getDatetime(), App.DB);

							MessageService.CACHE_MESSAGE.put(provider.getDatetime().getTime(), msg);
							provider.setSendType(SendType.SENDING);
						}

						adapter.notifyDataSetChanged();
					}
				});
				tipDialog.show();

			}
			else {
				BiaoQing biaoqingMsg = (BiaoQing) v.getTag();
				if (biaoqingMsg == null) {
					return;
				}

				if (Type.TEXT.equals(biaoqingMsg.getType())) {//输入文字加入小表情
					String content = edtInput.getText().toString().trim();
					SpannableStringBuilder builder = new SpannableStringBuilder(content);
					builder.append(biaoqingMsg.getSendMsg());
					builder = App.SMALLBIAOQING.convert(ChatActivity.this, builder, App.INT_UNSET);
					edtInput.setText(builder);
					//TODO 设置光标位置
					edtInput.setSelection(builder.length());
				}
				else if (Type.OUBA.equals(biaoqingMsg.getType())) {//欧巴表情
					sendMessage(Type.OUBA, biaoqingMsg.getSendMsg());
				}
				else if (Type.CIWEI.equals(biaoqingMsg.getType())) {
					sendMessage(Type.CIWEI, biaoqingMsg.getSendMsg());
				}
				else if (Type.OTHER.equals(biaoqingMsg.getType())) {// 删除
					//动作按下
				    int action = KeyEvent.ACTION_DOWN;
				    //code:删除，其他code也可以，例如 code = 0
				    int code = KeyEvent.KEYCODE_DEL;
				    KeyEvent event = new KeyEvent(action, code);
				    edtInput.onKeyDown(KeyEvent.KEYCODE_DEL, event); 
				}

			}
		}
	};

	/**
	 * 发送聊天信息
	 */
	private void sendMessage(Type type, String content) {
		if (!MessageConnection.instance.isConnected()) {
			Toast.makeText(this, R.string.err_socket, Toast.LENGTH_SHORT).show();
			return;
		}

		try {
			if (content.length() > 0) {
				long time = App.getServerTime();
				UserProfile profile = App.USER;
				// ==============保存到当前聊天记录里===================
				if (actionType.equals(ActionType.CHAT)) {// 私聊

					ChatMessage bean = new ChatMessage();
					bean.setToId(App.PREFS.getUid());
					bean.setSelfName(profile.getNickname());
					bean.setSelfPhotoUrl(profile.getPhotoSourceUrl());
					bean.setTargetId(targetId);
					bean.setTargetNickname(baseMessage.getTargetNickname());
					bean.setTargetPhotoUrl(baseMessage.getTargetPhotoSourceUrl());
					if (Type.CIWEI.equals(type) || Type.OUBA.equals(type)) {
						bean.setContent(content + App.FILE_SUFFIX);
					}
					else {
						bean.setContent(content);
					}
					bean.setActionType(ActionType.CHAT);
					bean.setType(type);
					bean.setDatetime(new Date(time));
//					bean.setRead(true);
					bean.setSendType(SendType.SENDING);
					bean.setSend(true);
					MessageService.CACHE_MESSAGE.put(time, bean);
					ChatMessage.insert(bean, App.DB);

					synchronized (adapter) {
						adapter.add(bean);
						adapter.notifyDataSetChanged();
					}

					SendMessage mBean = new SendMessage(App.PREFS.getUid(), profile.getNickname(), profile.getPhotoSourceUrl(), targetId, new Date(time), ActionType.CHAT, type, content);
					// TODO socket连着，直接发
					MessageConnection.instance.send(mBean.toMessage());
				}
				else {// 群聊

					MultiChatMessage bean = new MultiChatMessage();
					bean.setToId(App.PREFS.getUid());
					bean.setSelfName(profile.getNickname());
					bean.setSelfPhotoUrl(profile.getPhotoSourceUrl());
					bean.setActivityId(activityId);
					bean.setMultiId(multiId);

					// 插入数据库
					if (Type.CIWEI.equals(type) || Type.OUBA.equals(type)) {
						bean.setContent(content + App.FILE_SUFFIX);
					}
					else {
						bean.setContent(content);
					}
					bean.setActionType(ActionType.ACTIVITY_MULTI_CHAT);
					bean.setType(type);
					bean.setDatetime(new Date(time));
					bean.setSendType(SendType.SENDING);
					bean.setSend(true);
					MessageService.CACHE_MESSAGE.put(time, bean);
					// 插入数据库
					MultiChatMessage.insert(App.PREFS.getUid(), bean, App.DB);

					synchronized (adapter) {
						adapter.add(bean);
						adapter.notifyDataSetChanged();
					}

					SendGroupMessage mBean = new SendGroupMessage(App.PREFS.getUid(), profile.getNickname(), profile.getPhotoSourceUrl(), activityId, multiId, activityMsg, new Date(time), ActionType.ACTIVITY_MULTI_CHAT, type, content);
					ELog.i(mBean.toMessage());
					// TODO 
					MessageConnection.instance.send(mBean.toMessage());
				}

				// 从新计算高度
				list.setSelection(adapter.getCount() - 1);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onBackPressed() {
		// 关闭表情
		if (biaoqingView.getVisibility() == View.VISIBLE) {
			biaoqingView.setVisibility(View.GONE);
			imgBiaoqing.setImageResource(R.drawable.expression);
			getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
		}
		else {
			super.onBackPressed();
		}
	}

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
			case HANDLER_NOTIFY:
				list.setSelection(adapter.getCount() - 1);
				break;
			case HANDLER_MSG:
				BaseMessage chatMsg = (BaseMessage) msg.obj;

				synchronized (adapter) {
					adapter.add(chatMsg);
					adapter.notifyDataSetChanged();
					handler.sendEmptyMessage(HANDLER_NOTIFY);
				}
				break;
			case HuodongHttpController.HANDLER_GET_HUODONG_DETAIL://获取活动详情返回
				if (msg.obj != null) {
					if (msg.obj instanceof ActivityBean) {
						hasAttender = true;
						activityBean = (ActivityBean) msg.obj;
						headerView.update(activityBean);
					}
					else if (msg.obj instanceof String) {
//						Toast.makeText(mBaseApplication, (String) msg.obj, Toast.LENGTH_SHORT).show();
					}
				}
				break;

			case HANDLER_NOTIFY_MSSSAGE:
				synchronized (adapter) {
					adapter.notifyDataSetChanged();
				}
				list.setSelection(adapter.getCount() - 1);
				if (!actionType.equals(ActionType.CHAT)) {
					headerView.updateTime(tempTime);
					if (activityBean == null) {
						headerView.update(activityMsg);
					}
				}
				break;
			case HANDLER_OLD_MESSAGE:// 下拉获取以前消息
				synchronized (adapter) {
					adapter.notifyDataSetChanged();
				}
				list.setSelection(adapter.getCount() - 1);
				if (!actionType.equals(ActionType.CHAT)) {
					headerView.updateTime(tempTime);
					if (activityBean == null) {
						headerView.update(activityMsg);
					}
				}
				viewHeader.setState(HSZListViewAdapter.STATE_NORMAL);
				list.loaded();
				break;
		}
		return false;
	}

	//----------------------- NotificationObserver -----------------------//

	@Override
	public int getTargetId() {
		return multiId > 0 ? multiId : targetId;
	}

	@Override
	public Page getCurrentPage() {
		return Page.CHAT;
	}

	@Override
	public void receiveNotification(Object... params) {
		if (params[0] instanceof ChatMessage) {
			ChatMessage msg = (ChatMessage) params[0];
			msg.setSendType(SendType.READ);
			ChatMessage.update2Read(msg.getToId(), msg.getTargetUid(), App.DB);

			ELog.i("Read:" + msg.getToId() + "/" + msg.getTargetUid());
			handler.sendMessage(handler.obtainMessage(HANDLER_MSG, msg));
		}
		else if (params[0] instanceof MultiChatMessage) {
			MultiChatMessage msg = (MultiChatMessage) params[0];
			msg.setSendType(SendType.READ);
			MultiChatMessage.updateAllRead(App.PREFS.getUid(), msg.getMultiId(), App.DB);

			ELog.i("Read:" + msg.getToId() + "/" + msg.getTargetUid());
			handler.sendMessage(handler.obtainMessage(HANDLER_MSG, msg));
		}
		else if (params[0] instanceof Integer) {
			adapter.notifyDataSetChanged();
			handler.sendEmptyMessage(HANDLER_NOTIFY);
		}
	}

	private class AdapterImpl<E extends BaseMessage> extends HSZListViewAdapter<E> {

		private ActivityMessage msg = null;

		@Override
		public void onHeaderLoad() {
			ELog.i("");
			headerLoad();
		}

		public void setMessage(ActivityMessage msg) {
			this.msg = msg;
		}

		@Override
		public void onHeaderPull(View headerView, int top) {
			viewHeader.setState(HSZListViewAdapter.STATE_NORMAL);
			viewHeader.getTip().setText(R.string.list_pull);
		}

		@Override
		public void onHeaderPullOverLine(View headerView, int top) {
			viewHeader.setState(HSZListViewAdapter.STATE_PULL_OVER);
			viewHeader.getTip().setText(R.string.list_release);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			BaseMessage provider = get(position);

			ChatItem item = convertView == null ? new ChatItem(parent.getContext()) : (ChatItem) convertView;
			item.setMessage(msg);
			item.update(provider, position == 0 ? 0 : get(position - 1).getDatetime().getTime());
			item.setOnReSendListener(clickListener);

			return item;
		}
	}
}
