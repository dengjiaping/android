package com.oumen.widget.list;

import java.util.concurrent.TimeUnit;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.oumen.R;
import com.oumen.tools.ELog;

@SuppressLint("ClickableViewAccessibility")
public class HSZListView<E> extends FrameLayout implements View.OnTouchListener {
	public static final int PULL_RATIO = 2;

	protected InnerListView listView;
	protected View headerView;
	protected View footerView;
	protected View eventTargetView;
	
	protected LoadingView loadingView;
	protected View emptyView;
	
	protected int pullRatio = PULL_RATIO;
	protected int headerHeight;
	protected int footerHeight;
	
	protected HSZListViewAdapter<E> adapter;
	protected EventListener eventListener;
	
	protected GestureDetector gesture;

	public HSZListView(Context context) {
		this(context, null, 0);
	}

	public HSZListView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public HSZListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		gesture = new GestureDetector(context, new GestureListenerImpl());
		
		listView = new InnerListView(context);
		listView.setSelector(android.R.color.transparent);
		listView.setDivider(new ColorDrawable(Color.GRAY));
		listView.setDividerHeight(1);
		listView.setCacheColorHint(Color.TRANSPARENT);
		listView.setDrawingCacheEnabled(true);
		addView(listView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		
		loadingView = new LoadingView(context);
		loadingView.setVisibility(View.GONE);
		addView(loadingView, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER));
		
