package com.oumen.activity;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.oumen.R;
import com.oumen.android.App;
import com.oumen.message.ActivityMessage;

public class SmallPushActivityListItem extends RelativeLayout implements PushActivityListActivity.ItemData {
	protected ImageView imgCover;
	protected TextView txtTitle;
	protected TextView txtTime;
	protected TextView txtPlace;
	
	protected ActivityMessage data;

	public SmallPushActivityListItem(Context context) {
		this(context, null, 0);
	}

	public SmallPushActivityListItem(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SmallPushActivityListItem(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.push_activity_list_item_small, this, true);
		
		imgCover = (ImageView) findViewById(R.id.cover);
		txtTitle = (TextView) findViewById(R.id.nav_title);
		txtTime = (TextView) findViewById(R.id.time);
		txtPlace = (TextView) findViewById(R.id.place);
	}

	public void update(ActivityMessage data) {
		this.data = data;
		
		ImageLoader.getInstance().displayImage(data.getPicUrl(), imgCover);
		txtTitle.setText(data.getTitle());
		txtTime.setText("开始时间:" + App.YYYY_MM_DD_CHINESE_FORMAT.format(data.getStartTime()));
		txtPlace.setText("地点:" + data.getAddress());
	}

	@Override
	public ActivityMessage getActivityMessage() {
		return data;
	}
}
