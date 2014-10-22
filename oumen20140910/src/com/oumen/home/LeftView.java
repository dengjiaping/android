package com.oumen.home;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.oumen.R;
import com.oumen.activity.PushActivityListActivity;
import com.oumen.activity.search.SearchActivity;
import com.oumen.activity.user.UserActivity;
import com.oumen.android.App;
import com.oumen.android.UserProfile;
import com.oumen.base.Cache;
import com.oumen.circle.CircleActivity;
import com.oumen.cities.CityChooseActivity;
import com.oumen.cities.LocationChangeActivity;
import com.oumen.message.ActivityMessage;
import com.oumen.message.CircleMessage;
import com.oumen.message.MessageService;
import com.oumen.mv.index.IndexActivity;
import com.oumen.setting.SettingActivity;
import com.oumen.usercenter.UserCenterActivity;

/**
 * 
 * 左侧导航界面
 * 
 */
public class LeftView extends RelativeLayout {
	private ImageView photo;
	private TextView nickName;

	private LinearLayout userActivity;// 我的活动
	private LinearLayout messageCenter;// 消息中心
	private LinearLayout mv;// mv
	private LinearLayout group;// 偶们圈
	private LinearLayout setting;//设置界面
	private LinearLayout location;

	private View userActivityTip;
	private TextView messageCenterTip;
	private View mvTip;
	private TextView groupMegTip;
	private View settingTip;

	private TextView locationCity;

	private LoginConfrim loginConfirm;

	public static final DisplayImageOptions OPTIONS_HEAD_ROUND = new DisplayImageOptions.Builder().showImageForEmptyUri(R.drawable.default_home_left_photo).showImageOnFail(R.drawable.default_home_left_photo).cacheOnDisk(true).imageScaleType(ImageScaleType.EXACTLY)
			.bitmapConfig(Bitmap.Config.RGB_565).build();

	HomeFragment host;

	public LeftView(Context context) {
		this(context, null, 0);
	}

