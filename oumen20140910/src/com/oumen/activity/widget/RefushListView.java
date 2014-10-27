package com.oumen.activity.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.AbsListView;

import com.ab.view.pullview.AbPullListView;
import com.oumen.android.App;

public class RefushListView extends AbPullListView {
	private int scrollState = App.INT_UNSET;
	private int currentVisibleStartIndex = App.INT_UNSET;
	private int currentVisibleEndIndex = App.INT_UNSET;

	public RefushListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public RefushListView(Context context) {
		super(context);
	}
	
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		super.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
		// 设置当前屏幕显示的起始index和结束index
		currentVisibleStartIndex = firstVisibleItem;
		currentVisibleEndIndex = firstVisibleItem + visibleItemCount;
	}
	
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		this.scrollState = scrollState;
		super.onScrollStateChanged(view, scrollState);
	}

	public int getScrollState() {
		return scrollState;
	}

	public void setScrollState(int scrollState) {
		this.scrollState = scrollState;
	}

	public int getCurrentVisibleStartIndex() {
		return currentVisibleStartIndex;
	}

	public int getCurrentVisibleEndIndex() {
		return currentVisibleEndIndex;
	}
}
