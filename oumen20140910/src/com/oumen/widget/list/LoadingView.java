package com.oumen.widget.list;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class LoadingView extends LinearLayout {
	protected ProgressBar pgs;
	protected TextView tip;

	public LoadingView(Context context) {
		this(context, null, 0);
	}

	public LoadingView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public LoadingView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		setOrientation(HORIZONTAL);
		
		float density = getResources().getDisplayMetrics().density;
		
		LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.CENTER_VERTICAL;
		params.rightMargin = (int)(10 * density);
		
		pgs = new ProgressBar(context, null, android.R.attr.progressBarStyleSmallInverse);
		pgs.setIndeterminate(true);
		addView(pgs, params);
		
		params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.CENTER_VERTICAL;
		
		tip = new TextView(context);
		tip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
		tip.setTextColor(Color.BLACK);
		tip.setText("正在加载");
		addView(tip, params);
	}
}
