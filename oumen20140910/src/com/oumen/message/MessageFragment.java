package com.oumen.message;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.oumen.R;
import com.oumen.TitleBar;
import com.oumen.activity.PushActivityListActivity;
import com.oumen.android.App;
import com.oumen.android.BaseFragment;
import com.oumen.book.BookActivity;
import com.oumen.book.BookMessage;
import com.oumen.chat.ChatActivity;
import com.oumen.friend.FriendsActivity;
import com.oumen.home.HomeFragment;
import com.oumen.home.LoginConfrim;
import com.oumen.near.NearActivity;
import com.oumen.peer.OumenCircleDetailActivity;
import com.oumen.tools.ELog;
import com.oumen.widget.dialog.TwoButtonDialog;
/**
 * 消息中心
 *
 */
public class MessageFragment extends BaseFragment {
	private final int HANDLER_UPDATE_LIST = 1;
	
	private final MessageListAdapter adapter = new MessageListAdapter();
	
	private final NearMessage near = new NearMessage();
	
	//标题行控件
	private TitleBar titlebar;
	private Button btnLeft;
	private Button btnRight;
	
	private ListView lstView;
	
	private HomeFragment host;
	
	private LoginConfrim loginConfrim;
	
	private final IntentFilter receiverFilter = new IntentFilter(MessageService.RESPONSE_ACTION);

	private final BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			int type = intent.getIntExtra(MessageService.INTENT_KEY_TYPE, 0);
			switch (type) {
				case MessageService.TYPE_ACTIVITY_MESSAGE:
				case MessageService.TYPE_CHAT_MESSAGE:
				case MessageService.TYPE_ACTIVITY_MULTI_CHAT_STATUS_MESSAGE:
				case MessageService.TYPE_ACTIVITY_MULTI_CHAT_MESSAGE:
				case MessageService.TYPE_FRIEND_MESSAGE:
				case MessageService.TYPE_HELP_MESSAGE:
				case MessageService.TYPE_BOOK_MESSAGE:
				case MessageService.TYPE_BASE_MESSAGE:
				case MessageService.TYPE_REFRESH_NEWS:
					loadData();
					break;
				case MessageService.TYPE_CIRCLE_MESSAGE://偶们圈消息更新
					circleMsgTip();
					break;
				case MessageService.TYPE_USERINFO:
					circleMsgTip();
					break;
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		adapter.clickListener = clickListener;
		adapter.longClickListener = longClickListener;
		near.listener = nearListener;
		
		host = (HomeFragment) getParentFragment();
		loginConfrim = new LoginConfrim(host.getActivity());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		getActivity().registerReceiver(receiver, receiverFilter);
		
		View view = inflater.inflate(R.layout.fragment_message, container, false);
		titlebar = (TitleBar) view.findViewById(R.id.titlebar);
		
		titlebar.getTitle().setText(getResources().getString(R.string.tab_messages));
		btnLeft = titlebar.getLeftButton();
		btnRight = titlebar.getRightButton();
		circleMsgTip();
		
		btnLeft.setBackgroundResource(R.drawable.nav_btnselector);
		btnRight.setText(getResources().getString(R.string.friend));
		btnRight.setVisibility(View.VISIBLE);
		
		Resources res = view.getResources();
		lstView = (ListView) view.findViewById(R.id.listview);
		lstView.setSelector(android.R.color.transparent);
		lstView.setCacheColorHint(res.getColor(android.R.color.transparent));
		lstView.setScrollbarFadingEnabled(true);
		lstView.setDivider(new ColorDrawable(res.getColor(R.color.divider)));
		lstView.setDividerHeight(1);
		lstView.setAdapter(adapter);
		
		btnLeft.setOnClickListener(clickListener);
		btnRight.setOnClickListener(clickListener);
		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		loadData();
		near.obtainNearData();
	}
	
	public void circleMsgTip() {
		if (host.TitleBarMsgTipVisible()) {
			titlebar.setTipViewVisible(View.VISIBLE);
		}
		else {
			titlebar.setTipViewVisible(View.GONE);
		}
	}

	@Override
	public void onDestroyView() {
		getActivity().unregisterReceiver(receiver);
		hideDeleteDialog();
		super.onDestroyView();
	}

