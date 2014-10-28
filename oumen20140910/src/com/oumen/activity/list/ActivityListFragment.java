package com.oumen.activity.list;

import java.util.ArrayList;
import java.util.Date;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.oumen.FloatWindowController;
import com.oumen.R;
import com.oumen.TitleBar;
import com.oumen.activity.HuodongTypeUtil;
import com.oumen.activity.HuodongTypeUtil.FITLER_TYPE;
import com.oumen.activity.HuodongTypeUtil.AgeType;
import com.oumen.activity.HuodongTypeUtil.OrderType;
import com.oumen.activity.detail.HuoDongDetailActivity;
import com.oumen.activity.message.BaseActivityMessage;
import com.oumen.activity.widget.NewActivityFilterView;
import com.oumen.android.App;
import com.oumen.android.BaseFragment;
import com.oumen.android.UserProfile;
import com.oumen.base.Cache;
import com.oumen.home.LoginConfrim;
import com.oumen.tools.ELog;
import com.oumen.widget.calander.DateWidgetDayCell;
import com.oumen.widget.refushlist.AbOnListViewListener;
import com.oumen.widget.refushlist.AbPullListView;

/**
 * 活动列表界面
 * 
 */
public class ActivityListFragment extends BaseFragment implements View.OnTouchListener, ActivityHostProvider {
	public static final String HUODONG_TYPE = "huodong_type";

	public static final int RESULT_CODE_FROM_DETAIL = 2;

	private final String CACHE_KEY = "activities_";

	private final String STATE_USERPROFILE = "userprofile";

	private final ActivityAdapter adapter = new ActivityAdapter();

	//标题行控件
	private TitleBar titleBar;
	private Button btnLeft;
	private Button btnRight;
	private TextView tvTitle;

	private LinearLayout filterContainer;
	private FrameLayout orderContainer, ageContainer, dateContainer;
	// 筛选行
	private TextView tvOrder, tvAge, tvDate;

	private AbPullListView lstView;

	private NewActivityFilterView viewFilter;
	private FloatWindowController controllerFloat;

	private Animation animIn;
	private Animation animOut;

	// ====筛选的所有控件===
	/*
	 * 活动大类型
	 * 0,户外;1：室内;2：亲子，3：线上
	 */
	private int type = App.INT_UNSET;

	private FITLER_TYPE filter_type = FITLER_TYPE.OTHER; // 筛选排序类型（顺序，年龄，日期）
	/*
	 * 筛选条件之年龄
	 * 活动类型(0:备胎 1:怀孕 2:0-1岁 3:1-3岁 4:3-6岁 5:6岁以上)
	 */
	private AgeType ageType = AgeType.AGE_DEFAULT;
	/*
	 * 筛选条件之日期
	 * 日期格式：（0000-00-00）
	 */
	private String filterTime = null;
	/*
	 * 筛选条件之排序
	 * (默认，最新，人气)
	 */
	private OrderType orderType = OrderType.DEFAULT;

	private int currentPage = 1;

	private String[] huodongTypeList;

	private LoginConfrim loginConfrim;

	private HuodongListHttpController controller;
	
