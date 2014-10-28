package com.oumen.activity.list;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ab.view.listener.AbOnListViewListener;
import com.ab.view.pullview.AbPullListView;
import com.oumen.R;
import com.oumen.TitleBar;
import com.oumen.activity.detail.HuoDongDetailActivity;
import com.oumen.activity.message.BaseActivityMessage;
import com.oumen.android.App;
import com.oumen.android.BaseFragment;
import com.oumen.home.LoginConfrim;
import com.oumen.tools.ELog;

/**
 * 
 * 附近活动列表
 */
public class NearActivityFragment extends BaseFragment {
	public static final String CACHE_KEY = "activities_near";

	//标题行控件
	private TitleBar titleBar;
	private Button btnLeft;

	private AbPullListView lstView;

	private final ActivityAdapter adapter = new ActivityAdapter();

	private int currentPage = 1;

	private LoginConfrim loginConfrim;

	private HuodongListHttpController controller;

	private boolean firstFlag = true;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		loginConfrim = new LoginConfrim(getActivity());

		controller = new HuodongListHttpController(this, handler);
		
		if (firstFlag) {
			showProgressDialog();
			firstFlag = false;
		}
		App.THREAD.execute(new Runnable() {
			
			@Override
			public void run() {
				String cache = App.CACHE.read(CACHE_KEY);
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
		});
		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		//发起
		View view = inflater.inflate(R.layout.fragment_near_huodong, container, false);

		titleBar = (TitleBar) view.findViewById(R.id.titlebar);
		titleBar.getRightButton().setVisibility(View.GONE);
		titleBar.getTitle().setText("附近活动");

		btnLeft = titleBar.getLeftButton();
		btnLeft.setOnClickListener(clickListener);

		// ------------ListView-------------

		lstView = (AbPullListView) view.findViewById(R.id.refreshablelistview);
		lstView.setBackgroundColor(getResources().getColor(R.color.white));
		lstView.setDivider(new ColorDrawable(getResources().getColor(R.color.user_center_click_bg)));
		lstView.setDividerHeight(1);
		lstView.getHeaderView().setHeaderProgressBarDrawable(this.getResources().getDrawable(R.drawable.progress_circular));
		lstView.getFooterView().setFooterProgressBarDrawable(this.getResources().getDrawable(R.drawable.progress_circular));
		TextView emptyView = (TextView) view.findViewById(R.id.empty_view);
		emptyView.setText(getResources().getString(R.string.activity_near_empty));
		lstView.setAdapter(adapter);
		lstView.setEmptyView(emptyView);
		lstView.setSelector(getResources().getDrawable(R.drawable.white_and_grey_selector));
		lstView.setAbOnListViewListener(listViewListener);
		return view;
	}

	private AbOnListViewListener listViewListener = new AbOnListViewListener() {

		@Override
		public void onRefresh() {
			controller.obtainNearActivity(1);
		}

		@Override
		public void onLoadMore() {
			int page = adapter.isEmpty() ? 1 : currentPage + 1;
			controller.obtainNearActivity(page);
		}
	};

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		handler.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				if (!adapter.isEmpty()) {
					adapter.notifyDataSetChanged();
					dismissProgressDialog();
				}
				controller.obtainNearActivity(1);
			}
		}, 500);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	public boolean onBackPressed() {
		return super.onBackPressed();
	}

	public List<BaseActivityMessage> parseJson(JSONObject json) throws JSONException {
		List<BaseActivityMessage> results = new LinkedList<BaseActivityMessage>();
		JSONArray array = json.getJSONArray("data");

		for (int i = 0; i < array.length(); i++) {
			JSONObject itemJson = array.getJSONObject(i);
			results.add(new BaseActivityMessage(itemJson));
		}

		if (adapter.isEmpty() && !results.isEmpty()) {
			App.CACHE.save(CACHE_KEY, json.toString());
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
					}

					adapter.notifyDataSetChanged();
				}
				lstView.stopRefresh();
				lstView.stopLoadMore();
				dismissProgressDialog();
				break;

//			case HuodongListHttpController.HANDLER_UPDATE_PROGRESS:
//				ELog.i("Update");
//				synchronized (adapter) {
//					adapter.notifyDataSetChanged();
//				}
//				break;
		}
		return false;
	}

	private final View.OnClickListener clickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if (v == btnLeft) {
				getActivity().finish();
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
			item.update(itemData);
			return item;
		}
	}
}
