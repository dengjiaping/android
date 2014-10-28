package com.oumen.activity.user;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.oumen.R;
import com.oumen.TitleBar;
import com.oumen.activity.detail.HuoDongDetailActivity;
import com.oumen.activity.message.UserActivityMessage;
import com.oumen.android.App;
import com.oumen.android.BaseActivity;
import com.oumen.android.util.Constants;
import com.oumen.http.DefaultHttpCallback;
import com.oumen.http.ExceptionHttpResult;
import com.oumen.http.HttpRequest;
import com.oumen.http.HttpResult;
import com.oumen.tools.ELog;
import com.oumen.widget.refushlist.AbOnListViewListener;
import com.oumen.widget.refushlist.AbPullListView;

/**
 * 用户自己的活动界面
 *
 */
public class UserActivity extends BaseActivity {
	private final String CACHE_GOING_ACTIVITY = "going_user_activity_";
	private final String CACHE_FINISH_ACTIVITY = "finish_user_activity_";

	private final int HANDLER_REQUEST_LIST = 1;
	private final int HANDLER_REQUEST_FINISH_LIST = 2;
	//标题行控件
	private TitleBar titleBar;
	private Button btnLeft;

	private Button going, finish;

	private AbPullListView lstView;
	private AbPullListView lstView1;

	private TextView emptyView;

	private final ActivityAdapter adapter = new ActivityAdapter();
	private final ActivityAdapter adapter1 = new ActivityAdapter();

