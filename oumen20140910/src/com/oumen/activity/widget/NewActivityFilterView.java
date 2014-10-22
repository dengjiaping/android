package com.oumen.activity.widget;

import java.util.ArrayList;

import com.oumen.R;
import com.oumen.activity.HuodongTypeUtil.FITLER_TYPE;
import com.oumen.widget.calander.CalanderView;
import com.oumen.widget.calander.DateWidgetDayCell;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

public class NewActivityFilterView extends LinearLayout {
	private CalanderView calanderview;
	private Button btnClearDate;
	private LinearLayout orderContainer, ageContainer;

	private TextView[] orderViews = new TextView[3];
	private TextView[] ageViews = new TextView[7];

	private String[] orderList = new String[] {
			getResources().getString(R.string.activity_filter_order_default),
			getResources().getString(R.string.activity_filter_order_newest),
			getResources().getString(R.string.activity_filter_order_hot) };
	
	private String[] ageList = new String[] {
			getResources().getString(R.string.activity_filter_default),
			getResources().getString(R.string.activity_filter_beiyun),
			getResources().getString(R.string.activity_filter_huaiyun),
			getResources().getString(R.string.activity_filter_range1),
			getResources().getString(R.string.activity_filter_range2),
			getResources().getString(R.string.activity_filter_range3),
			getResources().getString(R.string.activity_filter_range4)};

	private Context context;

	private OnClickListener clickListener = null;

	public NewActivityFilterView(Context context) {
		this(context, null, 0);
	}

	public NewActivityFilterView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public NewActivityFilterView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.huodong_filter_view, this, true);

		this.context = context;

		calanderview = (CalanderView) findViewById(R.id.calander);
		btnClearDate = (Button) findViewById(R.id.clear_date);

		orderContainer = (LinearLayout) findViewById(R.id.order_container);
		
		ageContainer = (LinearLayout) findViewById(R.id.age_container);
		
		calanderview.setVisibility(View.GONE);
		btnClearDate.setVisibility(View.GONE);
		calanderview.setVisibility(View.GONE);
		ageContainer.setVisibility(View.GONE);

	}

	public void setDateCellClickListener(DateWidgetDayCell.OnItemClick mOnDayCellClick) {
		calanderview.setCellClickListener(mOnDayCellClick);
	}

	public void updateCurrentDate(DateWidgetDayCell item) {
		calanderview.updateCurrentDate(item);
	}
	
	public void clearDateSelection() {
		calanderview.clearDateSelection();
	}

	public void setViewOnClickListener(OnClickListener clickListener) {
		this.clickListener = clickListener;
		btnClearDate.setOnClickListener(clickListener);
	}

	public Button getClearDateButton() {
		return btnClearDate;
	}

	public void initData() {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		for (int i = 0; i < orderViews.length; i++) {
			orderViews[i] = (TextView) inflater.inflate(R.layout.default_listview_item, this, false);
			
			if (i == 0) {
				orderViews[i].setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.confirm_huodong), null);
			}
			else {
				orderViews[i].setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, null, null);
			}
			
			orderViews[i].setText(orderList[i]);
			orderViews[i].setOnClickListener(clickListener);
			orderContainer.addView(orderViews[i]);
		}
		
		for (int i = 0; i < ageViews.length; i++) {
			ageViews[i] = (TextView) inflater.inflate(R.layout.default_listview_item, this, false);
			if (i == 0) {
				ageViews[i].setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.confirm_huodong), null);
			}
			else {
				ageViews[i].setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, null, null);
			}
			ageViews[i].setText(ageList[i]);
			ageViews[i].setOnClickListener(clickListener);
			ageContainer.addView(ageViews[i]);
		}
	}

	private FITLER_TYPE type ;
	public void update(FITLER_TYPE type) {
		this.type = type;
		
		if (type.equals(FITLER_TYPE.DATE)) {
			calanderview.setVisibility(View.VISIBLE);
			btnClearDate.setVisibility(View.VISIBLE);
			ageContainer.setVisibility(View.GONE);
			orderContainer.setVisibility(View.GONE);
		}
		else if (type.equals(FITLER_TYPE.AGE)) {
			calanderview.setVisibility(View.GONE);
			btnClearDate.setVisibility(View.GONE);
			ageContainer.setVisibility(View.VISIBLE);
			orderContainer.setVisibility(View.GONE);
		}
		else {
			calanderview.setVisibility(View.GONE);
			btnClearDate.setVisibility(View.GONE);
			ageContainer.setVisibility(View.GONE);
			orderContainer.setVisibility(View.VISIBLE);
		}
	}
	
	public int updateView(String str) {
		if (type.equals(FITLER_TYPE.ORDER)) { // 排序
			int current = 0;
			for (int i = 0; i < orderList.length ; i++) {
				if (orderList[i].equals(str)) {
					current = i;
					orderViews[i].setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.confirm_huodong), null);
				}
				else {
					orderViews[i].setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, null, null);
				}
			}
			return current;
		}
		else if (type.equals(FITLER_TYPE.AGE)) {// 年龄
			int current = 0;
			for (int i = 0; i < ageList.length ; i++) {
				if (ageList[i].equals(str)) {
					current = i;
					ageViews[i].setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.confirm_huodong), null);
				}
				else {
					ageViews[i].setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, null, null);
				}
			}
			return current;
		}
		else {
			return 0;
		}
	}

	private class AdapterImp extends BaseAdapter {
		public final ArrayList<String> data = new ArrayList<String>();

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView item;
			if (convertView == null) {
				item = new TextView(context);
				LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				item = (TextView) inflater.inflate(R.layout.default_listview_item, parent, false);
			}
			else {
				item = (TextView) convertView;
			}
			item.setOnClickListener(clickListener);
			item.setText(data.get(position));

			return item;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public String getItem(int position) {
			return data.get(position);
		}

		@Override
		public int getCount() {
			return data.size();
		}

	}
}
