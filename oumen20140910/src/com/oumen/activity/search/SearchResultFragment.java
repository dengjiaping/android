package com.oumen.activity.search;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Toast;

import com.ab.view.listener.AbOnListViewListener;
import com.ab.view.pullview.AbPullListView;
import com.oumen.R;
import com.oumen.activity.HuodongTypeUtil;
import com.oumen.activity.HuodongTypeUtil.OrderType;
import com.oumen.activity.detail.HuoDongDetailActivity;
import com.oumen.activity.list.ActivityHostProvider;
import com.oumen.activity.list.HuodongListHttpController;
import com.oumen.activity.list.NearHuodongItem;
import com.oumen.activity.message.BaseActivityMessage;
import com.oumen.android.App;
import com.oumen.android.BaseFragment;
import com.oumen.home.LoginConfrim;
import com.oumen.tools.ELog;

/**
 * 
 * 附近活动列表
 */
public class SearchResultFragment extends BaseFragment implements ActivityHostProvider {
	public static final String CACHE_KEY = "activities_search_";

	//标题行控件
	private SearchTitleBar titleBar;
	private Button btnLeft, btnRight;

	private AbPullListView lstView;

	private final ActivityAdapter adapter = new ActivityAdapter();

	private int currentPage = 1;

	private LoginConfrim loginConfrim;

	private HuodongListHttpController controller;

	private InputMethodManager inputManager;

	private String tempInput = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

		loginConfrim = new LoginConfrim(getActivity());

		controller = new HuodongListHttpController(this, handler);

		String cache = App.CACHE.read(getCacheKey());
		if (!TextUtils.isEmpty(cache)) {
			try {
				List<BaseActivityMessage> results = parseJson(new JSONObject(cache));
				adapter.data.addAll(results);
			}
			catch (Exception e) {
				ELog.e("Exception:" + e.getMessage());
				e.printStackTrace();
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		//发起
		View view = inflater.inflate(R.layout.fragment_search_huodong, container, false);

		titleBar = (SearchTitleBar) view.findViewById(R.id.titlebar);
		btnRight = titleBar.getRightButton();
		btnRight.setText("搜索");
		btnRight.setOnClickListener(clickListener);
		titleBar.getTitle().setHint("输入关键字");
		titleBar.getTitle().setText("");
		btnLeft = titleBar.getLeftButton();
		btnLeft.setOnClickListener(clickListener);

		// ------------ListView-------------
		lstView = (AbPullListView) view.findViewById(R.id.refreshablelistview);
		lstView.setBackgroundColor(getResources().getColor(R.color.white));
		lstView.setDivider(new ColorDrawable(getResources().getColor(R.color.user_center_click_bg)));
		lstView.setDividerHeight(1);
		lstView.getHeaderView().setHeaderProgressBarDrawable(this.getResources().getDrawable(R.drawable.progress_circular));
		lstView.getFooterView().setFooterProgressBarDrawable(this.getResources().getDrawable(R.drawable.progress_circular));
		lstView.setEmptyView(view.findViewById(R.id.empty_view));
		lstView.setAdapter(adapter);
		lstView.setSelector(android.R.color.transparent);
		lstView.setAbOnListViewListener(listViewListener);
		return view;
	}

	private AbOnListViewListener listViewListener = new AbOnListViewListener() {

		@Override
		public void onRefresh() {
			controller.obtainActivities(HuodongTypeUtil.CONDITION_FUZZY_SEARCH, App.INT_UNSET, OrderType.DEFAULT.code(), null, 1);
		}

		@Override
		public void onLoadMore() {
			int page = adapter.isEmpty() ? 1 : currentPage + 1;
			controller.obtainActivities(HuodongTypeUtil.CONDITION_FUZZY_SEARCH, App.INT_UNSET, OrderType.DEFAULT.code(), null, page);
		}
	};

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		if (!adapter.isEmpty()) {
			adapter.notifyDataSetChanged();
		}
		controller.obtainActivities(HuodongTypeUtil.CONDITION_FUZZY_SEARCH, App.INT_UNSET, OrderType.DEFAULT.code(), null, 1);
	}

