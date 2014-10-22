package com.oumen.friend;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.oumen.R;
import com.oumen.TitleBar;
import com.oumen.android.App;
import com.oumen.android.BaseActivity;
import com.oumen.android.util.Constants;
import com.oumen.chat.ChatActivity;
import com.oumen.db.PinYin;
import com.oumen.http.DefaultHttpCallback;
import com.oumen.http.ExceptionHttpResult;
import com.oumen.http.HttpRequest;
import com.oumen.http.HttpResult;
import com.oumen.message.ActionType;
import com.oumen.message.ChatMessage;
import com.oumen.message.MessageService;
import com.oumen.message.SendType;
import com.oumen.message.Type;
import com.oumen.tools.ELog;
import com.oumen.widget.editview.ClearEditText;
import com.oumen.widget.sortview.PinyinComparator;
import com.oumen.widget.sortview.SideBar;
import com.oumen.widget.sortview.SideBar.OnTouchingLetterChangedListener;
import com.oumen.widget.sortview.SortDataItem;

public class FriendsActivity extends BaseActivity {
	public static final String ACTION_UPDATE = "com.oumen.friend.CopyOfChatFriendActivity";
	private final int ADDFRIEND_REQUEST_CODE = 1;

	private final int HANDLER_GET_LIST = 1;

	private TitleBar titleBar;
	private Button btnLeft;
	private ClearEditText edtSearch;
	private ListView lstView;
	private SideBar sideBar;
	private TextView txtLetterTip;
	private View containerAddFriend;
	
	private final LinkedList<SortDataItem<Friend>> allFriends = new LinkedList<SortDataItem<Friend>>();

	private final PinyinComparator comparator = new PinyinComparator();
	private final ContactAdapter adapter = new ContactAdapter();

	private final IntentFilter receiverFilter = new IntentFilter(MessageService.RESPONSE_ACTION);

