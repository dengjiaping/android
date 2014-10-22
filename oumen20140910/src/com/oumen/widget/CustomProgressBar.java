package com.oumen.widget;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.oumen.R;
import com.oumen.tools.ELog;

public class CustomProgressBar extends FrameLayout {
	protected ProgressBar indeterminateProgress;
	
	protected LinearLayout indicatorProgress;
	protected FrameLayout background;
	protected View foreground;
	protected TextView txtPercent;
	
	protected int indicatorWidth;
	protected int progress;
	protected int max;

	public CustomProgressBar(Context context) {
		this(context, null, 0);
	}

	public CustomProgressBar(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public CustomProgressBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		setBackgroundResource(R.color.transparent_half);
		
		indeterminateProgress = new ProgressBar(context);
		indeterminateProgress.setIndeterminate(true);
		addView(indeterminateProgress, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER));
		indeterminateProgress.setVisibility(View.GONE);
		
		indicatorProgress = new LinearLayout(context);
		indicatorProgress.setOrientation(LinearLayout.VERTICAL);
		indicatorProgress.setGravity(Gravity.CENTER);
		indicatorProgress.setVisibility(View.GONE);
		addView(indicatorProgress, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER));
		
		txtPercent = new TextView(context);
		txtPercent.setTextColor(Color.BLACK);
		indicatorProgress.addView(txtPercent, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		
		background = new FrameLayout(context);
		indicatorProgress.addView(background, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER));
		foreground = new View(context);
		background.addView(foreground, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT, Gravity.CENTER_VERTICAL));
	}
	
	public void update() {
		if (background.getVisibility() != View.VISIBLE) return;
		
		FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) foreground.getLayoutParams();
		params.width = progress * indicatorWidth / max;
		foreground.setLayoutParams(params);
		
		int precent = progress * 100 / max;
		txtPercent.setText(precent + "%");
		ELog.i("Progress:" + progress + "/" + max + " Width:" + params.width + "/" + indicatorWidth);
	}

	public int getProgress() {
		return progress;
	}

	public void setProgress(int progress) {
		this.progress = progress;
		if (indicatorWidth > 0) {
			update();
		}
	}

	public int getMax() {
		return max;
	}

	public void setMax(int max) {
		this.max = max;
		if (max == 0) {
			ELog.w("INVISIBLE");
			foreground.setVisibility(View.INVISIBLE);
		}
		else if (foreground.getVisibility() != View.VISIBLE) {
			ELog.w("VISIBLE");
			foreground.setVisibility(View.VISIBLE);
		}
	}

	public void setIndeterminate(boolean indeterminate) {
		if (indeterminate) {
			indeterminateProgress.setVisibility(View.VISIBLE);
			indicatorProgress.setVisibility(View.GONE);
		}
		else {
			indeterminateProgress.setVisibility(View.GONE);
			indicatorProgress.setVisibility(View.VISIBLE);
		}
	}
	
	public void setPercentColor(int color) {
		txtPercent.setTextColor(color);
	}
	
	public void setPercentSize(int size) {
		txtPercent.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
	}
	
	public void setPercentVisibility(int visibility) {
		txtPercent.setVisibility(visibility);
	}
	
	public void setIndicatorPadding(int left, int top, int right, int bottom) {
		background.setPadding(left, top, right, bottom);
	}
	
	public void setIndicatorHeight(int height) {
		LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) background.getLayoutParams();
		params.height = height;
		background.setLayoutParams(params);
	}
	
	public void setIndicatorBackground(int resid) {
		background.setBackgroundResource(resid);
	}
	
	public void setIndicatorBackgroundColor(int color) {
		background.setBackgroundColor(color);
	}
	
	public void setIndicatorForeground(int resid) {
		foreground.setBackgroundResource(resid);
	}
	
	public void setIndicatorForegroundColor(int color) {
		foreground.setBackgroundColor(color);
	}
	
	public void setIndicatorWidth(int width) {
		indicatorWidth = width;
	}
}
