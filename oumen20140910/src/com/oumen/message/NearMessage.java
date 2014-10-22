package com.oumen.message;

import java.util.ArrayList;
import java.util.Date;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;

import com.oumen.R;
import com.oumen.android.App;
import com.oumen.android.util.Constants;
import com.oumen.http.DefaultHttpCallback;
import com.oumen.http.ExceptionHttpResult;
import com.oumen.http.HttpRequest;
import com.oumen.http.HttpResult;
import com.oumen.tools.ELog;

public class NearMessage implements MessageListItemDataProvider {
	protected final ForegroundColorSpan spanActivityColor = new ForegroundColorSpan(0xFF00C2FA);
	protected final ForegroundColorSpan spanUserColor = new ForegroundColorSpan(0xFFFF790C);
	
	public static int NEAR_ACTIVITIES = 0;
	public static int NEAR_USERS = 0;
	
	protected NearListener listener;
	
	public interface NearListener {
		void onUpdate();
	}
	
	public NearMessage() {
	}
	
	public void setListener(NearListener listener) {
		this.listener = listener;
	}
	
	public void obtainNearData() {
		DefaultHttpCallback callback = new DefaultHttpCallback(new DefaultHttpCallback.EventListener() {

			@Override
			public void onSuccess(HttpResult result) {
				try {
					String res = result.getResult();
					ELog.i(res);

					JSONObject json = new JSONObject(res);
					NEAR_ACTIVITIES = json.getInt("actnum");
					NEAR_USERS = json.getInt("usernum");
					MessageService.NEAR_NEWS = NEAR_ACTIVITIES;
					
					if (listener != null) {
						listener.onUpdate();
					}
				}
				catch (Exception e) {
					ELog.e("Exception:" + e.getMessage());
					e.printStackTrace();
				}
			}

			@Override
			public void onForceClose(ExceptionHttpResult result) {}

			@Override
			public void onException(ExceptionHttpResult result) {}
		});

		ArrayList<NameValuePair> list = new ArrayList<NameValuePair>();
		list.add(new BasicNameValuePair("uid", String.valueOf(App.PREFS.getUid())));
		list.add(new BasicNameValuePair("lat", String.valueOf(App.latitude)));
		list.add(new BasicNameValuePair("lng", String.valueOf(App.longitude)));

		HttpRequest req = new HttpRequest(Constants.GET_NEAR_DATA, list, HttpRequest.Method.GET, callback);
		App.THREAD.execute(req);
	}

	@Override
	public int getNewCount() {
		return MessageService.NEAR_NEWS;
	}

	@Override
	public String getTitle() {
		return "偶们附近";
	}

	@Override
	public int getTitleRightIconResId() {
		return App.INT_UNSET;
	}

	@Override
	public CharSequence getDescription() {
		String res = "您附近有";
		String activities = NEAR_ACTIVITIES > 99 ? "99+" : String.valueOf(NEAR_ACTIVITIES),
				users = NEAR_USERS > 999 ? "999+" : String.valueOf(NEAR_USERS);
		int aStart = res.length();
		int aEnd = aStart + activities.length();
		res += activities + "个活动，";
		int uStart = res.length();
		int uEnd = uStart + users.length();
		res += users + "个用户";
		SpannableStringBuilder builder = new SpannableStringBuilder(res);
		builder.setSpan(spanActivityColor, aStart, aEnd, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
		builder.setSpan(spanUserColor, uStart, uEnd, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
		return builder;
	}

	@Override
	public int getIconResId() {
		return R.drawable.icon_near;
	}

	@Override
	public String getIconPath() {
		return null;
	}

	@Override
	public int getButtonIconResId() {
		return App.INT_UNSET;
	}

	@Override
	public Date getTimestamp() {
		return new Date();
	}
}
