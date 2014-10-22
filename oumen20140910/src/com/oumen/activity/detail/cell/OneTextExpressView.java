package com.oumen.activity.detail.cell;

import com.oumen.R;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TextView;
/**
 * 带有一个textview的活动详情子item
 * 只用于显示
 *
 */
public class OneTextExpressView extends DefaultView {
	private TextView content;

	public OneTextExpressView(Context context) {
		this(context, null, 0);
	}

	public OneTextExpressView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public OneTextExpressView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		content = (TextView) inflater.inflate(R.layout.huodong_detail_textview2_item, this, false);
		addViewToContainer(content);
	}
	
	public void setContent(String str) {
		content.setText(str);
	}
	
	public void setContent(SpannableStringBuilder builder) {
		content.setText(builder);
	}
	public TextView getContentView() {
		return content;
	}

}
