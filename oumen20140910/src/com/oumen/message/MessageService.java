package com.oumen.message;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.widget.Toast;

import com.oumen.MainActivity;
import com.oumen.R;
import com.oumen.account.LoginTask;
import com.oumen.android.App;
import com.oumen.android.App.NetworkType;
import com.oumen.android.UserProfile;
import com.oumen.android.util.Constants;
import com.oumen.android.util.SharePreferenceUtil;
import com.oumen.app.ActivityStack;
import com.oumen.auth.AuthAdapter;
import com.oumen.biaoqing.DownLoadUtil;
import com.oumen.book.BookMessage;
import com.oumen.circle.CircleActivity;
import com.oumen.friend.Friend;
import com.oumen.home.CheckVersion;
import com.oumen.home.HomeFragment;
import com.oumen.http.DefaultHttpCallback;
import com.oumen.http.ExceptionHttpResult;
import com.oumen.http.HttpRequest;
import com.oumen.http.HttpResult;
import com.oumen.message.assist.Receipt;
import com.oumen.message.connection.ConnectionListener;
import com.oumen.message.connection.MessageConnection;
import com.oumen.message.ext.LoginMessage;
import com.oumen.message.ext.LogoutMessage;
import com.oumen.tools.ELog;
import com.oumen.util.NotificationUtil;

public class MessageService extends Service implements ConnectionListener, Handler.Callback {
	//通知的id
	private final int NOTIFY_ID_CHAT = 990;
	private final int NOTIFY_ID_OUMEN = 991;
	private final int NOTIFY_ID_MESSAGE = 992;

	public static final String REQUEST_ACTION = "com.oumen.AppContrller.Request";
	public static final String RESPONSE_ACTION = "com.oumen.AppContrller.Response";

	public static final String INTENT_KEY_TYPE = "type";
	public static final String INTENT_KEY_DATA = "data";
	public static final String INTENT_KEY_RESULT = "result";
	public static final String INTENT_KEY_PARAM = "param";
	public static final String INTENT_KEY_MESSAGE = "msg";

	public static final int TYPE_STOP_SERVICE = -1;
	public static final int TYPE_CONNECT = 0;
	public static final int TYPE_LOGIN = 1;// TODO LoginMessage 登录成功/被踢下线
	public static final int TYPE_LOGOUT = 2;// TODO LogoutMessage
	public static final int TYPE_LOGIN_INTERRUPT = 3;
	public static final int TYPE_BASE_MESSAGE = 4;
	public static final int TYPE_CHAT_MESSAGE = 5;
	public static final int TYPE_ACTIVITY_MESSAGE = 6;
	public static final int TYPE_ACTIVITY_MULTI_CHAT_STATUS_MESSAGE = 7;
	public static final int TYPE_ACTIVITY_MULTI_CHAT_MESSAGE = 8;
	public static final int TYPE_CIRCLE_MESSAGE = 9;
	public static final int TYPE_FRIEND_MESSAGE = 10;
	public static final int TYPE_HELP_MESSAGE = 11;
	public static final int TYPE_BOOK_MESSAGE = 12;
	public static final int TYPE_USERINFO = 13;
	public static final int TYPE_REFRESH_NEWS = 14;//消息提醒
	public static final int TYPE_NOTIFICATION = 15;// 通知栏消息
	public static final int TYPE_CHECK_VERSION = 16;
	public static final int TYPE_NETWORK_STATUS = 17;
	public static final int TYPE_SWITCH_FRAGMENT = 18;
	public static final int TYPE_UPDATE_FRIEND_LIST = 19; // 更新好友列表
	public static final int TYPE_WEIXIN_AUTH = 20;
	public static final int TYPE_NOTIFY_LOCATION = 21;

	public static final int RESULT_SUCCESS = 1;
	public static final int RESULT_FAILED = 2;

	public static final int PARAM_TIMEOUT = 1;

	public static boolean isRunning = false;

	private final int HANDLER_SHOW_TOAST = 3;

	// 消息中心返回信息列表
	public static final Map<Long, BaseMessage> CACHE_MESSAGE = new HashMap<Long, BaseMessage>();

	public static final AtomicInteger NEWS = new AtomicInteger(0);
	public static int NEAR_NEWS = 0;

	private App app;
	private SharePreferenceUtil prefs;
	private LoginTask loginManager;

	private NotificationManager notificationManager;

