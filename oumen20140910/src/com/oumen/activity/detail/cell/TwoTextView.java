package com.oumen.activity.detail.cell;

import com.oumen.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
/**
 * 带有两个个textview的活动详情子item
 *
 */
public class TwoTextView extends DefaultView {
	private TextView content_first,content_second;

	public TwoTextView(Context context) {
		this(context, null, 0);
	}

	public TwoTextView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public TwoTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.huodong_detail_textview4_item, this, false);
		
		content_first = (TextView) view.findViewById(R.id.left_content);
		content_second = (TextView) view.findViewById(R.id.right_content);
		
		addViewToContainer(view);
	}
	
	public void setLeftContent(String content) {
		this.content_first.setText(content);
	}
	
	public void setRightContent(String content) {
		this.content_second.setText(content);
	}
}
