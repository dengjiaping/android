package com.oumen.friend;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.oumen.R;
import com.oumen.TitleBar;
import com.oumen.android.App;
import com.oumen.android.BaseActivity;
import com.oumen.android.UserProfile;
import com.oumen.android.util.Constants;
import com.oumen.http.DefaultHttpCallback;
import com.oumen.http.DefaultHttpCallback.EventListener;
import com.oumen.http.ExceptionHttpResult;
import com.oumen.http.HttpRequest;
import com.oumen.http.HttpResult;
import com.oumen.tools.ELog;
import com.oumen.user.UserInfoActivity;

public class AddFriendActivity extends BaseActivity {
	final int QUERY_ISFRIEND_SUCCESS = 1;
	final int QUERY_NOTFRIEND_SUCCESS = 2;
	final int QUERY_FRIEND_FAIL = 3;
	private TitleBar titleBar;
	private Button ivBack, ivSubmit;
	private EditText etContent;
	private TextView tvInvite;

	private FindPerson p;
	
	private InputMethodManager inputManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.add_friend);
		inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		
		init();
		p = new FindPerson();
	}

	private void init() {
		titleBar = (TitleBar) findViewById(R.id.titlebar);
		titleBar.getTitle().setText("添加好友");
		ivBack = titleBar.getLeftButton();
		ivSubmit = titleBar.getRightButton();
		ivSubmit.setText("查找");
		etContent = (EditText) findViewById(R.id.et_addfriend);
		tvInvite = (TextView) findViewById(R.id.tv_addfriend_invite);
		
		ivBack.setOnClickListener(clicklistener);
		ivSubmit.setOnClickListener(clicklistener);
		tvInvite.setOnClickListener(clicklistener);
	}

	private OnClickListener clicklistener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if (inputManager.isActive()) {
				inputManager.hideSoftInputFromWindow(etContent.getWindowToken(), 0);
			}
			
			if (v == ivBack) {
				finish();
			} 
			else if (v == ivSubmit) {
				if (TextUtils.isEmpty(etContent.getText().toString().trim())) {
					Toast.makeText(AddFriendActivity.this, "请输入Email或者偶们ID", Toast.LENGTH_SHORT).show();
					return;
				}
				if(!App.NetworkType.NONE.equals(App.getNetworkType())){
					// 获取
					showProgressDialog("正在查询，请稍后...");
					searchFriend();
				}else{
					Toast.makeText(AddFriendActivity.this, "亲，您的网络没有打开~", Toast.LENGTH_SHORT).show();
				}
			}
			else if (v == tvInvite) {
				Intent i = new Intent(mBaseApplication, InviteFriendActivity.class);
				startActivityForResult(i, 0);
			}
		}
	};

	private void searchFriend() {
		String content = etContent.getText().toString().trim();
		UserProfile profile = App.USER;
		if (content.equals(profile.getOmNumber()) || content.equals(profile.getUid())) {
			Toast.makeText(mBaseApplication, "请输入好友的信息", Toast.LENGTH_SHORT).show();
			return;
		}
		ArrayList<NameValuePair> list = new ArrayList<NameValuePair>();
		list.add(new BasicNameValuePair("friend_m", content));
		list.add(new BasicNameValuePair("user_id", String.valueOf(App.PREFS.getUid())));

		HttpRequest req = new HttpRequest(Constants.CHATFRIENT_ADDFRIEND, list, HttpRequest.Method.GET, findPersonCallback);
		App.THREAD.execute(req);
	}

	final DefaultHttpCallback findPersonCallback = new DefaultHttpCallback(new EventListener() {

		@Override
		public void onSuccess(HttpResult result) {
			try {
				String response = result.getResult();
				JSONObject obj = new JSONObject(response);
				if (response.contains("friend")) {
					p.setIsFriend(obj.getInt("friend"));
					Friend f = new Friend();

					f.setUid(Integer.parseInt(obj.getString("user_id")));
					f.setNickname(obj.getString("username"));
					f.setBabyType(TextUtils.isEmpty(obj.getString("babytype")) ? 2 : Integer.parseInt(obj.getString("babytype")));
					f.setPhotoUrl(obj.getString("head_photo"));
					f.setDescription(obj.getString("manifesto"));

					p.setFriend(f);

					if (p.getIsFriend() == 1) {
						handler.sendMessage(handler.obtainMessage(QUERY_ISFRIEND_SUCCESS, p.getFriend().getNickname()));
					}
					else if (p.getIsFriend() == 0) {
						handler.sendEmptyMessage(QUERY_NOTFRIEND_SUCCESS);
					}

				}
				else if (response.contains("tip")) {
					handler.sendMessage(handler.obtainMessage(QUERY_FRIEND_FAIL, obj.getString("tip")));
				}
			}
			catch (Exception e) {
				ELog.e("Exception e=" + e.getMessage());
				handler.sendMessage(handler.obtainMessage(QUERY_FRIEND_FAIL, "查找好友失败，请重试"));
				e.printStackTrace();
			}
		}

		@Override
		public void onForceClose(ExceptionHttpResult result) {
			handler.sendMessage(handler.obtainMessage(QUERY_FRIEND_FAIL, "查找好友失败，请重试"));
		}

		@Override
		public void onException(ExceptionHttpResult result) {
			handler.sendMessage(handler.obtainMessage(QUERY_FRIEND_FAIL, "查找好友失败，请重试"));
		}
	});

	@Override
	public boolean handleMessage(Message msg) {
		dismissProgressDialog();
		switch (msg.what) {
			case QUERY_ISFRIEND_SUCCESS:
				Toast.makeText(mBaseApplication, "您已经和" + (String) msg.obj + "是好友了", Toast.LENGTH_SHORT).show();
				break;
			case QUERY_NOTFRIEND_SUCCESS:
				ELog.i("查询到的好友信息：" + p.toString());
				Intent intent = new Intent(mBaseApplication, UserInfoActivity.class);
				intent.putExtra(UserInfoActivity.INTENT_KEY_UID, p.getFriend().uid);
				intent.putExtra(UserInfoActivity.INTENT_KEY_GENDER, p.getFriend().gender);
				startActivity(intent);
				finish();
				break;
			case QUERY_FRIEND_FAIL:
				Toast.makeText(mBaseApplication, (String) msg.obj, Toast.LENGTH_SHORT).show();
				break;
		}
		return super.handleMessage(msg);
	}
}
