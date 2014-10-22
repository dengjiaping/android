package com.oumen.widget;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

public class VerticalProgressBar extends FrameLayout {
	protected View viewIndicator;
	protected TextView txtTip;

	public VerticalProgressBar(Context context) {
		this(context, null, 0);
	}

	public VerticalProgressBar(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public VerticalProgressBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		setBackgroundColor(Color.WHITE);
		
		viewIndicator = new View(context);
		addView(viewIndicator, new LayoutParams(LayoutParams.MATCH_PARENT, 0, Gravity.BOTTOM));
		
		txtTip = new TextView(context);
		txtTip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
		txtTip.setTextColor(Color.BLACK);
		txtTip.setGravity(Gravity.CENTER);
		addView(txtTip, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
	}

	public void setTextColor(int color) {
		txtTip.setTextColor(color);
	}
	
	public void setTextSize(int size) {
		txtTip.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
	}
	
	public void setTipVisibility(int visibility) {
		txtTip.setVisibility(visibility);
	}
	
	public void setIndicatorColor(int color) {
		viewIndicator.setBackgroundColor(color);
	}
	
	public void update(final long current, final long total) {
		if (getHeight() <= 0) {
			//如果组件高度尚未初始化出来，则在可以拿到高度的时候更新UI
			post(new Runnable() {
				
				@Override
				public void run() {
					update(current, total);
				}
			});
		}
		else {
			LayoutParams params = (LayoutParams) viewIndicator.getLayoutParams();
			params.height = total == 0 ? 0 : (int) (current * getHeight() / total);
			viewIndicator.setLayoutParams(params);
			
			int progress = total == 0 ? 0 : (int) (current * 100 / total);
			txtTip.setText(progress + "%");
		}
	}
}