	private void loadData() {
		App.THREAD.execute(new Runnable() {
			
			@Override
			public void run() {
				int selfUid = App.PREFS.getUid();
				//=============================单聊消息===========================================
				ChatMessage oumenTeamChat = null;
				// 查找数据库中本人所有的聊天消息
				List<ChatMessage> tmpChats = ChatMessage.queryGroups(selfUid, App.DB);
				// 先判断是否有偶们团队的消息，进行标记
				for (ChatMessage i : tmpChats) {
					if (i.targetId == ChatMessage.OUMEN_TEAM_ID) {
						oumenTeamChat = i;
						break;
					}
				}
				/*
				 * 如果没有偶们团队的消息，就创建一个新的；如果有的话，就删除，因为之前都创建了
				 */
				if (oumenTeamChat == null) {
					oumenTeamChat = new ChatMessage();
					oumenTeamChat.actionType = ActionType.CHAT;
					oumenTeamChat.type = Type.TEXT;
					oumenTeamChat.targetId = ChatMessage.OUMEN_TEAM_ID;
					oumenTeamChat.targetNickname = getString(R.string.oumen_team);
					oumenTeamChat.datetime = new Date();
				}
				else {
					tmpChats.remove(oumenTeamChat);
				}
				ELog.i("Chats:" + tmpChats.size());
				
				oumenTeamChat.newCount = ChatMessage.queryNewCount(selfUid, ChatMessage.OUMEN_TEAM_ID, App.DB);
				for (ChatMessage i : tmpChats) {
					i.newCount = ChatMessage.queryNewCount(selfUid, i.targetId, App.DB);
				}
				//=============================群聊消息===========================================
				List<MultiChatMessage> tmpMultiChats = MultiChatMessage.queryGroups(selfUid, App.DB);
				LinkedList<MultiChatMessage> noActivityInfos = new LinkedList<MultiChatMessage>();
				for (MultiChatMessage i : tmpMultiChats) {
					i.activityMessage = ActivityMessage.query(selfUid, i.activityId, App.DB);
					if (i.activityMessage == null)
						noActivityInfos.add(i);
					else
						i.newCount = MultiChatMessage.queryNewCount(selfUid, i.multiId, App.DB);
				}
				tmpMultiChats.removeAll(noActivityInfos);
				ELog.i("MultiChats:" + tmpMultiChats.size());
				//=============================添加好友消息===========================================
				List<FriendMessage> tmpFriends = FriendMessage.query(selfUid, App.DB);
				ELog.i("Friends:" + tmpFriends.size());
				//=============================求助消息===========================================
				List<HelpMessage> tmpHelps = HelpMessage.query(selfUid, App.DB);
				ELog.i("Helps:" + tmpHelps.size());
				//=============================活动推送消息===========================================
				TopPushActivityMessage topPushActivity = null;
				ActivityMessage latestActivity = ActivityMessage.queryLatest(selfUid, App.DB);
				if (latestActivity == null) {
					topPushActivity = new TopPushActivityMessage();
					topPushActivity.title = getResources().getString(R.string.msg_recommend_title_default);
					topPushActivity.description = getResources().getString(R.string.msg_recommend_description_default);
				}
				else {
					topPushActivity = new TopPushActivityMessage(latestActivity);
					topPushActivity.description = topPushActivity.title;
					topPushActivity.title = getResources().getString(R.string.msg_recommend_title_default);
					topPushActivity.newCount = ActivityMessage.queryNewCount(selfUid, App.DB);
				}
				//=============================育儿宝典消息===========================================
				BookMessage book = BookMessage.queryLatest(selfUid, App.DB);
				
				LinkedList<MessageListItemDataProvider> topMessages = new LinkedList<MessageListItemDataProvider>();
				if (latestActivity != null) {
					topMessages.add(topPushActivity);
				}
				topMessages.add(oumenTeamChat);
				if (book != null) {
					book.setNewCount(BookMessage.queryNewCount(selfUid, App.DB));
					topMessages.add(book);
				}
				
				List<MessageListItemDataProvider> data = adapter.data;
				synchronized (data) {
					data.clear();
					data.addAll(tmpChats);
					data.addAll(tmpFriends);
					data.addAll(tmpMultiChats);
					data.addAll(tmpHelps);
					Collections.sort(data, sortComparator);
					data.addAll(0, topMessages);
//					data.add(0, near);
				}
				handler.sendEmptyMessage(HANDLER_UPDATE_LIST);
				
				//如果本地没有相关活动信息，就向服务器请求
				if (!noActivityInfos.isEmpty()) {
					int[] activityIds = new int[noActivityInfos.size()];
					int i = 0;
					for (MultiChatMessage msg : noActivityInfos) {
						activityIds[i++] = msg.activityId;
					}
					Intent req = MessageService.createRequestNotify(MessageService.TYPE_ACTIVITY_MESSAGE);
					req.putExtra(MessageService.INTENT_KEY_DATA, activityIds);
					getActivity().sendBroadcast(req);
				}
			}
		});
	}
	