	private String getCacheKey() {
		return CACHE_KEY + App.PREFS.getLastHistorySearch();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	public boolean onBackPressed() {
		if (inputManager.isActive()) {
			inputManager.hideSoftInputFromWindow(titleBar.getTitle().getWindowToken(), 0);
		}
		if (getFragmentManager().getBackStackEntryCount() > 0) {
			getFragmentManager().popBackStack();
		}
		else {
			getActivity().finish();
		}
		return true;
	}

	public List<BaseActivityMessage> parseJson(JSONObject json) {
		List<BaseActivityMessage> results = new LinkedList<BaseActivityMessage>();

		try {
			if (json.has("data")) {
				JSONArray array = json.getJSONArray("data");

				for (int i = 0; i < array.length(); i++) {
					JSONObject itemJson = array.getJSONObject(i);
					results.add(new BaseActivityMessage(itemJson));
				}

				if (adapter.isEmpty() && !results.isEmpty()) {
					App.CACHE.save(getCacheKey(), json.toString());
				}
			}
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
		return results;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
			case HuodongListHttpController.HANDLER_REQUEST_LIST:
				synchronized (adapter) {
					if (msg.obj instanceof List<?>) {
						List<BaseActivityMessage> results = (List<BaseActivityMessage>) msg.obj;
						if (msg.arg1 == 1) {
							adapter.data.clear();
						}
						adapter.data.addAll(results);

						if (!results.isEmpty()) {
							currentPage = msg.arg1;
						}
						adapter.notifyDataSetChanged();
					}
					else if (msg.obj instanceof CharSequence) {
						Toast.makeText(lstView.getContext(), (CharSequence) msg.obj, Toast.LENGTH_SHORT).show();
						if (!App.PREFS.getLastHistorySearch().equals(tempInput)) {
							adapter.data.clear();
						}
					}
					else {
						if (!App.PREFS.getLastHistorySearch().equals(tempInput)) {
							adapter.data.clear();
						}
					}
					adapter.notifyDataSetChanged();
				}
				lstView.stopRefresh();
				lstView.stopLoadMore();
				break;
		}
		return false;
	}

	private final View.OnClickListener clickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if (inputManager.isActive()) {
				inputManager.hideSoftInputFromWindow(titleBar.getTitle().getWindowToken(), 0);
			}

			if (v == btnLeft) {
				if (getFragmentManager().getBackStackEntryCount() > 0) {
					getFragmentManager().popBackStack();
				}
				else {
					getActivity().finish();
				}
			}
			else if (v == btnRight) {
				// TODO　搜索
				String str = titleBar.getTitle().getText().toString().trim();
				if (TextUtils.isEmpty(str)) {
					Toast.makeText(getActivity(), "请输入内容", Toast.LENGTH_SHORT).show();
					return;
				}
				tempInput = App.PREFS.getLastHistorySearch();
				App.PREFS.addHistorySearch(str);

				controller.obtainActivities(HuodongTypeUtil.CONDITION_FUZZY_SEARCH, App.INT_UNSET, OrderType.DEFAULT.code(), null, 1);
			}
			else if (v instanceof NearHuodongItem) {
				if (TextUtils.isEmpty(App.PREFS.getUserProfile())) {
					//TODO 跳转到登录界面
					loginConfrim.openDialog();
					return;
				}

				BaseActivityMessage item = (BaseActivityMessage) v.getTag();
				Intent intent = new Intent(getActivity(), HuoDongDetailActivity.class);
				intent.putExtra(HuoDongDetailActivity.INTENT_KEY_ACTIVITY_ID, item.getActivityId());
				getActivity().startActivityForResult(intent, 1);

			}
		}
	};

	private class ActivityAdapter extends BaseAdapter {
		private List<BaseActivityMessage> data = new ArrayList<BaseActivityMessage>();

		@Override
		public int getCount() {
			return data.size();
		}

		@Override
		public BaseActivityMessage getItem(int position) {
			return data.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			NearHuodongItem item;
			if (convertView == null) {
				item = new NearHuodongItem(getActivity());
				item.setOnClickListener(clickListener);
			}
			else {
				item = (NearHuodongItem) convertView;
			}
			BaseActivityMessage itemData = null;
			synchronized (this) {
				itemData = data.get(position);
			}
			item.setTag(itemData);
			item.update(itemData, itemData.getType());
			return item;
		}
	}
}