		int padding = getResources().getDimensionPixelSize(R.dimen.padding_super);
		TextView txt = new TextView(context);
		txt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
		txt.setTextColor(Color.BLACK);
		txt.setPadding(padding, padding, padding, padding);
		txt.setGravity(Gravity.CENTER);
		txt.setText("木有内容");
		txt.setVisibility(View.GONE);
		emptyView = txt;
		addView(emptyView, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER));
	}
	
	public void smoothScrollToPositionFromTop(int position, int offset, int duration) {
		listView.smoothScrollToPositionFromTop(position, offset, duration);
	}
	
	public void setEmptyText(CharSequence text) {
		if (emptyView instanceof TextView) {
			TextView txt = (TextView) emptyView;
			txt.setText(text);
		}
	}
	
	public void setEmptyText(int resid) {
		if (emptyView instanceof TextView) {
			TextView txt = (TextView) emptyView;
			txt.setText(resid);
		}
	}
	
	public void setEmptyViewVisibility(int visibility) {
		if (emptyView != null) {
			emptyView.setVisibility(visibility);
		}
	}
	
	public void setLoadingViewVisibility(int visibility) {
		if (loadingView != null) {
			loadingView.setVisibility(visibility);
		}
	}
	
	public void setCacheColorHint(int color) {
		listView.setCacheColorHint(color);
	}
	
	public void setDivider(Drawable divider) {
		listView.setDivider(divider);
	}
	
	public void setDividerHeight(int height) {
		listView.setDividerHeight(height);
	}
	
	public void setSelector(Drawable sel) {
		listView.setSelector(sel);
	}
	
	public void setSelector(int resId) {
		listView.setSelector(resId);
	}
	
	public void setListPadding(int left, int top, int right, int bottom) {
		listView.setPadding(left, top, right, bottom);
	}

	public int getHeaderHeight() {
		return headerHeight;
	}

	public void setHeaderHeight(int headerHeight) {
		this.headerHeight = headerHeight;
	}

	public View getHeaderView() {
		return headerView;
	}

	public void setHeaderView(View view, int headerHeight) {
		this.headerView = view;
		this.headerHeight = headerHeight;
		
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, headerHeight);
		params.topMargin = headerHeight * -1;
		addView(view, params);
		
		headerView.setOnTouchListener(this);
	}
	
	public void addHeaderView(View header) {
		listView.addHeaderView(header);
	}
	
	public View getFooterView() {
		return footerView;
	}

	public void setFooterView(View view, int height) {
		this.footerView = view;
		footerHeight = height;
		
		listView.addFooterView(footerView);
		
		footerView.setOnTouchListener(this);
	}
	
	public void setAdapter(HSZListViewAdapter<E> adapter) {
		this.adapter = adapter;
		listView.setAdapter(adapter);
	}

	public void setHeaderListener(EventListener eventListener) {
		this.eventListener = eventListener;
	}
	
	public void setSelection(int position) {
		listView.setSelection(position);
	}
	
	private float lastY = 0;
	private int srcListY = 0;
	private int direction = -1;
	
	private boolean headerLoading;
	private boolean footerLoading;
	private boolean prepareLoadHeader;
	private boolean prepareLoadFooter;
	private boolean pressed;
	
	public void loaded() {
		ELog.i("");
		while (adapter.isDataSetChanged()) {
			try {TimeUnit.MILLISECONDS.sleep(400);} catch (Exception e) {}
		}
		moveView(listView, 0);
		moveView(headerView, 0 - headerHeight);
		headerLoading = footerLoading = false;
	}
	
	public boolean isHeaderLoading() {
		return headerLoading;
	}

	public boolean isFooterLoading() {
		return footerLoading;
	}

	public void headerLoad() {
		if (isLoading() || headerView == null)
			return;

		onHeaderLoad();
	}
	
	public void footerLoad() {
		if (isLoading() || headerView == null)
			return;

		onFooterLoad();
	}
	
	private void moveView(View target, int top) {
		if (target == null)
			return;
		
		FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) target.getLayoutParams();
		params.topMargin = top;
		target.setLayoutParams(params);
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		synchronized (adapter) {
			if (adapter.isDataSetChanged())
				return false;
			
			eventTargetView = v;
			boolean consumed = false;
//			ELog.i(containerScroll.computeVerticalScrollOffset() + "/" + containerScroll.getHeight() + "/" + containerScroll.computeVerticalScrollRange());
			switch (event.getAction()) {
				case MotionEvent.ACTION_MOVE:
					consumed = move(event);
					break;
					
				case MotionEvent.ACTION_DOWN:
					consumed = down(event);
					if (v == headerView || v == footerView) {
						consumed = gesture.onTouchEvent(event);
					}
					break;
					
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_OUTSIDE:
					consumed = up(event);
					if (v == headerView || v == footerView) {
						consumed = gesture.onTouchEvent(event);
					}
					break;
			}
			return consumed;
		}
	}
	
	public boolean move(MotionEvent event) {
		if (!pressed) {
			lastY = event.getRawY();
			pressed = true;
		}
		
		if (isLoading()) {
			return false;
		}
		
		FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) listView.getLayoutParams();
		boolean consumed;
		float currentY = event.getRawY();
		float offsetVertical = Math.round((currentY - lastY) / pullRatio);
		int top = (int)(params.topMargin + offsetVertical);
		boolean isTop = listView.isTop(), isBottom = listView.isBottom();
		
		if (direction > 0 && (isTop || footerLoading)) {
			consumed = true;
		}
		else if (direction < 0 && (isBottom || headerLoading)) {
			consumed = true;
		}
		else {
			if (offsetVertical > 0) {
				consumed = isTop;
			}
			else if (offsetVertical < 0) {
				consumed = isBottom;
			}
			else {
				consumed = false;
			}
		}
		
