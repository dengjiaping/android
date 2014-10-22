package com.oumen.activity.detail.cell;

import com.oumen.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TextView;
/**
 * 带有一个textview的活动详情子item
 * 有监听事件
 *
 */
public class OneTexthasClickView extends DefaultView {
	private TextView content;

	public OneTexthasClickView(Context context) {
		this(context, null, 0);
	}

	public OneTexthasClickView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public OneTexthasClickView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		content = (TextView) inflater.inflate(R.layout.huodong_detail_textview_item, this, false);
		addViewToContainer(content);
	}

	public TextView getContentView() {
		return content;
	}
	public void setContent(String content) {
		this.content.setText(content);
	}
	
	public void setClickable(boolean clickable){
		content.setClickable(clickable);
	}
	
	public void setOnClickListener(OnClickListener clickListener) {
		content.setOnClickListener(clickListener);
	}
}
