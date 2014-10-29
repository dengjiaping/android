package com.oumen.activity.main;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.oumen.Controller;
import com.oumen.activity.list.DefaultHuodongItem;
import com.oumen.activity.message.ActivityTag;
import com.oumen.activity.message.BaseActivityMessage;
import com.oumen.activity.search.SearchActivity;
import com.oumen.android.App;
import com.oumen.android.App.NetworkType;
import com.oumen.android.util.Constants;
import com.oumen.base.Cache;
import com.oumen.cities.LocationChangeActivity;
import com.oumen.http.DefaultHttpCallback;
import com.oumen.http.ExceptionHttpResult;
import com.oumen.http.HttpRequest;
import com.oumen.http.HttpResult;
import com.oumen.http.DefaultHttpCallback.EventListener;
import com.oumen.tools.ELog;
import com.oumen.widget.refushlist.AbOnListViewListener;

public class ActivityMainController extends Controller<ActivityMainFragment> implements View.OnClickListener, Handler.Callback, AbOnListViewListener {
	static final int HANDLER_UPDATE_BANNER = 1;
	
	static final int HANDLER_REQUEST_LIST = 2;

	final Handler handler = new Handler(this);

	final StringBuilder today = new StringBuilder();

	final AdapterImpl adapter = new AdapterImpl();
	
	final String CACHE_KEY_ACTIVITY_TAG = "cache_activity_key_tag";
	
	public ActivityMainController(ActivityMainFragment host) {
		super(host);

		Calendar now = Calendar.getInstance();
		today.append(now.get(Calendar.YEAR)).append(App.NUMBER_FORMAT.format(now.get(Calendar.MONTH))).append(App.NUMBER_FORMAT.format(now.get(Calendar.DAY_OF_MONTH)));
	}

	void onViewCreated(View view, Bundle savedInstanceState) {
		//显示当前城市
		updateLocationCity();
		//加载轮播图
		if (!NetworkType.NONE.equals(App.getNetworkType())) {
			host.header.getImageSwitchView().obtainBanner(App.PREFS.getUid(), getObtainBannerDataListener());
		}
		// 加载主页信息
	}

	/**
	 * 更新当前城市信息
	 */
	public void updateLocationCity() {
		String str = App.CACHE.read(Cache.CACHE_USER_CHOOSE_CITY_NAME);
		if (TextUtils.isEmpty(str)) {
			str = "北京";
		}
		host.btnRight.setText(str);
	}

	@Override
	public boolean handleMessage(Message msg) {
		return false;
	}

	@Override
	public void onClick(View v) {
		if (v == host.btnleft) {
			host.host.menuToggle();
		}
		else if (v == host.btnRight) {
			host.startActivity(new Intent(host.getActivity(), LocationChangeActivity.class));
		}
		else if (v == host.title) {
			host.startActivity(new Intent(host.getActivity(), SearchActivity.class));
		}
	}

	/**
	 * banner图监听
	 * 
	 * @return
	 */
	EventListener getObtainBannerDataListener() {
		return new EventListener() {

			@Override
			public void onSuccess(HttpResult result) {
				try {
					String res = result.getResult();
					ELog.i(res);

					App.CACHE.save(host.CACHE_KEY, today.toString() + res);
					host.header.getImageSwitchView().onSuccess(res);
					handler.sendEmptyMessage(HANDLER_UPDATE_BANNER);
				}
				catch (Exception e) {
					ELog.i("Exception:" + e.toString());
					e.printStackTrace();
				}
			}

			@Override
			public void onForceClose(ExceptionHttpResult result) {
			}

			@Override
			public void onException(ExceptionHttpResult result) {
			}
		};
	}

	@Override
	public void onRefresh() {
		// TODO 刷新
	}

	@Override
	public void onLoadMore() {
		// 加载更多
	}

	class AdapterImpl extends BaseAdapter {
		final List<BaseActivityMessage> data = new ArrayList<BaseActivityMessage>();

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
			DefaultHuodongItem item;
			if (convertView == null) {
				item = new DefaultHuodongItem(parent.getContext());
			}
			else {
				item = (DefaultHuodongItem) convertView;
			}
			item.update(data.get(position));
			return item;
		}
	}
	
	/**
	 * 获取tag, 专家推荐， 热门活动
	 * @param page
	 */
	public void getActivityMessage(final int page) {
		DefaultHttpCallback callback = new DefaultHttpCallback(new DefaultHttpCallback.EventListener() {

			@Override
			public void onSuccess(HttpResult result) {
				try {
					String str = result.getResult();
					ELog.i(str);
					// 如果是第一页，就更新tag,专家推荐，热门活动
					if (page == 1) {
						JSONObject obj = new JSONObject(str);
						// TODO ==================获取活动标签=====================
						if (obj.has("tag")) {
							App.ACTIVITY_TAG_DATA.clear();
							JSONArray tagArray = obj.getJSONArray("tag");
							for (int i = 0;i < tagArray.length(); i++) {
								App.ACTIVITY_TAG_DATA.add(new ActivityTag(tagArray.getJSONObject(i)));
							}
						}
						
					}
					else {// 如果不是第一页，就直接加载热门活动
						
					}
//					List<BaseActivityMessage> results = host.parseJson(new JSONObject(str));
//					if (results.size() > 0) {
//						handler.sendMessage(handler.obtainMessage(HANDLER_REQUEST_LIST, page, 0, results));
//					}
//					else {
//						handler.sendEmptyMessage(HANDLER_REQUEST_LIST);
//					}
				}
				catch (Exception e) {
					ELog.e("Exception:" + e.getMessage());
					handler.sendMessage(handler.obtainMessage(HANDLER_REQUEST_LIST, "数据异常，获取活动失败"));
					e.printStackTrace();
				}
			}

			@Override
			public void onForceClose(ExceptionHttpResult result) {
				handler.sendEmptyMessage(HANDLER_REQUEST_LIST);
			}

			@Override
			public void onException(ExceptionHttpResult result) {
				handler.sendMessage(handler.obtainMessage(HANDLER_REQUEST_LIST, "网络异常，获取活动失败"));
			}
		});
		
		ArrayList<NameValuePair> list = new ArrayList<NameValuePair>();
		list.add(new BasicNameValuePair("page", String.valueOf(page)));
		HttpRequest req = new HttpRequest(Constants.GET_ACTIVITIES, list, HttpRequest.Method.GET, callback);
		App.THREAD.execute(req);
	}

}
