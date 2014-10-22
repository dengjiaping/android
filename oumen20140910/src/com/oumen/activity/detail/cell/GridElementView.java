package com.oumen.activity.detail.cell;

import com.oumen.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.GridView;
/**
 * 带有一个gridview的活动详情子item
 * 标题行有监听
 */
public class GridElementView extends DefaultView {
	private GridView gridview;

	public GridElementView(Context context) {
		this(context, null, 0);
	}

	public GridElementView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public GridElementView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.huodong_detail_gridview_item, this, false);
		
		gridview = (GridView) view.findViewById(R.id.gridview);
		addViewToContainer(view);
	}
	
	public void setGridViewAdapter(BaseAdapter adapter) {
		gridview.setAdapter(adapter);
	}
	
	public void setNameOnClickListener(OnClickListener clickListener) {
		name.setOnClickListener(clickListener);
	}
	
	public void setFocusable(boolean focusable){
		gridview.setFocusable(focusable);
	}

}
