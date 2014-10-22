package com.oumen;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class TitleBar extends RelativeLayout {
	protected TextView txtTitle;
	protected Button btnLeft;
	protected Button txtRight;
	protected View tipView;
	protected RelativeLayout rootView;

	public TitleBar(Context context) {
		this(context, null, 0);
	}

	public TitleBar(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public TitleBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.titlebar, this, true);
		
		rootView = (RelativeLayout) findViewById(R.id.root);
		btnLeft = (Button) findViewById(R.id.left);
		txtRight = (Button) findViewById(R.id.right);
		txtTitle = (TextView) findViewById(R.id.titlebar_title);
		tipView = (View) findViewById(R.id.message_tip);
		tipView.setVisibility(View.GONE);
	}

	public Button getLeftButton() {
		return btnLeft;
	}
	
	public Button getRightButton() {
		return txtRight;
	}
	
	public TextView getTitle() {
		return txtTitle;
	}
	
	public void setBackgroundTransparent() {
		rootView.setBackgroundResource(R.color.transparent);
	}
	
	public void setTipViewVisible(int visible) {
		tipView.setVisibility(visible);
	}
}
