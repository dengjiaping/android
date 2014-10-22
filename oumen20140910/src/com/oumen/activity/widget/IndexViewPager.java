package com.oumen.activity.widget;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import widget.viewpager.CirclePageIndicator;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.oumen.R;
import com.oumen.activity.ActivityWebViewActivity;
import com.oumen.activity.detail.HuoDongDetailActivity;
import com.oumen.activity.detail.cell.CircleCornerImageHasDownloadView;
import com.oumen.android.App;
import com.oumen.android.util.Constants;
import com.oumen.home.LoginConfrim;
import com.oumen.http.DefaultHttpCallback;
import com.oumen.http.DefaultHttpCallback.EventListener;
import com.oumen.http.HttpRequest;
import com.oumen.tools.ELog;
import com.oumen.widget.file.ImageData;
import com.oumen.widget.image.ImagePreviewActivity;
import com.oumen.widget.preview.image.ImagePagerFragment;

public class IndexViewPager extends FrameLayout implements Callback {
	private final int HANDLER_START_NEXT = 3;

	private final int TIMER_GAP = 5000;

	protected ViewPager pager;
	protected CirclePageIndicator indicator;

	private final AdapterImpl adapter = new AdapterImpl();

	private Context context;

	private LoginConfrim loginConfrim;

	private Handler handler = new Handler(this);

	private boolean stopTimerFlag = false;

	public IndexViewPager(Context context) {
		this(context, null, 0);
	}

	public IndexViewPager(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public IndexViewPager(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.context = context;

		loginConfrim = new LoginConfrim(context);

		int padding = getResources().getDimensionPixelSize(R.dimen.padding_large);

		pager = new ViewPager(context);
		pager.setAdapter(adapter);
		addView(pager, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

		indicator = new CirclePageIndicator(context);
		indicator.setPadding(padding, padding, padding, padding);
		indicator.setViewPager(pager);
		addView(indicator, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM));
	}

	public void setPagerViewLayoutParams() {
		android.view.ViewGroup.LayoutParams params = pager.getLayoutParams();
		params.width = getResources().getDisplayMetrics().widthPixels - getResources().getDimensionPixelSize(R.dimen.padding_large) * 2;
		params.height = params.width * 18 / 29;//Banner宽高比为29:18
		ELog.i("width =" + params.width + ", height =" + params.height);
		pager.setLayoutParams(params);
	}

	/**
	 * 获取轮播图信息
	 */
	public void obtainBanner(int uid, EventListener listener) {
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>(1);
		params.add(new BasicNameValuePair("user_id", String.valueOf(uid)));
		HttpRequest req = new HttpRequest(Constants.OUMENAMUSEMENT_IMAGESWITCH, params, HttpRequest.Method.GET, new DefaultHttpCallback(listener));
		App.THREAD.execute(req);
	}

	public void onSuccess(String res) throws Exception {
		JSONObject json = new JSONObject(res);
		LinkedList<ItemData> tmp = new LinkedList<ItemData>();
		if (json.getInt("count") > 0) {
			JSONArray array = json.getJSONArray("content");
			for (int i = 0; i < array.length(); i++) {
				try {
					tmp.add(new ItemData(array.getJSONObject(i)));
				}
				catch (Exception e) {
					ELog.i("Exception:" + e.toString());
					e.printStackTrace();
				}
			}
		}

		synchronized (adapter.data) {
			adapter.data.clear();
			adapter.data.addAll(tmp);
			if (adapter.data.size() <= 1) {
				indicator.setVisibility(View.GONE);
			}
			else {
				indicator.setVisibility(View.VISIBLE);
			}
			//TODO 开始轮播
			ELog.i("取到数据了");
			if (adapter.data.size() > 1) {
				startPlay();
			}
		}
	}

	public void notifyDataSetChanged() {
		adapter.notifyDataSetChanged();
	}

	public void addAll(Collection<ItemData> collection) {
		synchronized (adapter.data) {
			adapter.data.clear();
			adapter.data.addAll(collection);

			if (adapter.data.size() <= 1) {
				indicator.setVisibility(View.GONE);
			}
			else {
				indicator.setVisibility(View.VISIBLE);
			}
			ELog.i("取到数据了");
			if (adapter.data.size() > 1) {
				startPlay();
			}
		}
	}

	public void clear() {
		synchronized (adapter.data) {
			adapter.data.clear();
		}
	}

	public boolean isEmpty() {
		synchronized (adapter.data) {
			return adapter.data.isEmpty();
		}
	}

	public ArrayList<ItemData> copyDataSource() {
		ArrayList<ItemData> tmp = new ArrayList<ItemData>();
		Collections.copy(adapter.data, tmp);
		return tmp;
	}

	class AdapterImpl extends PagerAdapter {

		final List<ItemData> data = new ArrayList<ItemData>();

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			ItemData itemData = data.get(position);
			CircleCornerImageHasDownloadView item = new CircleCornerImageHasDownloadView(container.getContext());
			item.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
			item.setRadius(getResources().getDimensionPixelSize(R.dimen.radius_large));

			container.addView(item);
			if (!TextUtils.isEmpty(itemData.picUrl)) {
				item.update(itemData.picUrl);
			}

			item.setTag(itemData);
			item.setOnClickListener(clickListener);
			return item;
		}

		@Override
		public int getCount() {
			return data.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}
	}