	private final BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			int type = intent.getIntExtra(MessageService.INTENT_KEY_TYPE, 0);
			if (type == MessageService.TYPE_FRIEND_MESSAGE || type == MessageService.TYPE_UPDATE_FRIEND_LIST) {
				update();
			}
		}
	};

	@Override
	public void onStart() {
		super.onStart();
		registerReceiver(receiver, receiverFilter);
	}

	@Override
	public void onStop() {
		super.onStop();
		unregisterReceiver(receiver);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chatfriends);
		
		titleBar = (TitleBar) findViewById(R.id.titlebar);
		titleBar.getTitle().setText("好友列表");
		titleBar.getRightButton().setVisibility(View.GONE);

		btnLeft = titleBar.getLeftButton();
		btnLeft.setOnClickListener(clickListener);

		containerAddFriend = findViewById(R.id.add_friend_container);
		containerAddFriend.setOnClickListener(clickListener);

		lstView = (ListView) findViewById(R.id.lst);
		lstView.setOnItemClickListener(new AbsListView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Friend friend = adapter.data.get(position).getObject();
				ChatMessage msg = new ChatMessage();
				msg.setTargetId(friend.getUid());
				msg.setTargetNickname(friend.getNickname());
				msg.setTargetPhotoUrl(friend.getPhotoSourceUrl());
				msg.setActionType(ActionType.CHAT);
				msg.setType(Type.OTHER);
				msg.setSendType(SendType.READ);
				
				Intent intent = new Intent(FriendsActivity.this, ChatActivity.class);
				intent.putExtra(ChatActivity.REQUEST_MESSAGE, msg);
				startActivity(intent);
			}
		});

		txtLetterTip = (TextView) findViewById(R.id.tip);

		sideBar = (SideBar) findViewById(R.id.sidebar);
		sideBar.setTextView(txtLetterTip);
		// 设置右侧触摸监听
		sideBar.setOnTouchingLetterChangedListener(new OnTouchingLetterChangedListener() {

			@Override
			public void onTouchingLetterChanged(String s) {
				// 该字母首次出现的位置
				int position = adapter.getPositionForSection(s.charAt(0));
				if (position != -1) {
					lstView.setSelection(position);
				}
			}
		});

		edtSearch = (ClearEditText) findViewById(R.id.search);
		edtSearch.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// 当输入框里面的值为空，更新为原来的列表，否则为过滤数据列表
				filter(s.toString());
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});

		lstView.setAdapter(adapter);
		update();
	}

	private void filter(String filterStr) {
		if (TextUtils.isEmpty(filterStr)) {
			synchronized (adapter.data) {
				adapter.data.clear();
				adapter.data.addAll(allFriends);
			}
		}
		else {
			String pyKey = PinYin.getPinYin(filterStr, App.DB).toUpperCase(App.LOCALE);
			ELog.i("Key pinyin:" + pyKey);

			LinkedList<SortDataItem<Friend>> results = new LinkedList<SortDataItem<Friend>>();
			for (SortDataItem<Friend> i : allFriends) {
				String py = PinYin.getPinYin(i.getObject().getNickname(), App.DB).toUpperCase(App.LOCALE);
				if (py.contains(pyKey)) {
					results.add(i);
				}
			}

			synchronized (adapter.data) {
				adapter.data.clear();
				adapter.data.addAll(results);
			}
		}
		adapter.notifyDataSetChanged();
	}

	private final Runnable updateTask = new Runnable() {

		@Override
		public void run() {
			List<Friend> friends = Friend.query(App.PREFS.getUid(), App.DB);
			final int size = friends.size();
			if (size == 0) {
				// 进行联网去好友列表
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
									Friend friend =new Friend(obj);
									
									if (friend.getUid() != 0){
										friends.add(friend);
									}
								}
								catch (JSONException e) {
									ELog.e("Exception:" + e.getMessage());
									e.printStackTrace();
								}
							}
							// TODO 此处还需要处理，每次取到信息好友信息就把原来的好友都删除了，然后再把新的数据插入到数据库
							Friend.deleteAll(selfUid, App.DB);
							Friend.insert(friends, selfUid, App.DB);

							LinkedList<SortDataItem<Friend>> tmp = new LinkedList<SortDataItem<Friend>>();
							for (int i = 0; i < friends.size(); i++) {
								Friend friend = friends.get(i);
								ELog.i("Friend:" + friend.getNickname());
								if (TextUtils.isEmpty(friend.getNickname()))
									continue;

								String py = PinYin.getPinYin(friend.getNickname(), App.DB).toUpperCase(App.LOCALE);
								tmp.add(new SortDataItem<Friend>(friend, py));
							}
							Collections.sort(tmp, comparator);
							allFriends.clear();
							allFriends.addAll(tmp);

							synchronized (adapter.data) {
								adapter.data.clear();
								adapter.data.addAll(tmp);
							}
							handler.sendEmptyMessage(HANDLER_GET_LIST);
							
						}
						catch (Exception e) {
							ELog.e("Exception:" + e.getMessage());
							handler.sendEmptyMessage(HANDLER_GET_LIST);
							e.printStackTrace();
						}
					}

					@Override
					public void onForceClose(ExceptionHttpResult result) {
					}

					@Override
					public void onException(ExceptionHttpResult result) {
						handler.sendEmptyMessage(HANDLER_GET_LIST);
						ELog.e("Exception:" + result.getException().getMessage());
					}
				}));
				App.THREAD.execute(req);
			}
			else {
				LinkedList<SortDataItem<Friend>> tmp = new LinkedList<SortDataItem<Friend>>();
				for (int i = 0; i < size; i++) {
					Friend friend = friends.get(i);
					ELog.i("Friend:" + friend.getNickname());
					if (TextUtils.isEmpty(friend.getNickname()))
						continue;

					String py = PinYin.getPinYin(friend.getNickname(), App.DB).toUpperCase(App.LOCALE);
					tmp.add(new SortDataItem<Friend>(friend, py));
				}
				Collections.sort(tmp, comparator);
				allFriends.clear();
				allFriends.addAll(tmp);

				synchronized (adapter.data) {
					adapter.data.clear();
					adapter.data.addAll(tmp);
				}
				handler.sendEmptyMessage(HANDLER_GET_LIST);
			}

		}
	};

	private final View.OnClickListener clickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			if (v == btnLeft) {
				finish();
			}
			else if (v == containerAddFriend) {
				Intent intent = new Intent(FriendsActivity.this, AddFriendActivity.class);
				startActivityForResult(intent, ADDFRIEND_REQUEST_CODE);
			}
		}
	};

	private void update() {
		ELog.i("正在更新好友列表");
		App.THREAD.execute(updateTask);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	/**
	 * 联系人适配器
	 * 
	 * @author oumen
	 * 
	 */
	class ContactAdapter extends BaseAdapter implements SectionIndexer {
		final ArrayList<SortDataItem<Friend>> data = new ArrayList<SortDataItem<Friend>>();

		@Override
		public int getCount() {
			return data.size();
		}

		@Override
		public Object getItem(int position) {
			return data.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Item item = convertView == null ? new Item(parent.getContext()) : (Item) convertView;
			item.update(position);
			return item;
		}

		@Override
		public int getPositionForSection(int section) {
			for (int i = 0; i < getCount(); i++) {
				char firstChar = data.get(i).getPinyin().charAt(0);
				if (firstChar == section) {
					return i;
				}
			}

			return -1;
		}

		@Override
		public int getSectionForPosition(int position) {
			return data.get(position).getFirst();
		}

		@Override
		public Object[] getSections() {
			return null;
		}
	}

	class Item extends RelativeLayout {
		TextView txtCatalog;
		ImageView imgPhoto;
		TextView txtName;
		TextView txtDescription;

		int position;

		Item(Context context) {
			super(context, null, 0);
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			inflater.inflate(R.layout.chatfriend_item, this, true);

			txtCatalog = (TextView) findViewById(R.id.catalog);
			imgPhoto = (ImageView) findViewById(R.id.photo);
			txtName = (TextView) findViewById(R.id.name);
			txtDescription = (TextView) findViewById(R.id.description);
		}

		void update(int position) {
			this.position = position;
			SortDataItem<Friend> itemData = adapter.data.get(position);
			String nick = itemData.getObject().getNickname();

			// 根据position获取分类的首字母的Char ascii值
			int section = adapter.getSectionForPosition(position);

			// 如果当前位置等于该分类首字母的Char的位置 ，则认为是第一次出现
			if (position == adapter.getPositionForSection(section)) {
				txtCatalog.setVisibility(View.VISIBLE);
				txtCatalog.setText(String.valueOf(itemData.getFirst()));
			}
			else {
				txtCatalog.setVisibility(View.GONE);
			}

			//TODO 偶们头像用本地的（字体用18）
			txtName.setText(nick);
			if(itemData.getObject().getUid() == ChatMessage.OUMEN_TEAM_ID){
				imgPhoto.setImageResource(R.drawable.avatar_omen);
			}else {
				ImageLoader.getInstance().displayImage(itemData.getObject().getPhotoUrl(App.MIDDLE_PHOTO_SIZE), imgPhoto, App.OPTIONS_HEAD_ROUND, App.CIRCLE_IMAGE_LOADING_LISTENER);
			}
			txtDescription.setText(itemData.getObject().getDescription());
		}
	}

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
			case HANDLER_GET_LIST:
				adapter.notifyDataSetChanged();
				break;
		}
		return false;
	}
}