	private boolean firstFlag = true;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			App.USER.copyFrom((UserProfile) savedInstanceState.getSerializable(STATE_USERPROFILE));
		}
		Bundle args = getArguments();
		type = args.getInt(HUODONG_TYPE);

		animIn = AnimationUtils.loadAnimation(getActivity(), R.anim.top_in);
		animOut = AnimationUtils.loadAnimation(getActivity(), R.anim.top_out);
		huodongTypeList = getResources().getStringArray(R.array.huodong_type);

		loginConfrim = new LoginConfrim(getActivity());

		controller = new HuodongListHttpController(this, handler);

		if(firstFlag) {
			firstFlag = false;
			showProgressDialog();
		}
		
		App.THREAD.execute(new Runnable() {
			
			@Override
			public void run() {
				String key = getCacheKey();
				String cache = App.CACHE.read(key);
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
		View view = inflater.inflate(R.layout.fragment_huodong_list, container, false);
		titleBar = (TitleBar) view.findViewById(R.id.titlebar);

		FrameLayout layerLayout = (FrameLayout) view.findViewById(R.id.layer);
		tvTitle = titleBar.getTitle();
		btnLeft = titleBar.getLeftButton();
		btnRight = titleBar.getRightButton();
		btnRight.setText(R.string.search);
		btnRight.setVisibility(View.GONE);

		filterContainer = (LinearLayout) view.findViewById(R.id.filter_container);
		orderContainer = (FrameLayout) view.findViewById(R.id.order_container);
		ageContainer = (FrameLayout) view.findViewById(R.id.age_container);
		dateContainer = (FrameLayout) view.findViewById(R.id.date_container);

		tvOrder = (TextView) view.findViewById(R.id.order);
		tvAge = (TextView) view.findViewById(R.id.age);
		tvDate = (TextView) view.findViewById(R.id.date);
		updateFilterViewState(FITLER_TYPE.OTHER);

		ViewGroup.LayoutParams params = orderContainer.getLayoutParams();
		params.width = App.SCREEN_WIDTH / 3;
		orderContainer.setLayoutParams(params);
		ageContainer.setLayoutParams(params);
		dateContainer.setLayoutParams(params);

		orderContainer.setOnClickListener(clickListener);
		ageContainer.setOnClickListener(clickListener);
		dateContainer.setOnClickListener(clickListener);

		viewFilter = new NewActivityFilterView(container.getContext());
		viewFilter.setDateCellClickListener(mOnDayCellClick);
		viewFilter.setViewOnClickListener(clickListener);
		viewFilter.initData();
		controllerFloat = new FloatWindowController(layerLayout);
		layerLayout.setOnTouchListener(this);
		controllerFloat.setTargetView(viewFilter);

		btnLeft.setOnClickListener(clickListener);
		tvTitle.setOnClickListener(clickListener);
		btnRight.setOnClickListener(clickListener);

		// ------------ListView-------------
		lstView = (AbPullListView) view.findViewById(R.id.refreshablelistview);
		lstView.setBackgroundColor(getResources().getColor(R.color.white));
		lstView.setDivider(new ColorDrawable(getResources().getColor(R.color.transparent)));
		lstView.setDividerHeight(0);
		TextView emptyView = (TextView) view.findViewById(R.id.empty_view);
		emptyView.setText(getResources().getString(R.string.activity_list_empty));
		lstView.setEmptyView(emptyView);

		lstView.getHeaderView().setHeaderProgressBarDrawable(this.getResources().getDrawable(R.drawable.progress_circular));
		lstView.getFooterView().setFooterProgressBarDrawable(this.getResources().getDrawable(R.drawable.progress_circular));
		lstView.setAdapter(adapter);
		lstView.setSelector(android.R.color.transparent);
		lstView.setAbOnListViewListener(listviewListener);

		return view;
	}

	private AbOnListViewListener listviewListener = new AbOnListViewListener() {

		@Override
		public void onRefresh() {
			controller.obtainActivities(type, ageType.code(), orderType.code(), filterTime, 1);
		}

		@Override
		public void onLoadMore() {
			int page = adapter.isEmpty() ? 1 : currentPage + 1;
			controller.obtainActivities(type, ageType.code(), orderType.code(), filterTime, page);
		}
	};

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		
		if (type == App.INT_UNSET) {
			tvTitle.setText(R.string.filter);
			filterContainer.setVisibility(View.VISIBLE);
		}
		else if (type == HuodongTypeUtil.CONDITION_FUZZY_SEARCH) {
			tvTitle.setText(App.PREFS.getLastHistorySearch());
			filterContainer.setVisibility(View.GONE);
		}
		else {
			tvTitle.setText(huodongTypeList[type]);
			filterContainer.setVisibility(View.VISIBLE);
		}
		
		handler.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				initData();
			}
		}, 500);
	}

	public void initData() {
		if (!adapter.data.isEmpty() && firstFlag) {
			adapter.notifyDataSetChanged();
			dismissProgressDialog();
		}
		controller.obtainActivities(type, ageType.code(), orderType.code(), filterTime, 1);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

	private String getCacheKey() {
		// TODO 此处需要增加参数
		if (TextUtils.isEmpty(App.CACHE.read(Cache.CACHE_USER_CHOOSE_CITY_NAME))) {
			return CACHE_KEY + type;
		}
		else {
			return CACHE_KEY + type + App.CACHE.read(Cache.CACHE_USER_CHOOSE_CITY_NAME);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putSerializable(STATE_USERPROFILE, App.USER);
		super.onSaveInstanceState(outState);
	}

	private void updateFilterViewState(FITLER_TYPE type) {
		if (type.equals(FITLER_TYPE.ORDER)) {
			orderContainer.setBackgroundResource(R.drawable.huodong_choose_bg);
			ageContainer.setBackgroundResource(R.drawable.huodong_choose_default_bg);
			dateContainer.setBackgroundResource(R.drawable.huodong_choose_default_bg);

			tvOrder.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.pulldown_btn_on), null);
			tvAge.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.pulldown_btn), null);
			tvDate.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.pulldown_btn), null);
		}
		else if (type.equals(FITLER_TYPE.AGE)) {
			ageContainer.setBackgroundResource(R.drawable.huodong_choose_bg);
			orderContainer.setBackgroundResource(R.drawable.huodong_choose_default_bg);
			dateContainer.setBackgroundResource(R.drawable.huodong_choose_default_bg);

			tvAge.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.pulldown_btn_on), null);
			tvOrder.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.pulldown_btn), null);
			tvDate.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.pulldown_btn), null);
		}
		else if (type.equals(FITLER_TYPE.DATE)) {
			dateContainer.setBackgroundResource(R.drawable.huodong_choose_bg);
			ageContainer.setBackgroundResource(R.drawable.huodong_choose_default_bg);
			orderContainer.setBackgroundResource(R.drawable.huodong_choose_default_bg);

			tvDate.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.pulldown_btn_on), null);
			tvAge.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.pulldown_btn), null);
			tvOrder.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.pulldown_btn), null);
		}
		else {
			dateContainer.setBackgroundResource(R.drawable.huodong_choose_default_bg);
			ageContainer.setBackgroundResource(R.drawable.huodong_choose_default_bg);
			orderContainer.setBackgroundResource(R.drawable.huodong_choose_default_bg);

			tvDate.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.pulldown_btn), null);
			tvAge.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.pulldown_btn), null);
			tvOrder.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.pulldown_btn), null);
		}
	}

	@Override
	public boolean onBackPressed() {
		if (isPopupWindowShown()) {
			togglePopupWindow();
			return true;
		}

		return super.onBackPressed();
	}

	public boolean isPopupWindowShown() {
		if (controllerFloat == null)
			return false;
		return controllerFloat.isShown() || controllerFloat.isPlaying();
	}

	public boolean isPopupWindowPlaying() {
		if (controllerFloat == null)
			return false;
		return controllerFloat.isPlaying();
	}

	public void togglePopupWindow() {
		if (controllerFloat == null)
			return;
		if (controllerFloat.isPlaying())
			return;

		controllerFloat.startAnimation(controllerFloat.isShown() ? animOut : animIn);
	}

	public void resetPopupWindow() {
		controllerFloat.reset();
	}

	@Override
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
					String key = getCacheKey();
					App.CACHE.save(key, json.toString());
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
					}

					adapter.notifyDataSetChanged();
					lstView.stopRefresh();
					lstView.stopLoadMore();
				}
				dismissProgressDialog();
				break;
			case HuodongListHttpController.HANDLER_NONE_NETWORK:
				lstView.stopRefresh();
				lstView.stopLoadMore();
				if (adapter.isEmpty()) {
//					emptyContainer.setVisibility(View.VISIBLE);
//					emptyView.setText(getResources().getString(R.string.err_network_invalid));
				}
				else {
					Toast.makeText(lstView.getContext(), getResources().getString(R.string.err_network_invalid), Toast.LENGTH_SHORT).show();
				}
				break;
		}
		return false;
	}

	private DateWidgetDayCell.OnItemClick mOnDayCellClick = new DateWidgetDayCell.OnItemClick() {
		public void OnClick(DateWidgetDayCell item) {

			viewFilter.updateCurrentDate(item);

			filter_type = FITLER_TYPE.OTHER;
			updateFilterViewState(filter_type);
			togglePopupWindow();
			filterTime = App.YYYY_MM_DD_FORMAT.format(new Date(item.getDate().getTimeInMillis()));
			ELog.i("得到的时间" + filterTime);
			tvDate.setText(filterTime);
			initData();
		}
	};

	private final View.OnClickListener clickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if (v == btnLeft) {

				getActivity().finish();
			}
			else if (v instanceof Item) {
				if (TextUtils.isEmpty(App.PREFS.getUserProfile())) {
					//TODO 跳转到登录界面
					loginConfrim.openDialog();
					return;
				}
				BaseActivityMessage item = (BaseActivityMessage) v.getTag();
				Intent intent = new Intent(getActivity(), HuoDongDetailActivity.class);
				intent.putExtra(HuoDongDetailActivity.INTENT_KEY_ACTIVITY_ID, item.getActivityId());
				getActivity().startActivityForResult(intent, RESULT_CODE_FROM_DETAIL);

			}
			else if (v == orderContainer) {
				updateFilterViewState(FITLER_TYPE.ORDER);
				if (filter_type.equals(FITLER_TYPE.ORDER)) {
					filter_type = FITLER_TYPE.OTHER;
				}
				else if (filter_type.equals(FITLER_TYPE.OTHER)) {
					filter_type = FITLER_TYPE.ORDER;
				}
				else {
					filter_type = FITLER_TYPE.ORDER;
					viewFilter.update(filter_type);
					return;
				}

				viewFilter.update(filter_type);
				togglePopupWindow();
			}
			else if (v == ageContainer) {
				updateFilterViewState(FITLER_TYPE.AGE);

				if (filter_type.equals(FITLER_TYPE.AGE)) {
					filter_type = FITLER_TYPE.OTHER;
				}
				else if (filter_type.equals(FITLER_TYPE.OTHER)) {
					filter_type = FITLER_TYPE.AGE;
				}
				else {
					filter_type = FITLER_TYPE.AGE;
					viewFilter.update(filter_type);
					return;
				}

				viewFilter.update(filter_type);
				togglePopupWindow();
			}
			else if (v == dateContainer) {
				updateFilterViewState(FITLER_TYPE.DATE);

				if (filter_type.equals(FITLER_TYPE.DATE)) {
					filter_type = FITLER_TYPE.OTHER;
				}
				else if (filter_type.equals(FITLER_TYPE.OTHER)) {
					filter_type = FITLER_TYPE.DATE;
				}
				else {
					filter_type = FITLER_TYPE.DATE;
					viewFilter.update(filter_type);
					return;
				}

				viewFilter.update(filter_type);
				togglePopupWindow();
			}
			else if (v == viewFilter.getClearDateButton()) {
				// TODO 
				filter_type = FITLER_TYPE.OTHER;
				togglePopupWindow();
				updateFilterViewState(filter_type);

				filterTime = null;
				viewFilter.clearDateSelection();
				tvDate.setText("不限日期");
				initData();
			}
			else if (v instanceof TextView) {
				String str = ((TextView) v).getText().toString();
				ELog.i("得到的str = " + str);
				int currentId = viewFilter.updateView(str);
				ELog.i("currentId = " + currentId + ",filter_type = " + filter_type.name());

				updateData(str, filter_type);

				filter_type = FITLER_TYPE.OTHER;
				togglePopupWindow();
				updateFilterViewState(filter_type);

				initData();
			}
		}
	};

	private void updateData(String name, FITLER_TYPE filter_type) {
		if (filter_type.equals(FITLER_TYPE.AGE)) {
			ageType = AgeType.parseType(name);
			tvAge.setText(name);
		}
		else if (filter_type.equals(FITLER_TYPE.ORDER)) {
			orderType = OrderType.parseType(name);
			tvOrder.setText(name);
		}
	}

	private class ActivityAdapter extends BaseAdapter {
		
		public ArrayList<BaseActivityMessage> data = new ArrayList<BaseActivityMessage>();

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
			Item item;
			if (convertView == null) {
				item = new Item(parent.getContext());
				item.setOnClickListener(clickListener);
			}
			else {
				item = (Item) convertView;
			}
			BaseActivityMessage itemData = null;
			synchronized (this) {
				itemData = getItem(position);
			}
			item.setTag(itemData);
			item.update(itemData);
			return item;
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (!isPopupWindowPlaying()) {
			togglePopupWindow();
			filter_type = FITLER_TYPE.OTHER;
			updateFilterViewState(filter_type);
		}
		return true;
	}
}
