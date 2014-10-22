package com.oumen.activity.detail.cell;

import com.oumen.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
/**
 * 
 * 活动详情界面的子item
 *
 */
public class DefaultView extends RelativeLayout {
	private FrameLayout framlayout;
	private ImageView image;
	protected TextView name;
	private LinearLayout rootContainer;

	public DefaultView(Context context) {
		this(context, null, 0);
	}

	public DefaultView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public DefaultView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.huodong_detail_default_view, this, true);

		framlayout = (FrameLayout) findViewById(R.id.framlayout);
		image = (ImageView) findViewById(R.id.headphoto);
		name = (TextView) findViewById(R.id.name);
		rootContainer = (LinearLayout) findViewById(R.id.element_container);
	}
	
	public void setFramlayoutPaddingLeft(int left) {
		framlayout.setPadding(left, 0, 0, 0);
	}

	public void setImageRes(int resId) {
		image.setImageResource(resId);
	}

	public void setName(String name) {
		this.name.setText(name);
	}
	
	public void setName(int name) {
		this.name.setText(name);
	}

	/**
	 * 给容器中添加元素
	 */
	public void addViewToContainer(View view) {
		rootContainer.addView(view);
	}
}
