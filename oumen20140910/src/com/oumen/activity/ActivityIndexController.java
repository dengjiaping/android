package com.oumen.activity;

import java.util.Calendar;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;

import com.oumen.Controller;
import com.oumen.activity.list.ActivityFragment;
import com.oumen.activity.list.AmuseActivity;
import com.oumen.activity.search.SearchActivity;
import com.oumen.android.App;
import com.oumen.http.DefaultHttpCallback.EventListener;
import com.oumen.http.ExceptionHttpResult;
import com.oumen.http.HttpResult;
import com.oumen.tools.ELog;

public class ActivityIndexController extends Controller<ActivityIndexFragment> implements View.OnClickListener, Handler.Callback, View.OnTouchListener {
	final int HANDLER_UPDATE_BANNER = 1;

	final Handler handler = new Handler(this);

	final StringBuilder today = new StringBuilder();

	public ActivityIndexController(ActivityIndexFragment host) {
		super(host);

		Calendar now = Calendar.getInstance();
		today.append(now.get(Calendar.YEAR)).append(App.NUMBER_FORMAT.format(now.get(Calendar.MONTH))).append(App.NUMBER_FORMAT.format(now.get(Calendar.DAY_OF_MONTH)));
	}

	@Override
	public void onClick(View v) {
		if (v == host.barTitle.getRightButton()) {
			// TODO　跳转到附近活动
			Intent intent = new Intent(host.getActivity(), SearchActivity.class);
			host.startActivity(intent);
		}
		else if (v == host.barTitle.getLeftButton()) {
			host.host.menuToggle();
		}
		else if (v == host.btnHuWai) {
			filter(HuodongTypeUtil.CONDITION_HUWAI);
		}
		else if (v == host.btnShiNei) {
			filter(HuodongTypeUtil.CONDITION_SHINEI);
		}
		else if (v == host.btnXianShang) {
			filter(HuodongTypeUtil.CONDITION_XIANSHANG);
		}
		else if (v == host.fujin) {
			filter(HuodongTypeUtil.CONDITION_FUJIN);
		}
	}

	private void filter(int condition2) {
		ELog.i("Condition:" + condition2);
		Intent intent = new Intent(host.getActivity(), AmuseActivity.class);
		intent.putExtra(ActivityFragment.HUODONG_TYPE, condition2);
		host.startActivity(intent);
		host.pager.stopPlay();
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
					host.pager.onSuccess(res);
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
	public boolean handleMessage(Message msg) {
		if (msg.what == HANDLER_UPDATE_BANNER) {
			host.pager.notifyDataSetChanged();

			if (host.pager.isEmpty() && host.pager.getVisibility() == View.VISIBLE) {
				//隐藏
				host.pager.setVisibility(View.GONE);
			}
			else if (!host.pager.isEmpty() && host.pager.getVisibility() != View.VISIBLE) {
				//显示
				host.pager.setVisibility(View.VISIBLE);
			}
		}
		return false;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (!host.isPopupWindowPlaying()) {
			host.togglePopupWindow();
		}
		return true;
	}
}
