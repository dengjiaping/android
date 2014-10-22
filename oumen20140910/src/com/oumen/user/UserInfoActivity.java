package com.oumen.user;

import java.util.ArrayList;
import java.util.Date;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.oumen.R;
import com.oumen.TitleBar;
import com.oumen.android.App;
import com.oumen.android.BaseActivity;
import com.oumen.android.peers.entity.CircleUserBasicMsg;
import com.oumen.android.util.Constants;
import com.oumen.chat.ChatActivity;
import com.oumen.home.LoginConfrim;
import com.oumen.http.DefaultHttpCallback;
import com.oumen.http.DefaultHttpCallback.EventListener;
import com.oumen.http.ExceptionHttpResult;
import com.oumen.http.HttpRequest;
import com.oumen.http.HttpResult;
import com.oumen.message.ActionType;
import com.oumen.message.BaseMessage;
import com.oumen.message.ChatMessage;
import com.oumen.message.MessageService;
import com.oumen.message.SendMessage;
import com.oumen.message.SendType;
import com.oumen.message.Type;
import com.oumen.message.connection.MessageConnection;
import com.oumen.tools.ELog;
import com.oumen.widget.dialog.SingleEditorDialog;
import com.oumen.widget.file.ImageData;
import com.oumen.widget.image.ImagePreviewActivity;
import com.oumen.widget.list.HSZFooterView;
import com.oumen.widget.list.HSZHeaderListView;
import com.oumen.widget.list.HSZListViewAdapter;
import com.oumen.widget.preview.image.ImagePagerFragment;

/**
 * 用户个人中心界面
 * 
 */
public class UserInfoActivity extends BaseActivity {
	public static final String INTENT_KEY_UID = "uid";
	public static final String INTENT_KEY_GENDER = "gender";

	private final int HANLDER_GETUSERMESSAGE_SUCCESS = 1;
	private final int HANLDER_GETUSERMESSAGE_FAIL = 2;
	private final int HANDLER_ADD_FRIEDN_FLAG = 3;
	//标题行控件
	private TitleBar titlebar;
	private Button btnRight;
	private Button btnLeft;
	//listview控件
	private HSZHeaderListView<CircleUserBasicMsg> listView;
	private HSZFooterView footerView;
	private PersonCirlceHeaderView headerView;// 头文件信息

	private SingleEditorDialog dialogInput;

	private UserInfo info;
	private final UserInfoAdapter adapter = new UserInfoAdapter();

	private int uid = 0;

	private int page = 1;
	private int totalPage = 1;

	private HttpRequest req;