	private final Comparator<MessageListItemDataProvider> sortComparator = new Comparator<MessageListItemDataProvider>() {
		
		@Override
		public int compare(MessageListItemDataProvider lhs, MessageListItemDataProvider rhs) {
			if (lhs.getTimestamp() == null || rhs.getTimestamp() == null) {
				return 0;
			}
			return rhs.getTimestamp().compareTo(lhs.getTimestamp());
		}
	};
	
	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
			case HANDLER_UPDATE_LIST:
				adapter.notifyDataSetChanged();
				break;
		}
		return super.handleMessage(msg);
	}

	protected final View.OnClickListener clickListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			int id = v.getId();
			if(id == R.id.left){//左侧按钮
				//TODO 打开menu
				host.menuToggle();
			}
			else if (id == R.id.right) {//右侧按钮
				if (TextUtils.isEmpty(App.PREFS.getUserProfile())) {
					//TODO 跳转到登录界面
					loginConfrim.openDialog();
					return;
				}
				//通讯录
				Intent intent = new Intent(getActivity(), FriendsActivity.class);
				startActivityForResult(intent, HomeFragment.REQUEST_CODE_MESSAGE_CENTER);
			}
			else if (id == R.id.btn_left) {
				//对话框确定按钮
				App.THREAD.execute(deleteTask);
				synchronized (adapter) {
					adapter.data.remove(dialogDelete.getTag());
					adapter.notifyDataSetChanged();
				}
				hideDeleteDialog();
			}
			else if (id == R.id.btn_right) {
				//对话框取消按钮
				hideDeleteDialog();
			}
			// TODO　此处先留着（点击头像，进入个人中心）