	private final OnClickListener clickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {

			if (TextUtils.isEmpty(App.PREFS.getUserProfile())) {
				loginConfrim.openDialog();
				return;
			}

			if (v instanceof CircleCornerImageHasDownloadView) {
				stopPlay();
				ItemData itemData = (ItemData) v.getTag();
				if (itemData.type == ItemData.TYPE_ACTIVITY_DETAIL) {// 跳转到活动详情界面

					Intent intent = new Intent(context, HuoDongDetailActivity.class);
					intent.putExtra(HuoDongDetailActivity.INTENT_KEY_ACTIVITY_ID, itemData.activityId);
					context.startActivity(intent);
				}
				else if (itemData.type == ItemData.TYPE_ACTIVITY_URL) {// 跳转到活动网页界面
					Intent intent = new Intent(context, ActivityWebViewActivity.class);
					intent.putExtra(ActivityWebViewActivity.INTENT_WEBVIEW_ACTIVITY_URL, itemData.webUrl);
					intent.putExtra(ActivityWebViewActivity.INTENT_WEBVIEW_SHARE_MSG, itemData.shareJson);
					context.startActivity(intent);
				}
				else if (itemData.type == ItemData.TYPE_LAGER_IAMGE) {// 点击查看大图

					ArrayList<ImageData> templist = new ArrayList<ImageData>();
					templist.add(new ImageData(itemData.picUrl));

					Bundle params = new Bundle();
					params.putInt(ImagePagerFragment.PARAMS_KEY_START_INDEX, 0);
					params.putSerializable(ImagePagerFragment.PARAMS_KEY_DATA, templist);

					Intent intent = new Intent(v.getContext(), ImagePreviewActivity.class);
					intent.putExtra(ImagePreviewActivity.INTENT_KEY_DATA, params);
					v.getContext().startActivity(intent);
				}
			}
		}
	};

	public static class ItemData implements Parcelable {
		public static final int TYPE_ACTIVITY_DETAIL = 1;// APP显示活动
		public static final int TYPE_ACTIVITY_URL = 2;// 网页显示活动
		public static final int TYPE_LAGER_IAMGE = 3;// 点击查看大图

		int activityId;
		int type;
		String picUrl;
		// 新增网页分享数据信息
		String webUrl;
		//不同url分享出去的文字信息都不同，传递JsonArray
		String shareJson;

		public ItemData(JSONObject json) throws NumberFormatException, JSONException {
			activityId = Integer.parseInt(json.getString("atid"));
			picUrl = json.getString("pic");
			if (json.has("type")) {
				Object tmp = json.get("type");
				if (tmp instanceof Integer) {
					type = json.getInt("type");
				}
				else if (tmp instanceof String) {
					type = Integer.parseInt(json.getString("type"));
				}

				if (TYPE_ACTIVITY_URL == type) {//如果为网页打开，需要解析以下的信息
					webUrl = json.getString("url");
					if (json.has("share")) {
						shareJson = json.getString("share");
					}
				}

			}
		}

		ItemData(Parcel in) {
			activityId = in.readInt();
			type = in.readInt();
			picUrl = in.readString();
			webUrl = in.readString();
			shareJson = in.readString();
		}

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeInt(activityId);
			dest.writeInt(type);
			dest.writeString(picUrl);
			dest.writeString(webUrl);
			dest.writeString(shareJson);
		}

		public static final Parcelable.Creator<ItemData> CREATOR = new Parcelable.Creator<ItemData>() {
			public ItemData createFromParcel(Parcel in) {
				return new ItemData(in);
			}

			public ItemData[] newArray(int size) {
				return new ItemData[size];
			}
		};
	}

	private Runnable runnable = new Runnable() {

		@Override
		public void run() {
			handler.sendEmptyMessage(HANDLER_START_NEXT);
		}
	};

	/**
	 * 描述：自动轮播.
	 */
	public void startPlay() {
		stopTimerFlag = false;
		if (handler != null) {
			handler.postDelayed(runnable, TIMER_GAP);
		}
	}

	/**
	 * 描述：自动轮播.
	 */
	public void stopPlay() {
		stopTimerFlag = true;
		if (handler != null) {
			handler.removeCallbacks(runnable);
		}
	}

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
			case HANDLER_START_NEXT:
				int position = pager.getCurrentItem();
				ELog.i("position = " + position);
				if (position == adapter.getCount() - 1) {
					pager.setCurrentItem(0, false);
				}
				else {
					pager.setCurrentItem(position + 1, true);
				}
				if (!stopTimerFlag) {
					handler.postDelayed(runnable, TIMER_GAP);
				}

				break;
		}
		return false;
	}
}