	private LoginConfrim loginConfrim;
	private boolean first = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.usercircle);

		init();
		uid = getIntent().getIntExtra(INTENT_KEY_UID, 0);
		if (uid > 0) {
			// 初始化
			listView.headerLoad();
		}
		else {
			handler.sendMessage(handler.obtainMessage(HANLDER_GETUSERMESSAGE_FAIL, "获取用户信息失败"));
		}
		loginConfrim = new LoginConfrim(this);
	}

	@SuppressWarnings("unchecked")
	private void init() {
		// 标题行控件
		titlebar = (TitleBar) findViewById(R.id.titlebar);
		btnLeft = titlebar.getLeftButton();
		btnRight = titlebar.getRightButton();
		titlebar.getTitle().setText("个人中心");
		btnRight.setText(getResources().getString(R.string.chat));

		// ------------ListView-------------
		headerView = new PersonCirlceHeaderView(UserInfoActivity.this);
		footerView = new HSZFooterView(UserInfoActivity.this);

		listView = (HSZHeaderListView<CircleUserBasicMsg>) findViewById(R.id.list);
		listView.setBackgroundColor(getResources().getColor(R.color.user_bg));
		listView.setDivider(new ColorDrawable(getResources().getColor(R.color.transparent)));
		listView.setDividerHeight(1);
		listView.setFooterView(footerView, getResources().getDimensionPixelSize(R.dimen.list_footer_height_default));
		listView.setHeaderView(headerView);
		listView.setAdapter(adapter);
		listView.setSelector(android.R.color.transparent);

		btnLeft.setOnClickListener(clickListener);
		btnRight.setOnClickListener(clickListener);

		headerView.getPhoto().setOnClickListener(clickListener);
		headerView.getAddfriend().setEnabled(true);
		headerView.getAddfriend().setOnClickListener(clickListener);
	}

	private final View.OnClickListener clickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
				case R.id.positive:
					String input = dialogInput.getEditView().getText().toString();
					if (TextUtils.isEmpty(input)) {
						Toast.makeText(UserInfoActivity.this, "请输入留言", Toast.LENGTH_SHORT).show();
						return;
					}
					
					synchronized (dialogInput) {
						if (dialogInput != null) {
							Long time = App.getServerTime();
							//发送好友请求消息
							if (!MessageConnection.instance.isConnected()) {
								Toast.makeText(UserInfoActivity.this, R.string.err_socket, Toast.LENGTH_SHORT).show();
								dialogInput.dismiss();
								dialogInput = null;
								return;
							}
							
							handler.sendEmptyMessage(HANDLER_ADD_FRIEDN_FLAG);
							
							SendMessage mBean = new SendMessage(App.PREFS.getUid(), App.USER.getNickname(), App.USER.getPhotoSourceUrl(), uid, new Date(time), ActionType.REQUEST_FRIEND, Type.TEXT, input);
							MessageConnection.instance.send(mBean.toMessage());

							// 将好友申请消息保存到缓存里

							MessageService.CACHE_MESSAGE.put(time, null);

							dialogInput.dismiss();
							dialogInput = null;
						}
					}
					break;

				case R.id.negative:
					synchronized (dialogInput) {
						if (dialogInput != null) {
							dialogInput.dismiss();
							dialogInput = null;
						}
					}
					break;

				case R.id.left:
					finish();
					break;

				case R.id.right:
					//TODE 跳转要带的参数
					if (TextUtils.isEmpty(App.PREFS.getUserProfile())) {
						//TODO 跳转到登录界面
						loginConfrim.openDialog();
						break;
					}
					
					if (info == null) {
						return;
					}
					ChatMessage msg = new ChatMessage();
					msg.setTargetId(info.getUid());
					msg.setTargetNickname(info.getNickname());
					msg.setTargetPhotoUrl(info.getPhotoSourceUrl());
					msg.setActionType(ActionType.CHAT);
					msg.setType(Type.OTHER);
					msg.setSendType(SendType.READ);
					
					Intent intent = new Intent(UserInfoActivity.this, ChatActivity.class);
					intent.putExtra(ChatActivity.REQUEST_MESSAGE, msg);
					startActivity(intent);
					break;

				case R.id.btn_userinfotitle_addfriend:
					if (TextUtils.isEmpty(App.PREFS.getUserProfile())) {
						//TODO 跳转到登录界面
						loginConfrim.openDialog();
						break;
					}
					
					if (info == null) {
						return;
					}
					
					if (dialogInput == null) {
						dialogInput = new SingleEditorDialog(UserInfoActivity.this);
						dialogInput.getTitleView().setText("好友申请");
						dialogInput.getEditView().setHint("给ta留个言吧~");
						dialogInput.setOnClickListener(this);
					}
					dialogInput.show();
					break;
				case R.id.iv_userinfotitle_photo:// 头像的监听
					if (info == null) {
						return;
					}

					final ArrayList<ImageData> images = new ArrayList<ImageData>();
					ImageData image = new ImageData(info.getPhotoSourceUrl());
					images.add(image);

					Intent intent1 = new Intent(v.getContext(), ImagePreviewActivity.class);

					Bundle params = new Bundle();
					params.putSerializable(ImagePagerFragment.PARAMS_KEY_DATA, images);
					intent1.putExtra(ImagePreviewActivity.INTENT_KEY_DATA, params);
					v.getContext().startActivity(intent1);
					break;
			}
		}
	};

	/**
	 * 获取个人信息
	 * 
	 * @param pager
	 */
	public void getUserinfo(int fuid, int pager) {
		if(first) {
			showProgressDialog();
			first = false;
		}
		ArrayList<NameValuePair> list = new ArrayList<NameValuePair>();
		list.add(new BasicNameValuePair("uid", String.valueOf(fuid)));
		list.add(new BasicNameValuePair("friend_id", String.valueOf(App.PREFS.getUid())));
		list.add(new BasicNameValuePair("page", String.valueOf(pager)));

		req = new HttpRequest(Constants.OUMENCIRCLE_GETUSERINFO, list, HttpRequest.Method.GET, getUserMessageCallback);
		App.THREAD.execute(req);
	}

	final DefaultHttpCallback getUserMessageCallback = new DefaultHttpCallback(new EventListener() {

		@Override
		public void onSuccess(HttpResult result) {
			try {
				req = null;
				String response = result.getResult();
				ELog.i(response);

				JSONObject obj = new JSONObject(response);
				info = new UserInfo(obj);
				handler.sendEmptyMessage(HANLDER_GETUSERMESSAGE_SUCCESS);
			}
			catch (Exception e) {
				ELog.i("Exception e=" + e.getMessage());
				handler.sendMessage(handler.obtainMessage(HANLDER_GETUSERMESSAGE_FAIL, "获取用户信息失败"));
				e.printStackTrace();
			}

		}

		@Override
		public void onForceClose(ExceptionHttpResult result) {
			handler.sendMessage(handler.obtainMessage(HANLDER_GETUSERMESSAGE_FAIL, "获取用户信息失败"));
			req = null;
		}

		@Override
		public void onException(ExceptionHttpResult result) {
			req = null;
			handler.sendMessage(handler.obtainMessage(HANLDER_GETUSERMESSAGE_FAIL, "获取用户信息失败"));
		}
	});

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
			case HANLDER_GETUSERMESSAGE_SUCCESS:
				dismissProgressDialog();
				if (footerView != null) {
					footerView.setState(HSZListViewAdapter.STATE_NORMAL);
				}
				//更新头部信息
				headerView.update(info);
				
				if (uid != App.PREFS.getUid()) {
//					headerView.getAddfriend().setVisibility(View.VISIBLE);
					btnRight.setVisibility(View.VISIBLE);
				}
				else {
					btnRight.setVisibility(View.GONE);
					headerView.getAddfriend().setVisibility(View.GONE);
				}
				// 个人信息总页数
				totalPage = info.getPagetotal();

				synchronized (adapter) {
					if (page == 1) {
						adapter.clear();
					}
					if (info.getUserinfos() != null) {
						adapter.addAll(info.getUserinfos());
						adapter.notifyDataSetChanged();
					}
				}

				handler.postDelayed(new Runnable() {

					@Override
					public void run() {
						listView.loaded();
					}
				}, 500);
				break;

			case HANLDER_GETUSERMESSAGE_FAIL:
				dismissProgressDialog();
				if (footerView != null) {
					footerView.setState(HSZListViewAdapter.STATE_NORMAL);
				}
				listView.loaded();
				Toast.makeText(mBaseApplication, (String) msg.obj, Toast.LENGTH_SHORT).show();
				break;
			case HANDLER_ADD_FRIEDN_FLAG:// 隐藏添加好友按钮
				headerView.getAddfriend().setVisibility(View.GONE);
				break;
		}
		return false;
	}

	@Override
	public void onBackPressed() {
		if (req != null)
			req.close();
		super.onBackPressed();
	}

	/**
	 * listview的适配器
	 * 
	 */
	private class UserInfoAdapter extends HSZListViewAdapter<CircleUserBasicMsg> {

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Item item;
			if (convertView == null) {
				item = new Item(parent.getContext());
			}
			else {
				item = (Item) convertView;
			}
			item.update(get(position));
			return item;
		}

		@Override
		public void onHeaderPull(View headerView, int top) {
			super.onHeaderPull(headerView, top);
		}

		@Override
		public void onHeaderPullOverLine(View headerView, int top) {
			super.onHeaderPullOverLine(headerView, top);
		}

		@Override
		public void onHeaderLoad() {
			page = 1;
			getUserinfo(uid, page);
		}

		@Override
		public void onFooterLoad() {
			footerView.setState(HSZListViewAdapter.STATE_LOADING);
			page = page + 1;
			if (page <= totalPage) {
				getUserinfo(uid, page);
			}
			else {
				if (footerView != null) {
					footerView.setState(HSZListViewAdapter.STATE_NORMAL);
				}
				listView.loaded();
				Toast.makeText(mBaseApplication, "所有信息已加载", Toast.LENGTH_SHORT).show();
			}
		}
	}
}