//		ELog.i("Consumed:" + consumed + " Loading:" + headerLoading + "/" + footerLoading + " Pos:" + isTop + "/" + isBottom
//				+ " Vertical:" + offsetVertical + " Top:" + top + " Direction:" + direction);
		
		if (consumed) {
			moveView(listView, top);
			
			if (headerHeight != 0) {
				moveView(headerView, top - headerHeight);
				
				if (top > srcListY && top < headerHeight) {
					adapter.onHeaderPull(headerView, top);
				}
				else if (top >= headerHeight) {
					adapter.onHeaderPullOverLine(headerView, top);
				}
			}
		}

		direction = listView.getTop() - srcListY;
		lastY = currentY;
		
		if (headerView != null && !isLoading() && headerHeight > 0 && direction >= headerHeight) {
			prepareLoadHeader = true;
			prepareLoadFooter = false;
		}
		else if (footerView != null && !isLoading() && listView.isLast()) {
			prepareLoadFooter = true;
			prepareLoadHeader = false;
		}
		else {
			prepareLoadFooter = false;
			prepareLoadHeader = false;
		}
		return consumed;
	}
	
	public boolean up(MotionEvent event) {
//		ELog.i("Load:" + headerLoading + "/" + footerLoading + " Prepare:" + prepareLoadHeader + "/" + prepareLoadFooter + " Src:" + srcListY);
		
		pressed = false;
		
		if (headerView != null) {
			if (prepareLoadHeader && !isLoading()) {
				onHeaderLoad();
			}
		}
		
		if (footerView != null) {
			if (prepareLoadFooter && !isLoading()) {
				onFooterLoad();
			}
		}
		
		if (!headerLoading) {
			moveView(listView, srcListY);
			moveView(headerView, srcListY - headerHeight);
		}
		
		prepareLoadHeader = false;
		prepareLoadFooter = false;
		
		direction = 0;
		return false;
	}
	
	public boolean down(MotionEvent event) {
//		ELog.i("Load:" + headerLoading + "/" + footerLoading + " Prepare:" + prepareLoadHeader + "/" + prepareLoadFooter);
		FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) listView.getLayoutParams();
		pressed = true;
		lastY = event.getRawY();
		srcListY = params.topMargin;
		
		if (headerView != null) {
			headerView.setVisibility(footerLoading ? View.GONE : View.VISIBLE);
		}
		return false;
	}
	
	protected void onHeaderLoad() {
		headerView.setVisibility(View.VISIBLE);

		headerLoading = true;

		moveView(listView, srcListY + headerHeight);
		moveView(headerView, srcListY);

		adapter.onHeaderLoad();
	}
	
	protected void onFooterLoad() {
		footerLoading = true;
		adapter.onFooterLoad();
	}
	
	public boolean isLoading() {
		return headerLoading || footerLoading || loadingView.getVisibility() == View.VISIBLE;
	}
	
	@SuppressLint("ClickableViewAccessibility")
	private class InnerListView extends ListView implements ListView.OnScrollListener {

		public InnerListView(Context context) {
			super(context, null, 0);
			
			setOnScrollListener(this);
		}

		public boolean isTop() {
			return computeVerticalScrollOffset() == 0;
		}

		public boolean isBottom() {
			int offset = computeVerticalScrollOffset(), extent = computeVerticalScrollExtent(), range = computeVerticalScrollRange();
			return offset + extent == range;
//			return computeVerticalScrollOffset() + computeVerticalScrollExtent() == computeVerticalScrollRange();
		}
		
		public boolean isLast() {
			if (footerHeight <= 0)
				return isBottom();
			
			return computeVerticalScrollOffset() + computeVerticalScrollExtent() >= computeVerticalScrollRange() - footerHeight;
		}

		@Override
		public boolean onTouchEvent(MotionEvent event) {
			synchronized (adapter) {
				if (adapter.isDataSetChanged())
					return false;
				
				if (headerView == null && footerView == null)
					return super.onTouchEvent(event);

				boolean consumed = false;
//				ELog.i(containerScroll.computeVerticalScrollOffset() + "/" + containerScroll.getHeight() + "/" + containerScroll.computeVerticalScrollRange());
				switch (event.getAction()) {
					case MotionEvent.ACTION_MOVE:
						consumed = move(event);
						break;
						
					case MotionEvent.ACTION_DOWN:
						consumed = down(event);
						break;
						
					case MotionEvent.ACTION_UP:
					case MotionEvent.ACTION_OUTSIDE:
						consumed = up(event);
						break;
				}
				
				if (consumed)
					return true;
				else
					return super.onTouchEvent(event);
			}
		}

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			if (scrollState == OnScrollListener.SCROLL_STATE_IDLE && !isLoading() && footerView != null && isLast()) {
				onFooterLoad();
			}
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {}
	}
	
	private class GestureListenerImpl extends GestureDetector.SimpleOnGestureListener {

		@Override
		public boolean onDown(MotionEvent e) {
			return true;
		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			if (eventListener != null) {
				if (headerView != null && eventTargetView == headerView)
					eventListener.onHeaderClick();
				else if (footerView != null && eventTargetView == footerView)
					eventListener.onFooterClick();
			}
			return true;
		}
		
	}

	public boolean isTop() {
		return listView.isTop();
	}

	public boolean isBottom() {
		return listView.isBottom();
	}
	
	public boolean isLast() {
		return listView.isLast();
	}
}
