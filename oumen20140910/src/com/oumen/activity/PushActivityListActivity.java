package com.oumen.activity;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.oumen.R;
import com.oumen.TitleBar;
import com.oumen.activity.detail.HuoDongDetailActivity;
import com.oumen.activity.detail.cell.ActivityDetailHeaderView;
import com.oumen.activity.detail.cell.CircleCornerImageHasDownloadView;
import com.oumen.android.App;
import com.oumen.android.BaseActivity;
import com.oumen.home.LoginConfrim;
import com.oumen.message.ActivityMessage;

public class PushActivityListActivity extends BaseActivity {
	private LinearLayout list;
	private TitleBar titlebar;
	private Button btnLeft;

//	private ActivityDetailHeaderView bigItem;
	private BigPushActivityListItem bigItem;

	private LoginConfrim loginConfrim;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.push_activity_list);

		loginConfrim = new LoginConfrim(this);

		list = (LinearLayout) findViewById(R.id.list);

		titlebar = (TitleBar) findViewById(R.id.titlebar);
		titlebar.getTitle().setText(R.string.msg_recommend_title_default);
		titlebar.getRightButton().setVisibility(View.GONE);
		btnLeft = titlebar.getLeftButton();

		btnLeft.setOnClickListener(clickListener);

		loadData();
	}

	private void loadData() {
		int year = 0, month = 0, day = 0;
		long time = 0;
		LinearLayout lastGroupContainer = null;
		List<ActivityMessage> tmp = ActivityMessage.query(App.PREFS.getUid(), false, true, App.DB);
		Calendar date = Calendar.getInstance();
		for (ActivityMessage i : tmp) {
			date.setTime(i.getTimestamp());
			int y = date.get(Calendar.YEAR), m = date.get(Calendar.MONTH), d = date.get(Calendar.DAY_OF_MONTH);
			if (year != y || month != m || day != d) {
				list.addView(createTimeItem(i.getStartTime()));
				year = y;
				month = m;
				day = d;
			}
			if (time == i.getTimestamp().getTime()) {// 如果时间戳一样，就认为是同一批过来的
				View divider = new View(this);
				divider.setBackgroundResource(R.color.divider);
				lastGroupContainer.addView(divider, LayoutParams.MATCH_PARENT, 1);

				SmallPushActivityListItem item = new SmallPushActivityListItem(this);
				item.setOnClickListener(clickListener);
				item.update(i);
				lastGroupContainer.addView(item);
			}
			else {
				lastGroupContainer = createListGroupContainer();
				lastGroupContainer.removeAllViews();
				list.addView(lastGroupContainer);

//				bigItem = new ActivityDetailHeaderView(this);
				bigItem = new BigPushActivityListItem(this);
				bigItem.setImageHasClickListener(false, clickListener);
				bigItem.update(i);
				bigItem.setViewHeight();
				lastGroupContainer.addView(bigItem);
			}
			time = i.getTimestamp().getTime();
		}
	}

	private View createTimeItem(Date time) {
		int height = (int) (30 * getResources().getDisplayMetrics().density);
		TextView v = new TextView(this);
		v.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.text_small));
		v.setTextColor(getResources().getColor(R.color.white));
		v.setGravity(Gravity.CENTER);
		v.setText(App.YYYY_MM_DD_CHINESE_FORMAT.format(time));
		v.setBackgroundResource(R.drawable.group_chat_time_bg_default);
		int dim = getResources().getDimensionPixelSize(R.dimen.padding_micro);
		v.setPadding(dim, dim, dim, dim);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, height);
		params.gravity = Gravity.CENTER;
		params.bottomMargin = getResources().getDimensionPixelSize(R.dimen.default_gap);
		params.topMargin = getResources().getDimensionPixelSize(R.dimen.default_gap);
		v.setLayoutParams(params);
		return v;
	}

	private LinearLayout createListGroupContainer() {
		LinearLayout container = new LinearLayout(this);
		container.setOrientation(LinearLayout.VERTICAL);
		container.setBackgroundResource(R.drawable.push_activity_list_item_bg);
		container.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		return container;
	}

	private final View.OnClickListener clickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			if (v instanceof ItemData) {
				if (TextUtils.isEmpty(App.PREFS.getUserProfile())) {
					loginConfrim.openDialog();
					return;
				}

				ItemData itemData = (ItemData) v;
				ActivityMessage msg = itemData.getActivityMessage();

				Intent intent = new Intent(mBaseApplication, HuoDongDetailActivity.class);
				intent.putExtra(HuoDongDetailActivity.INTENT_KEY_ACTIVITY_ID, msg.getId());
				startActivity(intent);
			}
			else if (v.getId() == R.id.left) {
				finish();
			}
			else if (v instanceof CircleCornerImageHasDownloadView) {

				if (TextUtils.isEmpty(App.PREFS.getUserProfile())) {
					loginConfrim.openDialog();
					return;
				}

				ActivityMessage msg = bigItem.getActivityMessage();
				Intent intent = new Intent(mBaseApplication, HuoDongDetailActivity.class);
				intent.putExtra(HuoDongDetailActivity.INTENT_KEY_ACTIVITY_ID, msg.getId());
				startActivity(intent);
			}
		}
	};

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	public interface ItemData {
		ActivityMessage getActivityMessage();
	}
}