	private BroadcastReceiver requestReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			int type = intent.getIntExtra(INTENT_KEY_TYPE, -99);
//			ELog.w("Type:" + type);
			if (type == TYPE_LOGIN) {
				synchronized (loginManager) {
					int from = intent.getIntExtra(LoginTask.KEY_FROM, App.INT_UNSET);
					ELog.w("isLogin:" + LoginTask.hadLogin() + " From:" + (from == LoginTask.FROM_AUTO ? "auto" : "input"));
					if (from == LoginTask.FROM_AUTO && LoginTask.hadLogin())
						return;

					if (intent.hasExtra(LoginTask.KEY_INFO_ARRAY)) {
						loginManager.buildCache();

						String[] info = intent.getStringArrayExtra(LoginTask.KEY_INFO_ARRAY);
						loginManager.setEmail(info[0]);
						loginManager.setPassword(info[1]);
						
						if (info.length == 4) {//TODO 邮箱注册和手机注册
							loginManager.setNickname(info[2]);//手机注册的昵称没有用处，就是为了和邮箱注册走同一个通道
							loginManager.setRegisterFlag(info[3]);
						}
					}
					else {
						loginManager.reset();
					}
					if (intent.hasExtra(LoginTask.KEY_THIRDPARTY)) {
						loginManager.setThirdpartType((AuthAdapter.Type) intent.getSerializableExtra(LoginTask.KEY_THIRDPARTY));
					}
					if (intent.hasExtra(LoginTask.KEY_GUEST_ID)) {
						loginManager.setGuestId(intent.getStringExtra(LoginTask.KEY_GUEST_ID));
					}

					App.THREAD.execute(loginManager);
				}
			}
			else if (type == TYPE_LOGOUT) {
				synchronized (loginManager) {
					loginManager.logout();
				}
				//去掉消息通知
				removeNotification();
			}
			else if (type == TYPE_LOGIN_INTERRUPT) {
				synchronized (loginManager) {
					loginManager.interrupt();
				}
			}
			else if (type == TYPE_NETWORK_STATUS) {
				NetworkType networkType = App.getNetworkType();
//				ELog.i("Email:" + App.PREFS.getEmail() + " isLogin:" + LoginTask.isLogin() + " isConnect:" + MessageConnection.instance.isConnected());
				if (networkType.equals(NetworkType.NONE)) {
					Activity current = ActivityStack.getCurrent();
					if (current != null) {
						Toast.makeText(current, R.string.err_network_invalid, Toast.LENGTH_SHORT).show();
					}
					MessageConnection.instance.close(false, true);
				}
				else if (!MessageConnection.instance.isConnected() && App.PREFS.getEmail() != null && !TextUtils.isEmpty(App.PREFS.getPwd())) {
					loginManager.retryConnectOnNewThread();
				}
			}
			else if (type == TYPE_REFRESH_NEWS) {
				refreshNews();
			}
			else if (type == TYPE_STOP_SERVICE) {
				stopSelf();
				isRunning = false;
			}
			else if (type == TYPE_USERINFO) {
				synchronized (loginManager) {
					App.THREAD.execute(loginManager.obtainMyInfo());
				}
			}
			else if (type == TYPE_ACTIVITY_MESSAGE) {
				if (!App.NetworkType.NONE.equals(App.getNetworkType())) {
					int[] ids = intent.getIntArrayExtra(INTENT_KEY_DATA);
					for (int id : ids) {
						obtianActivityDetail(id);
					}
				}
			}
			else if (type == TYPE_NOTIFICATION) {// 在后台运行通知
				// 将通知栏去掉
				removeNotification();
			}
			else if (type == TYPE_FRIEND_MESSAGE) {//添加好友
				UserProfile profile = App.USER;
				int targetId = intent.getIntExtra(INTENT_KEY_DATA, App.INT_UNSET);
				if (targetId != App.INT_UNSET) {
					SendMessage mBean = new SendMessage(prefs.getUid(), profile.getNickname(), profile.getPhotoSourceUrl(), targetId, new Date(App.getServerTime()), ActionType.AGREE_FRIEND, Type.TEXT, "agree!");
					MessageConnection.instance.send(mBean.toMessage());
				}
			}
			else if (type == TYPE_CHECK_VERSION) {
				new CheckVersion(context).checkCurrentVersion();
			}
		}
	};

	/**
	 * 消息提醒
	 */
	private void refreshNews() {
		synchronized (App.DB) {
			int selfUid = prefs.getUid();
			int chats = ChatMessage.queryNewCount(selfUid, App.DB);
			int multiChats = MultiChatMessage.queryNewCount(selfUid, App.DB);
			int friends = FriendMessage.queryNewCount(selfUid, App.DB);
			int helps = HelpMessage.queryNewCount(selfUid, App.DB);
			int activitys = ActivityMessage.queryNewCount(selfUid, App.DB);
			int books = BookMessage.queryNewCount(selfUid, App.DB);

			NEWS.set(chats + multiChats + friends + helps + activitys + books + (NEAR_NEWS > 0 ? 1 : 0));
			ELog.i("News:" + NEWS.get() + "(Chats:" + chats + " Multis:" + multiChats + " Friends:" + friends + " Helps:" + helps + " Activitys:" + activitys + " Books:" + books + " Nears:" + NEAR_NEWS + ")");

			Intent notify = createResponseNotify(TYPE_REFRESH_NEWS);
			sendBroadcast(notify);
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		ELog.i("");

		app = (App) getApplication();
		prefs = new SharePreferenceUtil(getApplicationContext(), Constants.SAVE_USER);

		loginManager = new LoginTask(app);

		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		app.setmNotificationManager(notificationManager);

		IntentFilter filter = new IntentFilter();
		filter.addAction(REQUEST_ACTION);
		registerReceiver(requestReceiver, filter);

		MessageConnection.instance.setConnectionListener(this);

		if (!App.NetworkType.NONE.equals(App.getNetworkType())) {
			new CheckVersion(this).checkCurrentVersion();
		}

		isRunning = true;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		ELog.i("");
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		ELog.i("");
		unregisterReceiver(requestReceiver);
		removeNotification();
		isRunning = false;

		super.onDestroy();
	}

	@Override
	public void input(Object in) {
		if (in == null)
			return;

		if (in instanceof ChatMessage) { // TODO 单聊
			long time1 = System.currentTimeMillis();
			ELog.i(String.valueOf(time1));
			final ChatMessage msg = (ChatMessage) in;
			Receipt receipt = new Receipt(String.valueOf(prefs.getUid()), String.valueOf(msg.targetId), String.valueOf(msg.datetime.getTime()));
			MessageConnection.instance.send(receipt.toMessage());

			//TODO 判断是否有单张表情
			boolean exist = true;
			if (Type.OUBA.equals(msg.type)) {
				exist = DownLoadUtil.hasFile(App.getChatBiaoqingOubaPath() + "/" + msg.getContent());
			}
			else if (Type.CIWEI.equals(msg.type)) {
				exist = DownLoadUtil.hasFile(App.getChatBiaoqingCiweiPath() + "/" + msg.getContent());
			}
			if (!exist) {
				App.THREAD.execute(new Runnable() {

					@Override
					public void run() {
						DownLoadUtil.DownLoadOneBiaoqing(msg.type, msg.getContent());

						//将数据插入到数据库
						if (Type.OUBA.equals(msg.type) || Type.CIWEI.equals(msg.type)) {
							msg.setContent(msg.getContent() + App.FILE_SUFFIX);
						}
						msg.sendType = SendType.UNREAD;
						msg.setSend(false);
						ChatMessage.insert(msg, App.DB);

						if (App.PREFS.isAcceptMsg()) {
							sendNotification(msg);
						}
					}
				});
			}
			else {
				//将数据插入到数据库
				if (Type.OUBA.equals(msg.type) || Type.CIWEI.equals(msg.type)) {
					msg.setContent(msg.getContent() + App.FILE_SUFFIX);
				}
				msg.sendType = SendType.UNREAD;
				msg.setSend(false);
				ChatMessage.insert(msg, App.DB);

				if (App.PREFS.isAcceptMsg()) {
					sendNotification(msg);
				}
			}
			ELog.i(String.valueOf(System.currentTimeMillis() - time1));
		}
		else if (in instanceof MultiChatMessage) { // TODO 群聊
			final MultiChatMessage msg = (MultiChatMessage) in;
			Receipt receipt = new Receipt(String.valueOf(prefs.getUid()), String.valueOf(msg.multiId), String.valueOf(msg.datetime.getTime()));
			MessageConnection.instance.send(receipt.toMessage());

			//TODO 判断是否有单张表情
			boolean exist = true;
			if (Type.OUBA.equals(msg.type)) {
				exist = DownLoadUtil.hasFile(App.getChatBiaoqingOubaPath() + "/" + msg.getContent());
			}
			else if (Type.CIWEI.equals(msg.type)) {
				exist = DownLoadUtil.hasFile(App.getChatBiaoqingCiweiPath() + "/" + msg.getContent());
			}
			if (!exist) {
				App.THREAD.execute(new Runnable() {

					@Override
					public void run() {
						DownLoadUtil.DownLoadOneBiaoqing(msg.type, msg.getContent());

						//将数据插入到数据库
						if (Type.OUBA.equals(msg.type) || Type.CIWEI.equals(msg.type)) {
							msg.setContent(msg.getContent() + App.FILE_SUFFIX);
						}
						msg.selfName = App.USER.getNickname();
						msg.selfPhotoUrl = App.USER.getPhotoSourceUrl();
						msg.sendType = SendType.UNREAD;
						msg.send = false;
						MultiChatMessage.insert(prefs.getUid(), msg, App.DB);

						Activity currentActivity = ActivityStack.getCurrent();
						if (currentActivity != null && currentActivity instanceof NotificationObserver) {
							NotificationObserver no = (NotificationObserver) currentActivity;
							if (NotificationObserver.Page.CHAT.equals(no.getCurrentPage()) && no.getTargetId() == msg.multiId) {
								ELog.i("Ignore notification");
								no.receiveNotification(msg);
							}
						}
					}
				});
			}
			else {
				//将数据插入到数据库
				if (Type.OUBA.equals(msg.type) || Type.CIWEI.equals(msg.type)) {
					msg.setContent(msg.getContent() + App.FILE_SUFFIX);
				}
				msg.selfName = App.USER.getNickname();
				msg.selfPhotoUrl = App.USER.getPhotoSourceUrl();
				msg.sendType = SendType.UNREAD;
				msg.send = false;
				MultiChatMessage.insert(prefs.getUid(), msg, App.DB);

				Activity currentActivity = ActivityStack.getCurrent();
				if (currentActivity != null && currentActivity instanceof NotificationObserver) {
					NotificationObserver no = (NotificationObserver) currentActivity;
					if (NotificationObserver.Page.CHAT.equals(no.getCurrentPage()) && no.getTargetId() == msg.multiId) {
						ELog.i("Ignore notification");
						no.receiveNotification(msg);
					}
				}
			}
		}
		else if (in instanceof FriendMessage) { // TODO 好友请求
			FriendMessage msg = (FriendMessage) in;
			Receipt receipt = new Receipt(String.valueOf(prefs.getUid()), String.valueOf(msg.targetId), String.valueOf(msg.datetime.getTime()));
			MessageConnection.instance.send(receipt.toMessage());

			FriendMessage.insert(msg, App.DB);

			if (!ActionType.AGREE_FRIEND.equals(msg.getActionType())) {// 如果过来的是同意好友请求
				//TODO confirm_friend 返回没有targetName字段，所以从数据库中取消息
				if (App.PREFS.isAcceptMsg()) {
					sendNotification(FriendMessage.query(App.USER.getUid(), msg.getTargetUid(), App.DB));
				}

				Intent notify = createResponseNotify(TYPE_FRIEND_MESSAGE);
				sendBroadcast(notify);
			}

			if (ActionType.CONFIRM_FRIEND.equals(msg.getActionType())) {// 如果过来的是同意好友请求
				//1. 清空好友请求对应的数据库信息
				FriendMessage tempMsg = FriendMessage.query(App.USER.getUid(), msg.getTargetUid(), App.DB);// TODO 此处这么处理是因为返回来没有昵称和头像，需要从数据库中查询得到
				//2.在聊天数据库中添加和此人的请求消息
				ChatMessage bean = new ChatMessage();
				bean.setToId(App.USER.getUid());
				bean.setSelfName(App.USER.getNickname());
				bean.setSelfPhotoUrl(App.USER.getPhotoSourceUrl());
				bean.setTargetId(tempMsg.getTargetUid());
				bean.setTargetNickname(tempMsg.getTargetNickname());
				bean.setTargetPhotoUrl(tempMsg.getTargetPhotoSourceUrl());
				bean.setContent("嗨");
				bean.setActionType(ActionType.CHAT);
				bean.setType(Type.TEXT);
				bean.setDatetime(tempMsg.getDatetime());
				bean.setSendType(SendType.UNREAD);
				bean.setSend(false);
				ChatMessage.insert(bean, App.DB);

				//3.删除对应好友请求数据
				FriendMessage.delete(App.USER.getUid(), msg.getTargetUid(), App.DB);

				//4.更新好友列表
				Friend friend = new Friend();
				friend.setUid(msg.getTargetUid());
				friend.setNickname(tempMsg.getTargetNickname());
				friend.setPhotoUrl(tempMsg.getTargetPhotoSourceUrl());

				Friend.insert(friend, App.PREFS.getUid(), App.DB);

				obtainFriendList();
				// 5. 发广播
				if (App.PREFS.isAcceptMsg()) {
					sendNotification(bean);
				}
			}

		}
		else if (in instanceof HelpMessage) { // TODO 求助
			HelpMessage msg = (HelpMessage) in;
			Receipt receipt = new Receipt(String.valueOf(prefs.getUid()), String.valueOf(msg.targetId), String.valueOf(msg.datetime.getTime()));
			MessageConnection.instance.send(receipt.toMessage());

			HelpMessage.insert(prefs.getUid(), msg, App.DB);

			Intent notify = createResponseNotify(TYPE_HELP_MESSAGE);
			sendBroadcast(notify);
		}
		else if (in instanceof BookMessage) { // TODO 育儿宝典
			BookMessage msg = (BookMessage) in;
			BookMessage.insert(prefs.getUid(), msg, App.DB);

			Intent notify = createResponseNotify(TYPE_BOOK_MESSAGE);
			sendBroadcast(notify);
		}
		else if (in instanceof ActivityMultiChatStatusMessage) {
			ActivityMultiChatStatusMessage msg = (ActivityMultiChatStatusMessage) in;
			Receipt receipt = new Receipt(String.valueOf(prefs.getUid()), String.valueOf(msg.targetId), String.valueOf(msg.datetime.getTime()));
			MessageConnection.instance.send(receipt.toMessage());

			MultiChatMessage multiChat = new MultiChatMessage(msg);
			multiChat.selfName = App.USER.getNickname();
			multiChat.selfPhotoUrl = App.USER.getPhotoSourceUrl();
			MultiChatMessage.insert(prefs.getUid(), multiChat, App.DB);

			Intent notify = createResponseNotify(TYPE_ACTIVITY_MULTI_CHAT_STATUS_MESSAGE);
			notify.putExtra(MultiChatMessage.KEY_MULTI_ID, multiChat.getMultiId());
			sendBroadcast(notify);
		}
		else if (in instanceof Receipt) {// TODO 发送消息的回执
			Receipt receipt = (Receipt) in;
			if (!TextUtils.isEmpty(receipt.getTimestamp())) {
				long time = Long.valueOf(receipt.getTimestamp());
				Object tempObj = CACHE_MESSAGE.get(time);
				if (tempObj instanceof ChatMessage) {//私聊的回执
					//1.将消息添加到数据库
					ChatMessage msg = (ChatMessage) tempObj;
					msg.sendType = SendType.READ;
//					ChatMessage.insert(msg, App.DB);
					ChatMessage.updateSendTypeMsg(prefs.getUid(), msg, App.DB);
					//2.向界面发送广播
					Intent notify = createResponseNotify(TYPE_CHAT_MESSAGE);
					notify.putExtra(BaseMessage.KEY_TARGET_ID, msg.getTargetUid());
					notify.putExtra(INTENT_KEY_DATA, true);
					sendBroadcast(notify);
				}
				else if (tempObj instanceof MultiChatMessage) {//群聊的回执
					//1.将消息添加到数据库
					MultiChatMessage msg = (MultiChatMessage) tempObj;
					msg.sendType = SendType.READ;
//					MultiChatMessage.insert(prefs.getUid(), msg, App.DB);
					MultiChatMessage.updateSendTypeMsg(prefs.getUid(), msg, App.DB);
					//2.向界面发送广播
					Intent notify = createResponseNotify(TYPE_ACTIVITY_MULTI_CHAT_MESSAGE);
					notify.putExtra(MultiChatMessage.KEY_MULTI_ID, msg.getMultiId());
					notify.putExtra(INTENT_KEY_DATA, true);
					sendBroadcast(notify);
				}
				else if (tempObj instanceof FriendMessage) {//添加好友的回执
					//1.将消息添加到数据库
					FriendMessage msg = (FriendMessage) tempObj;
					FriendMessage.insert(msg, App.DB);
					//2.向界面发送广播
					Intent notify = createResponseNotify(TYPE_FRIEND_MESSAGE);
					sendBroadcast(notify);
				}
			}
		}
		else if (in instanceof CircleMessage) { // TODO 偶们圈消息
			CircleMessage msg = (CircleMessage) in;
			if (msg.getTargetUid() != prefs.getUid()) {// 如果为自己发的消息，就忽略.不是，再存入数据库，发广播
				//将消息插入到数据库中
				msg.sendType = SendType.UNREAD;
				CircleMessage.insert(msg, App.DB);

				if (App.PREFS.isAcceptMsg()) {
					sendNotification(msg);
				}

				Intent notify = createResponseNotify(TYPE_CIRCLE_MESSAGE);
				sendBroadcast(notify);
			}
		}
		else if (in instanceof ActivityMessage) {
			Intent notify = createResponseNotify(TYPE_ACTIVITY_MESSAGE);
			sendBroadcast(notify);
		}
		else if (in instanceof ActivityGroupMessage) { // TODO 活动推荐
			ActivityGroupMessage msg = (ActivityGroupMessage) in;
			msg.insert(prefs.getUid(), App.DB);

			Intent notify = createResponseNotify(TYPE_ACTIVITY_MESSAGE);
			sendBroadcast(notify);
		}
		else if (in instanceof LoginMessage) { // TODO 登录
			LoginMessage msg = (LoginMessage) in;
			loginManager.processLoginMessage(msg, app);
		}
		else if (in instanceof LogoutMessage) { // TODO 登出
			LogoutMessage msg = (LogoutMessage) in;
			if (msg.isSuccess()) {
				// 发广播
				Intent notify = createResponseNotify(TYPE_LOGOUT);
				sendBroadcast(notify);
			}
		}
		else if (in instanceof BaseMessage) {
			BaseMessage msg = (BaseMessage) in;
			Receipt receipt = new Receipt(String.valueOf(prefs.getUid()), String.valueOf(msg.targetId), String.valueOf(msg.datetime.getTime()));
			MessageConnection.instance.send(receipt.toMessage());

			if (App.PREFS.isAcceptMsg()) {
				if (ActionType.REQUEST_FRIEND.equals(msg.actionType)) {
					sendNotification(msg);
				}
			}

			Intent notify = createResponseNotify(TYPE_BASE_MESSAGE);
			sendBroadcast(notify);
		}
		refreshNews();
	}

	@Override
	public void onSendFailed(String reference, Exception e) {
		try {
			ELog.i(reference);
			JSONObject json = new JSONObject(reference);
			if (json.has("Message")) {
				//聊天发送失败
				JSONObject reslut = json.getJSONObject("Message");
				long time = reslut.getLong("timestamp");
				Object tempObj = CACHE_MESSAGE.get(time);
				if (tempObj instanceof ChatMessage) {
					ChatMessage msg = (ChatMessage) tempObj;
					// 修改数据库中对应的数据
					ChatMessage.updateSendTypeMsg(App.USER.getUid(), msg.getTargetUid(), msg.getSendType(), SendType.SENDFAIL, msg.getTimestamp(), App.DB);

					msg.setSendType(SendType.SENDFAIL);

					Activity currentActivity = ActivityStack.getCurrent();
					if (currentActivity != null && currentActivity instanceof NotificationObserver) {
						NotificationObserver no = (NotificationObserver) currentActivity;
						if (NotificationObserver.Page.CHAT.equals(no.getCurrentPage()) && no.getTargetId() == msg.targetId) {
							no.receiveNotification(0);
						}
					}
				}
				else if (tempObj instanceof MultiChatMessage) {
					MultiChatMessage msg = (MultiChatMessage) tempObj;
					MultiChatMessage.updateSendTypeMsg(App.USER.getUid(), msg.getMultiId(), msg.getSendType(), SendType.SENDFAIL, msg.getDatetime(), App.DB);

					msg.setSendType(SendType.SENDFAIL);

					Activity currentActivity = ActivityStack.getCurrent();
					if (currentActivity != null && currentActivity instanceof NotificationObserver) {
						NotificationObserver no = (NotificationObserver) currentActivity;
						if (NotificationObserver.Page.CHAT.equals(no.getCurrentPage()) && no.getTargetId() == msg.multiId) {
							no.receiveNotification(0);
						}
					}
				}
				// TODO　发送好友请求失败怎么处理？？？
//				else if (tempObj instanceof FriendMessage) {
//					FriendMessage msg = (FriendMessage) tempObj;
//				}
			}
		}
		catch (Exception ex) {
		}
	}

	/**
	 * 请求好友列表
	 */
	private void obtainFriendList() {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("user_id", String.valueOf(App.PREFS.getUid())));
		HttpRequest req = new HttpRequest(Constants.FIND_FRIEND_SERVICE, params, HttpRequest.Method.GET, new DefaultHttpCallback(new DefaultHttpCallback.EventListener() {

			@Override
			public void onSuccess(HttpResult result) {
				try {
					String str = result.getResult();
					ELog.i(str);

					int selfUid = App.PREFS.getUid();
					JSONArray array = new JSONArray(str);
					LinkedList<Friend> friends = new LinkedList<Friend>();
					for (int i = 0; i < array.length(); i++) {
						JSONObject obj = array.getJSONObject(i);

						try {
							friends.add(new Friend(obj));
						}
						catch (JSONException e) {
							ELog.e("Exception:" + e.getMessage());
							e.printStackTrace();
						}
					}
					// TODO 此处还需要处理，每次取到信息好友信息就把原来的好友都删除了，然后再把新的数据插入到数据库
//					Friend.deleteAll(selfUid, App.DB);
					Friend.insert(friends, selfUid, App.DB);

					// TODO 此处为什么要发广播，没有搞清楚，先不要删除
					Intent notify = createResponseNotify(TYPE_UPDATE_FRIEND_LIST);
					sendBroadcast(notify);

				}
				catch (Exception e) {
					ELog.e("Exception:" + e.getMessage());
					e.printStackTrace();
				}
			}

			@Override
			public void onForceClose(ExceptionHttpResult result) {
			}

			@Override
			public void onException(ExceptionHttpResult result) {
				ELog.e("Exception:" + result.getException().getMessage());
			}
		}));
		App.THREAD.execute(req);
	}

	/**
	 * 获取活动详情
	 * 
	 * @param activityId
	 */
	synchronized private void obtianActivityDetail(int activityId) {
		ArrayList<NameValuePair> list = new ArrayList<NameValuePair>();
		list.add(new BasicNameValuePair("atid", String.valueOf(activityId)));
		list.add(new BasicNameValuePair("uid", String.valueOf(prefs.getUid())));
		HttpRequest req = new HttpRequest(Constants.GET_AMUSEMENT_DETAIL, list, HttpRequest.Method.GET, new DefaultHttpCallback(new DefaultHttpCallback.EventListener() {

			@Override
			public void onSuccess(HttpResult result) {
				try {
					String str = result.getResult();
					ELog.i(str);

					if ("[]".equals(str))
						return;

					JSONObject obj = new JSONObject(str);
					ActivityMessage msg = new ActivityMessage(obj, ActivityMessage.FROM_HTTP);
					ActivityMessage.insert(prefs.getUid(), msg, App.DB);

					boolean hasCreatedMessage = MultiChatMessage.hasActivityCreateMessage(prefs.getUid(), msg.multiId, App.DB);
					if (!hasCreatedMessage) {
						List<MultiChatMessage> exists = MultiChatMessage.querySingleGroup(prefs.getUid(), msg.multiId, App.INT_UNSET, App.INT_UNSET, 1, App.DB);
						long datetime;
						if (exists.isEmpty()) {
							datetime = App.getServerTime();
						}
						else {
							datetime = exists.get(0).datetime.getTime() - 1;

							for (MultiChatMessage i : exists) {
								//因为群聊消息插入数据库比群创建消息早，所以分组查询时群创建消息会成为最新消息（实际应该比群聊消息早），
								//所以这里先删掉群聊消息，然后在群创建消息插入数据库以后重新插入
								MultiChatMessage.delete(i.datetime.getTime(), App.DB);
							}
						}
						MultiChatMessage multiMsg = new MultiChatMessage();
						multiMsg.selfName = App.USER.getNickname();
						multiMsg.selfPhotoUrl = App.USER.getPhotoSourceUrl();
//						multiMsg.actionType = ActionType.MULTI_CREATE;
						multiMsg.type = Type.TEXT;
						multiMsg.activityId = msg.id;
						multiMsg.multiId = msg.multiId;
//						multiMsg.read = false;
						multiMsg.sendType = SendType.UNREAD;
						multiMsg.datetime = new Date(datetime);
						if (msg.ownerId == prefs.getUid()) {
							multiMsg.actionType = ActionType.MULTI_CREATE;
							multiMsg.content = MultiChatMessage.getCreateMultiChatInfo(app);
						}
						else {
							multiMsg.actionType = ActionType.MULTI_JOIN;
							// TODO 此处处理是为了，让用户填写电话号码的判断
							multiMsg.targetId = App.USER.getUid();
							multiMsg.content = MultiChatMessage.getJoinMultiChatInfo(app, msg.title);
						}
						MultiChatMessage.insert(prefs.getUid(), multiMsg, App.DB);

						if (!exists.isEmpty()) {
							Collections.reverse(exists);
							for (MultiChatMessage i : exists) {
								//将群聊消息重新插入数据库
								MultiChatMessage.insert(prefs.getUid(), i, App.DB);
							}
						}
					}

					Intent notify = createResponseNotify(TYPE_ACTIVITY_MESSAGE);
					notify.putExtra(INTENT_KEY_DATA, msg);
					sendBroadcast(notify);
				}
				catch (Exception e) {
					ELog.e("Exception:" + e.getMessage());
					e.printStackTrace();
				}
			}

			@Override
			public void onForceClose(ExceptionHttpResult result) {
				ELog.e("Exception:" + result.getException() == null ? "" : result.getException().getMessage());
			}

			@Override
			public void onException(ExceptionHttpResult result) {
				ELog.e("Exception:" + result.getException() == null ? "" : result.getException().getMessage());
			}
		}));
		App.THREAD.execute(req);
	}

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
			case HANDLER_SHOW_TOAST:
				Toast.makeText(app, (String) msg.obj, Toast.LENGTH_SHORT).show();
				break;
		}
		return false;
	}

	public static Intent createResponseNotify(int type) {
		Intent notify = new Intent(RESPONSE_ACTION);
		notify.putExtra(INTENT_KEY_TYPE, type);
		return notify;
	}

	public static Intent createRequestNotify(int type) {
		Intent notify = new Intent(REQUEST_ACTION);
		notify.putExtra(INTENT_KEY_TYPE, type);
		return notify;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private PendingIntent createOpenChatPendingIntent(BaseMessage msg) {
		Intent intent = new Intent(this, MainActivity.class);
		intent.putExtra(MainActivity.INTENT_KEY_CURRENT_FRAGMENT, MainActivity.Frag.HOME);
		intent.putExtra(MainActivity.TYPE, HomeFragment.TYPE_MESSAGE);
		return PendingIntent.getActivity(app, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}

	private PendingIntent createOpenFriendPendingIntent(BaseMessage msg) {
		Intent intent = new Intent(this, MainActivity.class);
		intent.putExtra(MainActivity.INTENT_KEY_CURRENT_FRAGMENT, MainActivity.Frag.HOME);
		intent.putExtra(MainActivity.TYPE, HomeFragment.TYPE_MESSAGE);
		return PendingIntent.getActivity(app, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}

	private PendingIntent createOpenOumenPendingIntent(BaseMessage msg) {
		//TODO 偶们圈消息提醒
		Intent intent = new Intent(this, CircleActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		return PendingIntent.getActivity(app, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}

	private boolean sendNotification(BaseMessage msg) {
		if (msg instanceof ChatMessage) {
			PendingIntent contentIntent = createOpenChatPendingIntent(msg);
			Activity currentActivity = ActivityStack.getCurrent();
			if (currentActivity != null && currentActivity instanceof NotificationObserver) {
				NotificationObserver no = (NotificationObserver) currentActivity;
				if (NotificationObserver.Page.CHAT.equals(no.getCurrentPage()) && no.getTargetId() == msg.targetId) {
					ELog.i("Ignore notification");
					no.receiveNotification(msg);
					return true;
				}
			}

			String ticker = null;
			String content = null;
			if (Type.TEXT.equals(msg.type)) {
				ticker = "您有新的聊天消息：" + msg.getTargetNickname() + ":" + msg.getContent();
				content = msg.getContent();
			}
			else {
				ticker = "您有新的聊天消息：" + msg.getTargetNickname() + ":表情";
				content = ":表情";
			}
			sendNotification(NOTIFY_ID_CHAT, ticker, msg.targetNickname, content, BitmapFactory.decodeResource(getResources(), R.drawable.icon), contentIntent);
		}
		else if (msg instanceof FriendMessage) {
			PendingIntent contentIntent = createOpenFriendPendingIntent(msg);
			Activity currentActivity = ActivityStack.getCurrent();
			if (currentActivity != null && currentActivity instanceof NotificationObserver) {
				NotificationObserver no = (NotificationObserver) currentActivity;
				if (NotificationObserver.Page.MESSAGE.equals(no.getCurrentPage())) {
					ELog.i("Ignore notification");
					no.receiveNotification(msg);
					return true;
				}
			}
			if (ActionType.CONFIRM_FRIEND.equals(msg.actionType)) {
				String title = msg.targetNickname + "同意添加您为好友";
				sendNotification(NOTIFY_ID_MESSAGE, title, msg.targetNickname, msg.content, BitmapFactory.decodeResource(getResources(), R.drawable.icon), contentIntent);
			}
			else if (ActionType.REQUEST_FRIEND.equals(msg.actionType)) {
				String title = msg.targetNickname + "请求添加您为好友";
				sendNotification(NOTIFY_ID_MESSAGE, title, msg.targetNickname, msg.content, BitmapFactory.decodeResource(getResources(), R.drawable.icon), contentIntent);
			}
		}
		else if (msg instanceof CircleMessage) {
			PendingIntent contentIntent = createOpenOumenPendingIntent(msg);
			Activity currentActivity = ActivityStack.getCurrent();
			if (currentActivity != null && currentActivity instanceof NotificationObserver) {
				NotificationObserver no = (NotificationObserver) currentActivity;
				if (NotificationObserver.Page.OTHER.equals(no.getCurrentPage())) {
					ELog.i("Ignore notification");
					no.receiveNotification(msg);
					return true;
				}
			}
			sendNotification(NOTIFY_ID_OUMEN, "您有新的偶们圈消息", msg.targetNickname, msg.content, BitmapFactory.decodeResource(getResources(), R.drawable.icon), contentIntent);
		}
		return false;
	}

	private void sendNotification(int notifyId, String ticker, String title, String content, Bitmap largeIcon, PendingIntent contentIntent) {
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this).setSmallIcon(R.drawable.icon).setTicker(ticker).setContentTitle(title).setContentText(content).setLargeIcon(largeIcon).setContentIntent(contentIntent).setAutoCancel(true);
		Notification notification = builder.build();
		notification = NotificationUtil.setAlarmParams(getApplicationContext(), notification);
		notificationManager.notify(notifyId, notification);
	}

	public void removeNotification() {
		notificationManager.cancel(NOTIFY_ID_OUMEN);
		notificationManager.cancel(NOTIFY_ID_CHAT);
		notificationManager.cancel(NOTIFY_ID_MESSAGE);
	}

}
