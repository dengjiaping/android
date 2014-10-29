package com.oumen.activity.detail;


import widget.viewpager.CirclePageIndicator;

import com.oumen.R;
import com.oumen.activity.message.DetailActivityMessage;
import com.oumen.android.App;
import com.oumen.message.ActivityMessage;
import com.oumen.widget.file.ImageData;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.view.ViewPager;
import android.text.SpannableStringBuilder;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

public class HuodongDetailHeader extends FrameLayout {
	private ViewPager viewpager;
	private TextView title;
	private TextView pingjia;
	private CirclePageIndicator indicator;
	private NewHeaderAdapter adapter;
	
	private int width = App.INT_UNSET;
	
	protected ActivityMessage data;

	public HuodongDetailHeader(Context context) {
		this(context, null, 0);
	}

	public HuodongDetailHeader(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public HuodongDetailHeader(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.new_huodong_detail_headerview, this, true);

		viewpager = (ViewPager) findViewById(R.id.header_viewpager);
		
		adapter = new NewHeaderAdapter();
		viewpager.setAdapter(adapter);
		
		title = (TextView) findViewById(R.id.header_title);
		pingjia = (TextView) findViewById(R.id.header_pingjia);
		
		indicator = (CirclePageIndicator) findViewById(R.id.indicator);
		indicator.setViewPager(viewpager);
		indicator.setFillColor(getResources().getColor(R.color.white));
//		indicator.setPageColor(getResources().getColor(R.color.default_bg));
		
		setViewDefaultHeight(width);
	}
	/**
	 * 设置viewpager高度
	 * @param width
	 * @param padding
	 */
	public void setViewDefaultHeight(int width) {
		ViewGroup.LayoutParams params = viewpager.getLayoutParams();
		params.width = width;
		params.height = params.width * 18 / 29;//Banner宽高比为29:18
		viewpager.setLayoutParams(params);
	}
	
	public void update(DetailActivityMessage provider) {
		title.setText(provider.getSenderName());
		
		int size = provider.getHuodongPics().size();
		if (size > 0) {
			if (size <= 1) {
				indicator.setVisibility(View.GONE);
			}
			else {
				indicator.setVisibility(View.VISIBLE);
			}
			for (int i = 0; i < size; i++) {
				ImageData data = new ImageData(provider.getHuodongPics().get(i));
				adapter.datas.add(data);
			}
			adapter.notifyDataSetChanged();
		}
	}
}
