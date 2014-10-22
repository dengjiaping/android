package com.oumen.activity.detail.cell;

import com.oumen.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
/**
 * 带有三个textview的活动详情子item
 *
 */
public class ThireTextView extends DefaultView {
	private TextView content_first,content_second,content_third;

	public ThireTextView(Context context) {
		this(context, null, 0);
	}

	public ThireTextView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ThireTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.huodong_detail_textview3_item, this, false);
		
		content_first = (TextView) view.findViewById(R.id.first_content);
		content_second = (TextView) view.findViewById(R.id.second_content);
		content_third = (TextView) view.findViewById(R.id.third_content);
		
		addViewToContainer(view);
	}
	
	public void setFirstContent(String content) {
		this.content_first.setText(content);
	}
	
	public void setSecondContent(String content) {
		this.content_second.setText(content);
	}
	
	public void setThirdContent(String content) {
		this.content_third.setText(content);
	}

}