//			else if (id == R.id.icon) {
//				//头像
//				MessageListItemDataProvider provider = MessageListItem.getDataProviderByChild(v);
//				if (provider instanceof ChatMessage) {
//					ChatMessage msg = (ChatMessage) provider;
//					
//					if (msg.targetId == ChatMessage.OUMEN_TEAM_ID) {
//						//点击偶们团队头像
//						openChatActivity(msg);
//					}
//					else {
//						Intent intent = new Intent(getActivity(), UserInfoActivity.class);
//						intent.putExtra(UserInfoActivity.INTENT_KEY_UID, msg.targetId);
//						startActivityForResult(intent, HomeFragment.REQUEST_MESSAGE_CENTER);
//					}
//				}
//				else if (provider instanceof HelpMessage) {
//					HelpMessage msg = (HelpMessage) provider;
//					openHelpActivity(msg);
//				}
//				else if (provider instanceof TopPushActivityMessage) {
//					TopPushActivityMessage msg = (TopPushActivityMessage) provider;
//					openActivityRecommendActivity(msg);
//				}
//				else if (provider instanceof FriendMessage) {
//					FriendMessage msg = (FriendMessage) provider;
//
//					Intent intent = new Intent(getActivity(), UserInfoActivity.class);
//					intent.putExtra(UserInfoActivity.INTENT_KEY_UID, msg.targetId);
//					startActivity(intent);
//					
//					FriendMessage.update2read(msg.toId, msg.targetId, App.DB);
//				}
//				else if (provider instanceof BookMessage) {
//					BookMessage msg = (BookMessage) provider;
//					openBookActivity(msg);
//				}
//				else if (provider instanceof NearMessage){//群聊
//					NearMessage msg = (NearMessage) provider;
//					openNearActivity(msg);
//				}
//			}
			else if (id == R.id.button) {
				//确认添加好友
				MessageListItemDataProvider provider = MessageListItem.getDataProviderByChild(v);
				FriendMessage msg;
				if (provider instanceof FriendMessage) {
					msg = (FriendMessage) provider;
				}
				else {
					return;
				}
				
				//向服务器发送添加好友请求
				Intent req = MessageService.createRequestNotify(MessageService.TYPE_FRIEND_MESSAGE);
				req.putExtra(MessageService.INTENT_KEY_DATA, msg.getTargetUid());
				getActivity().sendBroadcast(req);
				
				FriendMessage.update2read(msg.toId, msg.targetId, App.DB);
			}
			else if (v instanceof MessageListItem) {// TODO　list的item 单条列表项
				MessageListItem item = (MessageListItem) v;
				MessageListItemDataProvider provider = item.provider;
				if (provider instanceof ChatMessage) {//私聊
					ChatMessage msg = (ChatMessage) provider;
					openChatActivity(msg);
				}
				else if (provider instanceof MultiChatMessage){//群聊
					MultiChatMessage msg = (MultiChatMessage) provider;
					openChatActivity(msg);
				}
				else if (provider instanceof HelpMessage) {
					HelpMessage msg = (HelpMessage) provider;
					openHelpActivity(msg);
				}
				else if (provider instanceof TopPushActivityMessage) {
					TopPushActivityMessage msg = (TopPushActivityMessage) provider;
					openActivityRecommendActivity(msg);
				}
				else if (provider instanceof FriendMessage) {
					FriendMessage msg = (FriendMessage) provider;
					openChatActivity(msg);
					
					FriendMessage.update2read(msg.toId, msg.targetId, App.DB);
				}
				else if (provider instanceof BookMessage) {
					BookMessage msg = (BookMessage) provider;
					openBookActivity(msg);
				}
				else if (provider instanceof NearMessage){//群聊
					NearMessage msg = (NearMessage) provider;
					openNearActivity(msg);
				}
			}
		}
	};
	
	protected final View.OnLongClickListener longClickListener = new View.OnLongClickListener() {
		
		@Override
		public boolean onLongClick(View v) {
			if (v instanceof MessageListItem) {
				MessageListItem item = (MessageListItem) v;
				MessageListItemDataProvider provider = item.provider;
				if (provider instanceof TopPushActivityMessage)
					return false;//活动推荐不能删
				else if (provider instanceof ChatMessage) {
					ChatMessage msg = (ChatMessage) provider;
					if (msg.targetId == ChatMessage.OUMEN_TEAM_ID)
						return false;//偶们团队不能删
				}
				
				if (!isShowingDeleteDialog()) {
					showDeleteDialog(provider);
				}
				
				return true;
			}
			return false;
		}
	};
	
	private final NearMessage.NearListener nearListener = new NearMessage.NearListener() {
		
		@Override
		public void onUpdate() {
			host.getActivity().sendBroadcast(MessageService.createRequestNotify(MessageService.TYPE_REFRESH_NEWS));
//			getActivity().sendBroadcast(MessageService.createRequestNotify(MessageService.TYPE_REFRESH_NEWS));
		}
	};
	
	private void openNearActivity(NearMessage msg) {
		ELog.i("");
		MessageService.NEAR_NEWS = 0;
		getActivity().sendBroadcast(MessageService.createRequestNotify(MessageService.TYPE_REFRESH_NEWS));
		
		Intent intent = new Intent(getActivity(), NearActivity.class);
		getActivity().startActivity(intent);
	}
	
	private void openActivityRecommendActivity(TopPushActivityMessage msg) {
		startActivity(new Intent(getActivity(), PushActivityListActivity.class));
		
		msg.newCount = 0;
		ActivityMessage.updateAllRead(App.PREFS.getUid(), App.DB);

		getActivity().sendBroadcast(MessageService.createRequestNotify(MessageService.TYPE_REFRESH_NEWS));
	}
	
	private void openHelpActivity(HelpMessage msg) {
		Intent intent = new Intent(getActivity(), OumenCircleDetailActivity.class);
		intent.putExtra(BaseMessage.KEY_TARGET_ID, msg.groupId);
		startActivity(intent);
		
		msg.sendType = SendType.READ;
		HelpMessage.update2read(msg.datetime.getTime(), App.DB);

		getActivity().sendBroadcast(MessageService.createRequestNotify(MessageService.TYPE_REFRESH_NEWS));
	}
	
	private void openBookActivity(BookMessage msg) {
		ELog.i("");
		startActivity(new Intent(getActivity(), BookActivity.class));
		
		msg.setRead(true);
		msg.setNewCount(0);
		BookMessage.updateAllRead(App.PREFS.getUid(), App.DB);

		getActivity().sendBroadcast(MessageService.createRequestNotify(MessageService.TYPE_REFRESH_NEWS));
	}
	
	private void openChatActivity(BaseMessage msg) {
		if (TextUtils.isEmpty(App.PREFS.getUserProfile())) {
			//TODO 跳转到登录界面
			loginConfrim.openDialog();
			return;
		}
		
		Intent intent = new Intent(getActivity(), ChatActivity.class);
		intent.putExtra(ChatActivity.REQUEST_MESSAGE, msg);
		
		if (SendType.UNREAD.equals(msg.sendType)) {
			msg.sendType = SendType.READ;
		}
		if (msg instanceof MultiChatMessage) {
			MultiChatMessage.updateAllRead(App.PREFS.getUid(), msg.targetId, App.DB);
		}
		else {
			ChatMessage.update2Read(App.PREFS.getUid(), msg.targetId, App.DB);
		}
		getActivity().sendBroadcast(MessageService.createRequestNotify(MessageService.TYPE_REFRESH_NEWS));
		
		getActivity().startActivityForResult(intent, HomeFragment.REQUEST_CODE_MESSAGE_CENTER);
	}
	
	
	private final Runnable deleteTask = new Runnable() {
		
		@Override
		public void run() {
			int selfUid = App.PREFS.getUid();
			MessageListItemDataProvider provider = (MessageListItemDataProvider) dialogDelete.getTag();
			if (provider instanceof ChatMessage) {
				ChatMessage msg = (ChatMessage) provider;
				ChatMessage.delete(selfUid, msg.targetId, App.DB);
			}
			else if (provider instanceof HelpMessage) {
				HelpMessage msg = (HelpMessage) provider;
				HelpMessage.delete(msg.datetime.getTime(), App.DB);
			}
			else if (provider instanceof TopPushActivityMessage) {
			}
			else if (provider instanceof FriendMessage) {
				FriendMessage msg = (FriendMessage) provider;
				FriendMessage.delete(selfUid, msg.targetId, App.DB);
			}
			else if (provider instanceof BookMessage) {
//				BookMessage msg = (BookMessage) provider;
			}
			else if (provider instanceof MultiChatMessage) {
				MultiChatMessage msg = (MultiChatMessage) provider;
				MultiChatMessage.deleteAll(selfUid, msg.multiId, App.DB);
			}
			
			getActivity().sendBroadcast(MessageService.createRequestNotify(MessageService.TYPE_REFRESH_NEWS));
		}
	};

	//------------------ Dialog ------------------//
	private TwoButtonDialog dialogDelete;
	
	private void showDeleteDialog(MessageListItemDataProvider provider) {
		if (dialogDelete == null) {
			dialogDelete = new TwoButtonDialog(getActivity());
			dialogDelete.setCancelable(true);
			dialogDelete.getMessageView().setText(R.string.delete_confirm);
			dialogDelete.getLeftButton().setOnClickListener(clickListener);
			dialogDelete.getRightButton().setOnClickListener(clickListener);
		}
		dialogDelete.setTag(provider);
		dialogDelete.show();
	}
	
	private void hideDeleteDialog() {
		if (isShowingDeleteDialog())
			dialogDelete.dismiss();
	}
	
	private boolean isShowingDeleteDialog() {
		return dialogDelete != null && dialogDelete.isShowing();
	}
}
