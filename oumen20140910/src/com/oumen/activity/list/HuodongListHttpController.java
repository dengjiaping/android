package com.oumen.activity.list;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import com.oumen.activity.HuodongTypeUtil;
import com.oumen.activity.message.BaseActivityMessage;
import com.oumen.android.App;
import com.oumen.android.util.Constants;
import com.oumen.base.Cache;
import com.oumen.http.DefaultHttpCallback;
import com.oumen.http.ExceptionHttpResult;
import com.oumen.http.HttpRequest;
import com.oumen.http.HttpResult;
import com.oumen.tools.ELog;

import android.os.Handler;
import android.text.TextUtils;

public class HuodongListHttpController {
	public static final int HANDLER_REQUEST_LIST = 1;
	public static final int HANDLER_NONE_NETWORK = 2;//没有网络
//	public static final int HANDLER_UPDATE_PROGRESS = 2;
	private Handler handler;

	private ActivityHostProvider host;
	private NearActivityFragment host1;

	public HuodongListHttpController(ActivityHostProvider host, Handler handler) {
		this.handler = handler;
		this.host = host;
	}

	public HuodongListHttpController(NearActivityFragment host, Handler handler) {
		this.handler = handler;
		this.host1 = host;
	}

	/**
	 * 获取活动列表
	 * 
	 * @param starttime
	 *            活动开始时间
	 * @param age
	 *            适合的参与者年龄
	 * @param order
	 *            排序
	 * @param actmoney
	 *            参加活动的费用
	 * @param pager
	 *            获取第几页信息 //actname
	 * 
	 *            page=1&uid=299838&usertype=2&lat=39.913506&lng=116.519554&
	 *            hdtypes=0&order=2&date=2014-10-05&applyage=5
	 */
	public void obtainActivities(int type, int age, int order, String date, final int page) {
		//TODO　没有网络就不网络请求了，直接返回
		if (App.NetworkType.NONE.equals(App.getNetworkType())) {
			handler.sendEmptyMessage(HANDLER_NONE_NETWORK);
			return;
		}
		DefaultHttpCallback callback = new DefaultHttpCallback(new DefaultHttpCallback.EventListener() {

			@Override
			public void onSuccess(HttpResult result) {
				try {
					String str = result.getResult();
					ELog.i(str);

					List<BaseActivityMessage> results = host.parseJson(new JSONObject(str));
					if (results.size() > 0) {
						handler.sendMessage(handler.obtainMessage(HANDLER_REQUEST_LIST, page, 0, results));
					}
					else {
						handler.sendEmptyMessage(HANDLER_REQUEST_LIST);
					}
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
		list.add(new BasicNameValuePair("uid", String.valueOf(App.PREFS.getUid())));// uid
		list.add(new BasicNameValuePair("usertype", "2"));// 用户类型（目前只有企业）

		if (age != App.INT_UNSET) {
			list.add(new BasicNameValuePair("applyage", String.valueOf(age)));
		}

		if (type != App.INT_UNSET) {
			if (type == HuodongTypeUtil.CONDITION_FUZZY_SEARCH) {
				list.add(new BasicNameValuePair("actname", App.PREFS.getLastHistorySearch()));
			}
			else {
				list.add(new BasicNameValuePair("hdtypes", String.valueOf(type)));
			}

			if (!TextUtils.isEmpty(App.CACHE.read(Cache.CACHE_USER_CHOOSE_CITY_NAME))) {
				list.add(new BasicNameValuePair("city", App.CACHE.read(Cache.CACHE_USER_CHOOSE_CITY_NAME)));
			}
			else {
				if (App.PREFS.getLatitude() != 0.0f && App.PREFS.getLongitude() != 0.0f) {
					list.add(new BasicNameValuePair("lat", String.valueOf(App.PREFS.getLatitude())));
					list.add(new BasicNameValuePair("lng", String.valueOf(App.PREFS.getLongitude())));
				}
				else {
					list.add(new BasicNameValuePair("lat", String.valueOf(App.latitude)));
					list.add(new BasicNameValuePair("lng", String.valueOf(App.longitude)));
				}
			}
		}

		if (!TextUtils.isEmpty(date)) {
			list.add(new BasicNameValuePair("date", date));
		}

		list.add(new BasicNameValuePair("order", String.valueOf(order)));

		HttpRequest req = new HttpRequest(Constants.GET_ACTIVITIES, list, HttpRequest.Method.GET, callback);
		App.THREAD.execute(req);
	}

	/**
	 * 获取偶们附近活动
	 * 
	 * @param page
	 */
	public void obtainNearActivity(final int page) {
		//TODO　没有网络就不网络请求了，直接返回
		if (App.NetworkType.NONE.equals(App.getNetworkType())) {
			handler.sendEmptyMessage(HANDLER_NONE_NETWORK);
			return;
		}
		DefaultHttpCallback callback = new DefaultHttpCallback(new DefaultHttpCallback.EventListener() {

			@Override
			public void onSuccess(HttpResult result) {
				try {
					String str = result.getResult();
					ELog.i(str);

					List<BaseActivityMessage> lists = host1.parseJson(new JSONObject(str));

					if (lists.size() > 0) {
						handler.sendMessage(handler.obtainMessage(HANDLER_REQUEST_LIST, page, 0, lists));
					}
					else {
						handler.sendEmptyMessage(HANDLER_REQUEST_LIST);
					}
				}
				catch (Exception e) {
					ELog.e("Exception:" + e.getMessage());
					handler.sendMessage(handler.obtainMessage(HANDLER_REQUEST_LIST, "获取附近活动失败"));
					e.printStackTrace();
				}
			}

			@Override
			public void onForceClose(ExceptionHttpResult result) {
			}

			@Override
			public void onException(ExceptionHttpResult result) {
				handler.sendMessage(handler.obtainMessage(HANDLER_REQUEST_LIST, "获取附近活动失败"));
			}
		});

		ArrayList<NameValuePair> list = new ArrayList<NameValuePair>();
		list.add(new BasicNameValuePair("user_id", String.valueOf(App.PREFS.getUid())));
		list.add(new BasicNameValuePair("lat", String.valueOf(App.latitude)));
		list.add(new BasicNameValuePair("lng", String.valueOf(App.longitude)));
		list.add(new BasicNameValuePair("page", String.valueOf(page)));

		HttpRequest req = new HttpRequest(Constants.GET_NEAR_ACTIVITIES, list, HttpRequest.Method.GET, callback);
		App.THREAD.execute(req);
	}

}
