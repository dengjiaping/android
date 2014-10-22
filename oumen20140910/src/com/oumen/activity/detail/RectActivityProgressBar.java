package com.oumen.activity.detail;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.oumen.R;
import com.oumen.tools.ELog;

public class RectActivityProgressBar extends FrameLayout {
	protected View viewIndicator;
	protected int progress;
	protected int max;
	protected int width;

	public RectActivityProgressBar(Context context) {
		this(context, null, 0);
	}

	public RectActivityProgressBar(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public RectActivityProgressBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.activity_progress_rect, this, true);
		
		viewIndicator = findViewById(R.id.indicator);
	}
	
	public void update() {
		if (viewIndicator.getVisibility() != View.VISIBLE) return;
		
		if (width == 0) {
			width = (int)(getWidth() - getResources().getDisplayMetrics().density * (5 * 2 + 30 * 2));
		}
		
		FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) viewIndicator.getLayoutParams();
		params.width = progress * width / max;
		viewIndicator.setLayoutParams(params);
	}

	public int getProgress() {
		return progress;
	}

	public void setProgress(int progress) {
		this.progress = progress;
		update();
	}

	public int getMax() {
		return max;
	}

	public void setMax(int max) {
		this.max = max;
		if (max == 0) {
			ELog.w("INVISIBLE");
			viewIndicator.setVisibility(View.INVISIBLE);
		}
		else if (viewIndicator.getVisibility() != View.VISIBLE) {
			ELog.w("VISIBLE");
			viewIndicator.setVisibility(View.VISIBLE);
		}
	}

}