	private int isover = 0; // ==0 ，获取正在进行的活动； == 1,已经结束的活动
	private int goingPage = 1;
	private int finishPage = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_huodong);
		init();
		initGoingList();
	}

	private void initGoingList() {
		if (TextUtils.isEmpty(App.CACHE.read(getCacheKey(CACHE_GOING_ACTIVITY)))) {
			getUserHuodong(goingPage);
		}
		else {
			try {
				JSONArray array = new JSONArray(App.CACHE.read(getCacheKey(CACHE_GOING_ACTIVITY)));
				List<UserActivityMessage> results = parseJson(array);
				handler.sendMessage(handler.obtainMessage(HANDLER_REQUEST_LIST, 1, 0, results));
			}
			catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	private String getCacheKey(String tip) {
		return tip + App.USER.getUid();
	}

	private void initFinishList() {
		if (TextUtils.isEmpty(App.CACHE.read(getCacheKey(CACHE_FINISH_ACTIVITY)))) {
			getUserHuodong(goingPage);
		}
		else {
			try {
				JSONArray array = new JSONArray(App.CACHE.read(getCacheKey(CACHE_FINISH_ACTIVITY)));
				List<UserActivityMessage> results = parseJson(array);
				handler.sendMessage(handler.obtainMessage(HANDLER_REQUEST_LIST, 1, 0, results));
			}
			catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	private void init() {
		titleBar = (TitleBar) findViewById(R.id.titlebar);
		titleBar.getTitle().setText("我的活动");
		titleBar.getRightButton().setVisibility(View.GONE);
		btnLeft = titleBar.getLeftButton();

		going = (Button) findViewById(R.id.ongoing);
		finish = (Button) findViewById(R.id.onfinish);

		TextView emptyView = (TextView) findViewById(R.id.empty_view);
		emptyView.setText(getResources().getString(R.string.activity_user_empty));

		lstView = (AbPullListView) findViewById(R.id.refreshablelistview);
		lstView.setBackgroundColor(getResources().getColor(R.color.white));
		lstView.setDivider(new ColorDrawable(getResources().getColor(R.color.user_center_click_bg)));
		lstView.setDividerHeight(1);
		lstView.setEmptyView(emptyView);
		
		lstView.getHeaderView().setHeaderProgressBarDrawable(this.getResources().getDrawable(R.drawable.progress_circular));
		lstView.getFooterView().setFooterProgressBarDrawable(this.getResources().getDrawable(R.drawable.progress_circular));
		lstView.setAdapter(adapter);
		lstView.setSelector(android.R.color.transparent);
		lstView.setAbOnListViewListener(listviewListener);

		lstView1 = (AbPullListView) findViewById(R.id.refreshablelistview1);
		lstView1.setBackgroundColor(getResources().getColor(R.color.white));
		lstView1.setDivider(new ColorDrawable(getResources().getColor(R.color.user_center_click_bg)));
		lstView1.setDividerHeight(1);
		lstView1.setEmptyView(emptyView);
		lstView1.getHeaderView().setHeaderProgressBarDrawable(this.getResources().getDrawable(R.drawable.progress_circular));
		lstView1.getFooterView().setFooterProgressBarDrawable(this.getResources().getDrawable(R.drawable.progress_circular));
		lstView1.setAdapter(adapter1);
		lstView1.setSelector(android.R.color.transparent);
		lstView1.setVisibility(View.GONE);
		lstView1.setAbOnListViewListener(listviewListener1);

		btnLeft.setOnClickListener(clickListener);
		going.setOnClickListener(clickListener);
		finish.setOnClickListener(clickListener);
	}

	private AbOnListViewListener listviewListener = new AbOnListViewListener() {

		@Override
		public void onRefresh() {
			goingPage = 1;
			getUserHuodong(goingPage);
		}

		@Override
		public void onLoadMore() {
			int page = adapter.isEmpty() ? 1 : goingPage + 1;
			getUserHuodong(page);
		}

	};

	private AbOnListViewListener listviewListener1 = new AbOnListViewListener() {

		@Override
		public void onRefresh() {
			finishPage = 1;
			getUserHuodong(finishPage);
		}

		@Override
		public void onLoadMore() {
			int page = adapter.isEmpty() ? 1 : finishPage + 1;
			getUserHuodong(page);
		}
	};

	private final OnClickListener clickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			if (v == btnLeft) {
				setResult(Activity.RESULT_OK);
				finish();
			}
			else if (v == going) {
				if (isover == 0) {
					return;
				}
				isover = 0;
				going.setTextColor(getResources().getColor(R.color.default_bg));
				going.setBackgroundResource(R.drawable.user_huodong_ongoing_left);

				finish.setTextColor(getResources().getColor(R.color.white));
				finish.setBackgroundResource(R.drawable.user_huodong_ongoing_right);

				initGoingList();
			}
			else if (v == finish) {
				if (isover == 1) {
					return;
				}
				isover = 1;
				going.setTextColor(getResources().getColor(R.color.white));
				going.setBackgroundResource(R.drawable.user_huodong_ongoing_left1);

				finish.setTextColor(getResources().getColor(R.color.default_bg));
				finish.setBackgroundResource(R.drawable.user_huodong_ongoing_right1);

				initFinishList();
			}
			else if (v instanceof UserHuodongItem) {// list的item
				ELog.i("点击了");
				UserActivityMessage item = (UserActivityMessage) v.getTag();
				Intent intent = new Intent(UserActivity.this, HuoDongDetailActivity.class);
				intent.putExtra(HuoDongDetailActivity.INTENT_KEY_ACTIVITY_ID, item.getActivityId());
				startActivityForResult(intent, 1);
			}
		}
	};

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			getUserHuodong(goingPage);
		}
	};

	private class ActivityAdapter extends BaseAdapter {
		private List<UserActivityMessage> data = new ArrayList<UserActivityMessage>();

		@Override
		public int getCount() {
			return data.size();
		}

		@Override
		public UserActivityMessage getItem(int position) {
			return data.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			UserHuodongItem item = null;
			if (convertView == null) {
				item = new UserHuodongItem(UserActivity.this);
				item.setOnClickListener(clickListener);
			}
			else {
				item = (UserHuodongItem) convertView;
			}
			UserActivityMessage itemData = null;
			synchronized (this) {
				itemData = data.get(position);
			}
			item.setTag(itemData);
			item.update(itemData);
			return item;
		}
	}

	private List<UserActivityMessage> parseJson(JSONArray array) throws JSONException {
		List<UserActivityMessage> results = new LinkedList<UserActivityMessage>();

		for (int i = 0; i < array.length(); i++) {
			JSONObject itemJson = array.getJSONObject(i);
			results.add(new UserActivityMessage(itemJson));
		}

		// 缓存
		if (adapter.isEmpty() && !results.isEmpty()) {
			if (isover == 0) {
				App.CACHE.save(getCacheKey(CACHE_GOING_ACTIVITY), array.toString());
			}
			else {
				App.CACHE.save(getCacheKey(CACHE_FINISH_ACTIVITY), array.toString());
			}
		}
		return results;
	}

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
			case HANDLER_REQUEST_LIST:
				synchronized (adapter) {
					if (msg.obj instanceof List<?>) {
						List<UserActivityMessage> results = (List<UserActivityMessage>) msg.obj;
						if (msg.arg1 == 1) {
							adapter.data.clear();
						}
						adapter.data.addAll(results);

						if (!results.isEmpty()) {
							goingPage = msg.arg1;
						}
						adapter.notifyDataSetChanged();
					}
					else if (msg.obj instanceof CharSequence) {
						Toast.makeText(lstView.getContext(), (CharSequence) msg.obj, Toast.LENGTH_SHORT).show();
					}
					adapter.notifyDataSetChanged();
				}
				lstView.stopRefresh();
				lstView.stopLoadMore();
				break;
			case HANDLER_REQUEST_FINISH_LIST:
				synchronized (adapter1) {
					if (msg.obj instanceof List<?>) {
						List<UserActivityMessage> results = (List<UserActivityMessage>) msg.obj;
						if (msg.arg1 == 1) {
							adapter1.data.clear();
						}
						adapter1.data.addAll(results);

						if (!results.isEmpty()) {
							finishPage = msg.arg1;
						}
						adapter1.notifyDataSetChanged();
					}
					else if (msg.obj instanceof CharSequence) {
						Toast.makeText(lstView1.getContext(), (CharSequence) msg.obj, Toast.LENGTH_SHORT).show();
					}
					adapter1.notifyDataSetChanged();
				}
				lstView1.stopRefresh();
				lstView1.stopLoadMore();
				break;
		}
		return super.handleMessage(msg);
	}

	/**
	 * 获取用户参与的活动列表
	 * 
	 * @param page
	 */
	private void getUserHuodong(final int page) {
		DefaultHttpCallback callback = new DefaultHttpCallback(new DefaultHttpCallback.EventListener() {

			@Override
			public void onSuccess(HttpResult result) {
				try {
					String str = result.getResult();
					ELog.i(str);

					List<UserActivityMessage> lists = new LinkedList<UserActivityMessage>();
					JSONArray array = new JSONArray(str);
					if (array.length() == 0) {
						handler.sendMessage(handler.obtainMessage(HANDLER_REQUEST_LIST, "没有活动了"));
						return;
					}
					lists = parseJson(array);

					handler.sendMessage(handler.obtainMessage(HANDLER_REQUEST_LIST, page, 0, lists));
				}
				catch (Exception e) {
					ELog.e("Exception:" + e.getMessage());
					handler.sendMessage(handler.obtainMessage(HANDLER_REQUEST_LIST, "获取活动失败"));
					e.printStackTrace();
				}
			}

			@Override
			public void onForceClose(ExceptionHttpResult result) {
				handler.sendEmptyMessage(HANDLER_REQUEST_LIST);
			}

			@Override
			public void onException(ExceptionHttpResult result) {
				handler.sendMessage(handler.obtainMessage(HANDLER_REQUEST_LIST, "获取活动失败"));
			}
		});

		//http://www.oumen.com/huodong/act/myActivity?uid=243&isover=1&page=1&pagenum=10
		// isover = 1,已经结束; =0 正在进行
		ArrayList<NameValuePair> list = new ArrayList<NameValuePair>();
		list.add(new BasicNameValuePair("uid", String.valueOf(App.PREFS.getUid())));
		list.add(new BasicNameValuePair("isover", String.valueOf(isover)));
		list.add(new BasicNameValuePair("page", String.valueOf(page)));

		HttpRequest req = new HttpRequest(Constants.GET_USER_ACTIVITY_LIST, list, HttpRequest.Method.GET, callback);
		App.THREAD.execute(req);
	}

}