	public LeftView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public LeftView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		loginConfirm = new LoginConfrim(context);

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.home_left, this, true);

		photo = (ImageView) findViewById(R.id.sliding_photo);
		nickName = (TextView) findViewById(R.id.sliding_nickname);

		userActivity = (LinearLayout) findViewById(R.id.sliding_useractivity_container);
		messageCenter = (LinearLayout) findViewById(R.id.sliding_message_center);
		mv = (LinearLayout) findViewById(R.id.sliding_mv_container);
		group = (LinearLayout) findViewById(R.id.sliding_quan);
		setting = (LinearLayout) findViewById(R.id.sliding_setting_container);
		location = (LinearLayout) findViewById(R.id.sliding_location_container);

		userActivityTip = (View) findViewById(R.id.sliding_usercenter_tip);
		messageCenterTip = (TextView) findViewById(R.id.sliding_message_center_tip);
		mvTip = (View) findViewById(R.id.sliding_mv_tip);
		groupMegTip = (TextView) findViewById(R.id.sliding_circle_tip);
		settingTip = (View) findViewById(R.id.sliding_setting_tip);

		locationCity = (TextView) findViewById(R.id.sliding_city);

		userActivity.setOnClickListener(clickListener);
		messageCenter.setOnClickListener(clickListener);
		mv.setOnClickListener(clickListener);
		group.setOnClickListener(clickListener);
		setting.setOnClickListener(clickListener);
		photo.setOnClickListener(clickListener);
		location.setOnClickListener(clickListener);
		locationCity.setOnClickListener(clickListener);

	}

	/**
	 * 更新数据
	 */
	public void update() {
		// 头像的赋值
		UserProfile profile = App.USER;
		if (profile.hasPhoto()) {
			ImageLoader.getInstance().displayImage(profile.getPhotoUrl(2 * getResources().getDimensionPixelSize(R.dimen.userinfo_bg_head_photo_size)), photo, OPTIONS_HEAD_ROUND, App.CIRCLE_IMAGE_LOADING_LISTENER);
		}
		else {
			photo.setImageResource(R.drawable.default_home_left_photo);
		}
		nickName.setText(profile.getNickname());

		if (App.PREFS.isFirstToUserCenter()) {
			userActivityTip.setVisibility(View.VISIBLE);
		}
		else {
			userActivityTip.setVisibility(View.GONE);
		}

		if (App.PREFS.isFristToMV()) {
			mvTip.setVisibility(View.VISIBLE);
		}
		else {
			mvTip.setVisibility(View.GONE);
		}

		if (App.PREFS.isFirstToSetting()) {
			settingTip.setVisibility(View.VISIBLE);
		}
		else {
			settingTip.setVisibility(View.GONE);
		}

		updateLocationCity();

		updateMessageTip();
	}
	
	public void updateLocationCity() {
		String str = App.CACHE.read(Cache.CACHE_USER_CHOOSE_CITY_NAME);
		if (TextUtils.isEmpty(str)) {
			str = "北京";
		}
		locationCity.setText(str);
	}

	/**
	 * 更新偶们圈消息提醒提示
	 * 
	 * @param str
	 */
	public int updateMessageTip() {
		int oumencount = CircleMessage.queryNewCount(App.PREFS.getUid(), App.DB);
		if (oumencount > 0) {
			groupMegTip.setVisibility(View.VISIBLE);
			ViewGroup.LayoutParams params = groupMegTip.getLayoutParams();
			params.width = getResources().getDimensionPixelSize(R.dimen.default_big_gap);
			params.height = getResources().getDimensionPixelSize(R.dimen.default_big_gap);
			groupMegTip.setLayoutParams(params);
			groupMegTip.setText(String.valueOf(oumencount));
		}
		else {
			if (App.PREFS.isFirstToCircle()) {
				groupMegTip.setVisibility(View.VISIBLE);
				ViewGroup.LayoutParams params = groupMegTip.getLayoutParams();
				params.width = getResources().getDimensionPixelSize(R.dimen.padding_medium);
				params.height = getResources().getDimensionPixelSize(R.dimen.padding_medium);
				groupMegTip.setLayoutParams(params);
				groupMegTip.setText("");
			}
			else {
				groupMegTip.setVisibility(View.GONE);
			}
		}

		int msgCount = ActivityMessage.queryNewCount(App.PREFS.getUid(), App.DB);
		if (msgCount > 0) {
			messageCenterTip.setVisibility(View.VISIBLE);
			ViewGroup.LayoutParams params = messageCenterTip.getLayoutParams();
			params.width = getResources().getDimensionPixelSize(R.dimen.default_big_gap);
			params.height = getResources().getDimensionPixelSize(R.dimen.default_big_gap);
			messageCenterTip.setLayoutParams(params);
			messageCenterTip.setText(String.valueOf(msgCount));
		}
		else {
			messageCenterTip.setVisibility(View.GONE);
		}

		return msgCount + oumencount;
	}

	private final OnClickListener clickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (v == locationCity) {// 当前定位城市
				//TODO 
				Intent intent = new Intent(getContext(), CityChooseActivity.class);
				host.startActivity(intent);
			}
			else if (v == userActivity) {
				if (TextUtils.isEmpty(App.PREFS.getUserProfile())) {
					loginConfirm.openDialog();
					return;
				}
				App.PREFS.setFirstToUserCenter(false);
				userActivityTip.setVisibility(View.GONE);

				getContext().startActivity(new Intent(getContext(), UserActivity.class));
			}
			else if (v == messageCenter) {
				getContext().startActivity(new Intent(getContext(), PushActivityListActivity.class));

				ActivityMessage.updateAllRead(App.PREFS.getUid(), App.DB);

				getContext().sendBroadcast(MessageService.createRequestNotify(MessageService.TYPE_REFRESH_NEWS));
			}
			else if (v == photo) {//用户中心
				if (TextUtils.isEmpty(App.PREFS.getUserProfile())) {
					loginConfirm.openDialog();
					return;
				}
				host.getActivity().sendBroadcast(MessageService.createRequestNotify(MessageService.TYPE_USERINFO));

				host.startActivityForResult(new Intent(getContext(), UserCenterActivity.class), HomeFragment.REQUEST_CODE_USER_CENTER);
			}
			else if (v == mv) {
				host.getActivity().sendBroadcast(MessageService.createRequestNotify(MessageService.TYPE_USERINFO));

				if (TextUtils.isEmpty(App.PREFS.getUserProfile())) {
					//TODO 跳转到登录界面
					loginConfirm.openDialog();
					return;
				}

				App.PREFS.setFirstToMV(false);
				mvTip.setVisibility(View.GONE);

				getContext().startActivity(new Intent(getContext(), IndexActivity.class));
			}
			else if (v == group) {
				host.getActivity().sendBroadcast(MessageService.createRequestNotify(MessageService.TYPE_USERINFO));
				App.PREFS.setFirstToCircle(false);
				groupMegTip.setVisibility(View.GONE);

				Intent intent = new Intent(getContext(), CircleActivity.class);
				getContext().startActivity(intent);
			}
			else if (v == setting) {
				host.getActivity().sendBroadcast(MessageService.createRequestNotify(MessageService.TYPE_USERINFO));
				App.PREFS.setFirstToSetting(false);
				settingTip.setVisibility(View.GONE);

				getContext().startActivity(new Intent(getContext(), SettingActivity.class));
			}
			else if (v == location) {
				getContext().startActivity(new Intent(getContext(), LocationChangeActivity.class));
			}
		}
	};

}
