package com.oumen.circle;

import com.oumen.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class MoreView extends LinearLayout {
	protected Button btnEnjoy;
	protected Button btnComment;
	protected Button btnShare;

	public MoreView(Context context) {
		this(context, null, 0);
	}

	public MoreView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public MoreView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.circle_more, this, true);
		
		btnEnjoy = (Button) findViewById(R.id.enjoy);
		btnComment = (Button) findViewById(R.id.comment);
		btnShare = (Button) findViewById(R.id.share);
	}

	public void setButtonClickListener(View.OnClickListener listener) {
		btnEnjoy.setOnClickListener(listener);
		btnComment.setOnClickListener(listener);
		btnShare.setOnClickListener(listener);
	}
	
	public void setData(Object data) {
		setTag(data);
		btnEnjoy.setTag(data);
		btnComment.setTag(data);
		btnShare.setTag(data);
	}
	
	public void setEnjoyText(int textResId, int iconResId, int bgResId) {
		btnEnjoy.setText(textResId);
		btnEnjoy.setCompoundDrawablesWithIntrinsicBounds(iconResId, 0, 0, 0);
		btnEnjoy.setBackgroundResource(bgResId);
	}
}
