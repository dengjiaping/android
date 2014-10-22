package com.oumen.activity.detail;


import widget.viewpager.CirclePageIndicator;

import com.oumen.R;
import com.oumen.activity.message.ActivityBean;
import com.oumen.android.App;
import com.oumen.message.ActivityMessage;
import com.oumen.tools.ELog;
import com.oumen.widget.file.ImageData;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.view.ViewPager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
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
	private Resources res ;
	
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

		res = context.getResources();
		width = res.getDisplayMetrics().widthPixels;
		
		viewpager = (ViewPager) findViewById(R.id.header_viewpager);
		
		adapter = new NewHeaderAdapter();
		viewpager.setAdapter(adapter);
		
		title = (TextView) findViewById(R.id.header_title);
		pingjia = (TextView) findViewById(R.id.header_pingjia);
		
		indicator = (CirclePageIndicator) findViewById(R.id.indicator);
		indicator.setViewPager(viewpager);
		indicator.setFillColor(getResources().getColor(R.color.detail_orange));
		
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
	
	public void update(ActivityBean provider) {
		title.setText(provider.getSenderName());
		SpannableStringBuilder builder;
		// TODO 
		if ( provider.getHaoPing() == 0 && provider.getChaPing() == 0 ) {
			builder = new SpannableStringBuilder();
			builder.append("商家好评:100%");
			ForegroundColorSpan colorSpen = new ForegroundColorSpan(getResources().getColor(R.color.detail_orange));
			builder.setSpan(colorSpen, "商家好评:".length(), ("商家好评:100%").length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			AbsoluteSizeSpan sizeSpan = new AbsoluteSizeSpan(18 * 2);
			builder.setSpan(sizeSpan, "商家好评:".length(), ("商家好评:100%").length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			pingjia.setText(builder);
		}
		else {
			ELog.i("provider.getHaoPing() = " + provider.getHaoPing() + ",provider.getChaPing() = " + provider.getChaPing());
			int temp = (provider.getHaoPing()/(provider.getHaoPing() + provider.getChaPing())) * 100;
			builder = new SpannableStringBuilder();
			builder.append("商家好评:"+ temp +"%");
			ForegroundColorSpan colorSpen = new ForegroundColorSpan(getResources().getColor(R.color.detail_orange));
			builder.setSpan(colorSpen, "商家好评:".length(), ("商家好评:"+ temp +"%").length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			AbsoluteSizeSpan sizeSpan = new AbsoluteSizeSpan(18 * 2);
			builder.setSpan(sizeSpan, "商家好评:".length(), ("商家好评:"+ temp +"%").length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			pingjia.setText(builder);
		}
		
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
