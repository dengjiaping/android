package com.oumen.near;

import java.util.ArrayList;
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
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.Toast;

import com.oumen.R;
import com.oumen.TitleBar;
import com.oumen.activity.detail.HuoDongDetailActivity;
import com.oumen.activity.message.DetailActivityMessage;
import com.oumen.activity.widget.IndexViewPager;
import com.oumen.android.App;
import com.oumen.android.BaseFragment;
import com.oumen.android.util.Constants;
import com.oumen.http.DefaultHttpCallback;
import com.oumen.http.ExceptionHttpResult;
import com.oumen.http.HttpRequest;
import com.oumen.http.HttpResult;
import com.oumen.mv.MvComposeService;
import com.oumen.tools.ELog;
import com.oumen.user.UserInfoActivity;
import com.oumen.widget.list.HSZFooterView;
import com.oumen.widget.list.HSZHeaderView;
import com.oumen.widget.list.HSZListView;
import com.oumen.widget.list.HSZListViewAdapter;

public class NearFragment extends BaseFragment {
	private final String CACHE_KEY = "nears";
	
	private final String KEY_SAVE_NEARS = "nears";
	private final String KEY_SAVE_ACTIVITIES = "activities";

	private final int DATA_TYPE_NEAR = 0;
	private final int DATA_TYPE_ACTIVITY = 1;
	
	private final int HANDLER_REQUEST_DATA = 1;

	final int USERINFO_REQUEST_CODE = 1;
	final int ACTIVITY_REQUEST_CODE = 2;
	
	private final List<IndexViewPager.ItemData> activities = new LinkedList<IndexViewPager.ItemData>();
	private final List<NearBean> nears = new LinkedList<NearBean>();

	//标题行控件
	private TitleBar titlebar;
	private Button btnNavLeft;
	
	private ActivityPanel activityPanel;

	private HSZListView<NearBean> lstView;
	private HSZHeaderView header;
	private HSZFooterView footer;
	private NearAdapter adapter;

	private int page = 1;

	private int activityNum = 0;
	
	private HttpRequest req;

	private final IntentFilter LOCATION_UPDATE_FILTER = new IntentFilter(MvComposeService.MV_COMPOSE_SERVICE_NOTIFY_ACTION);

