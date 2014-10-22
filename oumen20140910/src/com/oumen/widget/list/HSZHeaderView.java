package com.oumen.widget.list;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.oumen.R;

public class HSZHeaderView extends FrameLayout {
	protected TextView txtTip;
	protected ProgressBar progress;
	
	protected int state;

	public HSZHeaderView(Context context) {
		this(context, null, 0);
	}

	public HSZHeaderView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public HSZHeaderView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.list_header_default, this, true);
		
		txtTip = (TextView) findViewById(R.id.tip);
		progress = (ProgressBar) findViewById(R.id.progress);
	}
	
	public void setState(int state) {
		this.state = state;
		
		if (state == HSZListViewAdapter.STATE_LOADING) {
			txtTip.setVisibility(View.GONE);
			progress.setVisibility(View.VISIBLE);
		}
		else {
			txtTip.setVisibility(View.VISIBLE);
			progress.setVisibility(View.GONE);
		}
	}
	
	public int getState() {
		return state;
	}

	public TextView getTip() {
		return txtTip;
	}
	
	public ProgressBar getProgress() {
		return progress;
	}
}