	private final BroadcastReceiver LOCATION_UPDATE_RECEIVER = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			page = 0;
			getContent();
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		adapter = new NearAdapter();
	}

	@SuppressWarnings("unchecked")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		getActivity().registerReceiver(LOCATION_UPDATE_RECEIVER, LOCATION_UPDATE_FILTER);

		View view = inflater.inflate(R.layout.fragment_near, container, false);

		titlebar = (TitleBar) view.findViewById(R.id.titlebar);
		titlebar.getTitle().setText(getResources().getString(R.string.module_title_near));
		titlebar.getRightButton().setVisibility(View.GONE);
		btnNavLeft = titlebar.getLeftButton();
		btnNavLeft.setOnClickListener(clickListener);
		
		activityPanel = new ActivityPanel(container.getContext());

		header = new HSZHeaderView(getActivity());
		footer = new HSZFooterView(getActivity());
		footer.setVisibility(View.GONE);

		lstView = (HSZListView<NearBean>) view.findViewById(R.id.refreshablelistview);
		lstView.setBackgroundColor(getResources().getColor(R.color.white));
		lstView.setDivider(new ColorDrawable(getResources().getColor(R.color.oumen_line)));
		lstView.setDividerHeight(1);
		lstView.addHeaderView(activityPanel);
		lstView.setHeaderView(header, getResources().getDimensionPixelSize(R.dimen.list_header_height_default));
		lstView.setFooterView(footer, getResources().getDimensionPixelSize(R.dimen.list_footer_height_default));
		lstView.setAdapter(adapter);
		lstView.setSelector(android.R.color.transparent);

		setActivityVisibility(View.GONE);
		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		if (savedInstanceState != null) {
			if (savedInstanceState.containsKey(KEY_SAVE_NEARS)) {
				ArrayList<NearBean> saves = savedInstanceState.getParcelableArrayList(KEY_SAVE_NEARS);
				synchronized (adapter) {
					adapter.clear();
					adapter.addAll(saves);
					adapter.notifyDataSetChanged();

					footer.setVisibility(adapter.isEmpty() ? View.GONE : View.VISIBLE);
				}
			}
			if (savedInstanceState.containsKey(KEY_SAVE_ACTIVITIES)) {
				ArrayList<IndexViewPager.ItemData> saves = savedInstanceState.getParcelableArrayList(KEY_SAVE_ACTIVITIES);
				activityPanel.clear();
				activityPanel.addAll(saves);
				activityPanel.notifyDataSetChanged();
				
				if (saves.isEmpty()) {
					setActivityVisibility(View.GONE);
				}
				else {
					setActivityVisibility(View.VISIBLE);
				}
			}
		}

		String cache = App.CACHE.read(CACHE_KEY);
		if (!TextUtils.isEmpty(cache))  {
			JSONArray array;
			try {
				array = new JSONArray(cache);
				json2list(array);
				
				handler.sendEmptyMessage(HANDLER_REQUEST_DATA);
			}
			catch (JSONException e) {
				ELog.e("Exception:" + e.getMessage());
				e.printStackTrace();
			}
		}

		if (App.latitude != 0 && App.longitude != 0) {
			lstView.headerLoad();
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		ArrayList<NearBean> saveNears = adapter.copyDataSource();
		ArrayList<IndexViewPager.ItemData> saveActivities = activityPanel.copyDataSource();
		if (!saveNears.isEmpty()) {
			outState.putParcelableArrayList(KEY_SAVE_NEARS, saveNears);
		}
		if (!saveActivities.isEmpty()) {
			outState.putParcelableArrayList(KEY_SAVE_ACTIVITIES, saveActivities);
		}
		super.onSaveInstanceState(outState);
	}

	private void setActivityVisibility(int visibility) {
		AbsListView.LayoutParams params = (AbsListView.LayoutParams) activityPanel.getLayoutParams();
		if (params == null) {
			params = new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		}
		
		if (visibility == View.VISIBLE) {
			params.height = LayoutParams.WRAP_CONTENT;
		}
		else {
			params.height = 1;
		}
		activityPanel.setLayoutParams(params);
	}
	
	@Override
	public void onStart() {
		ELog.i("onStart()");
		if (activityPanel != null) {
			activityPanel.startTimer();
		}
		super.onStart();
	}
	
	private final View.OnClickListener clickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			getActivity().finish();
		}
	};

	/**
	 * listview的item监听
	 */
	private final View.OnClickListener cellClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			Object tag = view.getTag();
			if (tag != null && tag instanceof NearBean) {
				ELog.i("进入个人中心");
				NearBean result = (NearBean) tag;
				Intent intent = new Intent(getActivity(), UserInfoActivity.class);
				intent.putExtra(UserInfoActivity.INTENT_KEY_UID, result.getUser_id());
				getActivity().startActivityForResult(intent, USERINFO_REQUEST_CODE);
			}
			else if (tag != null && tag instanceof DetailActivityMessage) {
				ELog.i("进入活动");
				DetailActivityMessage result = (DetailActivityMessage) tag;
				Intent intent = new Intent(getActivity(), HuoDongDetailActivity.class);
				intent.putExtra(HuoDongDetailActivity.INTENT_KEY_ACTIVITY_ID, result.getId());
				getActivity().startActivityForResult(intent, ACTIVITY_REQUEST_CODE);
			}
		}
	};

	@Override
	public void onDestroyView() {
		if (req != null) {
			req.close();
		}
		getActivity().unregisterReceiver(LOCATION_UPDATE_RECEIVER);
		activityPanel.stopTimer();
		super.onDestroyView();
	}
	
	@Override
	public void onDestroy() {
		activityPanel.stopTimer();
		super.onDestroy();
	}
	
	@Override
	public void onStop() {
		activityPanel.stopTimer();
		super.onStop();
	}

	@Override
	public boolean onBackPressed() {
		if (req != null) {
			req.close();
		}
		return false;
	}
	
	private void json2list(JSONArray array) throws JSONException {
		for (int i = 0; i < array.length(); i++) {
			JSONObject json = array.getJSONObject(i);
			int dataType = json.getInt("usertype");
			if (dataType == DATA_TYPE_NEAR) {
				nears.add(new NearBean(json));
			}
			else if (dataType == DATA_TYPE_ACTIVITY && page == 1) {
				activities.add(new IndexViewPager.ItemData(json));
			}
		}
		
		if (page == 1) {
			App.CACHE.save(CACHE_KEY, array.toString());
		}
	}

	/**
	 * 联网获取附近人信息
	 * 
	 * @param page
	 */
	private void getContent() {
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>(4);
		params.add(new BasicNameValuePair("user_id", String.valueOf(App.PREFS.getUid())));
		params.add(new BasicNameValuePair("page", String.valueOf(page)));
		params.add(new BasicNameValuePair("lat", String.valueOf(App.latitude)));
		params.add(new BasicNameValuePair("lng", String.valueOf(App.longitude)));
		String uri = page == 1 ? Constants.NRAR_BY_URL : Constants.NRAR_BY_NEXT;
		req = new HttpRequest(uri, params, HttpRequest.Method.GET, reqNearsCallback);
		App.THREAD.execute(req);
	}

	private final DefaultHttpCallback reqNearsCallback = new DefaultHttpCallback(new DefaultHttpCallback.EventListener() {

		@Override
		public void onSuccess(HttpResult result) {
			try {
				String str = result.getResult();
				ELog.i(str);

				JSONObject object = new JSONObject(str);
				Object content = object.get("content");
				if (content instanceof String) {
					// 如果content是空字符串，则表示没有数据
					handler.sendEmptyMessage(HANDLER_REQUEST_DATA);
					return;
				}
				// TODO 此处少解析一个字段actnum
				if (object.has("actnum")) {
					activityNum = Integer.valueOf(object.getString("actnum"));
				}
				
				JSONArray array = object.getJSONArray("content");
				json2list(array);
				
				handler.sendEmptyMessage(HANDLER_REQUEST_DATA);
			}
			catch (Exception e) {
				ELog.e("Exception:" + e.getMessage());
				handler.sendMessage(handler.obtainMessage(HANDLER_REQUEST_DATA, "获取附近信息失败"));
				e.printStackTrace();
			}
			finally {
				req = null;
			}
		}

		@Override
		public void onForceClose(ExceptionHttpResult result) {
			handler.sendEmptyMessage(HANDLER_REQUEST_DATA);
		}

		@Override
		public void onException(ExceptionHttpResult result) {
			handler.sendMessage(handler.obtainMessage(HANDLER_REQUEST_DATA, "获取附近 信息失败"));
			req = null;
		}
	});

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
			case HANDLER_REQUEST_DATA:
				if (footer != null) {
					footer.setState(HSZListViewAdapter.STATE_NORMAL);
				}
				if (header != null) {
					header.setState(HSZListViewAdapter.STATE_NORMAL);
				}
				if (msg.obj == null) {
					if (page == 1) {
						if (activities.isEmpty()) {
							setActivityVisibility(View.GONE);
						}
						else {
							setActivityVisibility(View.VISIBLE);
							//TODO 此处显示的是附近出现的活动个数，不是轮播图的活动的个数
							activityPanel.updateTip(String.valueOf(activityNum));
							activityPanel.addAll(activities);
							activityPanel.notifyDataSetChanged();
							activities.clear();
						}
					}
					
					synchronized (adapter) {
						adapter.addAll(nears);
						adapter.notifyDataSetChanged();
						footer.setVisibility(adapter.isEmpty() ? View.GONE : View.VISIBLE);
						nears.clear();
					}
				}
				else {
					Toast.makeText(getActivity(), (String) msg.obj, Toast.LENGTH_SHORT).show();
				}
				lstView.loaded();
				break;
		}
		return false;
	}

	private class NearAdapter extends HSZListViewAdapter<NearBean> {

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			NearItem item;
			if (convertView == null) {
				item = new NearItem(parent.getContext());
				item.setOnClickListener(cellClickListener);
			}
			else {
				item = (NearItem) convertView;
			}
			item.setTag(getItem(position));
			item.update(getItem(position));
			return item;
		}

		@Override
		public void onHeaderLoad() {
			if (!App.NetworkType.NONE.equals(App.getNetworkType())) {
				header.setState(HSZListViewAdapter.STATE_LOADING);
				page = 1;
				getContent();
			}
			else {
				Toast.makeText(getActivity(), R.string.err_network_invalid, Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		public void onHeaderPull(View headerView, int top) {
			super.onHeaderPull(headerView, top);
			header.setState(HSZListViewAdapter.STATE_NORMAL);
		}

		@Override
		public void onHeaderPullOverLine(View headerView, int top) {
			super.onHeaderPullOverLine(headerView, top);
			header.setState(HSZListViewAdapter.STATE_PULL_OVER);
		}

		@Override
		public void onFooterLoad() {
			footer.setState(HSZListViewAdapter.STATE_LOADING);
			if (!App.NetworkType.NONE.equals(App.getNetworkType())) {
				if (adapter.isEmpty())
					page = 1;
				else
					page++;
				getContent();
			}
			else {
				if (footer != null) {
					footer.setState(HSZListViewAdapter.STATE_NORMAL);
				}
				if (header != null) {
					header.setState(HSZListViewAdapter.STATE_NORMAL);
				}
				lstView.loaded();
				Toast.makeText(getActivity(), R.string.err_network_invalid, Toast.LENGTH_SHORT).show();
			}
		}
	}
}
