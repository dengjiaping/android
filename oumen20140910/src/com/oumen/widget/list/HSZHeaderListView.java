package com.oumen.widget.list;

import java.util.concurrent.TimeUnit;

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

public class HSZHeaderListView<E> extends FrameLayout implements View.OnTouchListener {
	public static final int PULL_RATIO = 4;

	protected InnerListView listView;
	protected View headerView;
	protected View footerView;
	protected View eventTargetView;
	
	protected LoadingView loadingView;
	protected View emptyView;
	
	protected int pullRatio = PULL_RATIO;
	protected int headerLoadLine;
	protected int footerHeight;
	
	protected HSZListViewAdapter<E> adapter;
	protected EventListener eventListener;
	
	protected GestureDetector gesture;

	public HSZHeaderListView(Context context) {
		this(context, null, 0);
	}

	public HSZHeaderListView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public HSZHeaderListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		gesture = new GestureDetector(context, new GestureListenerImpl());
		
		listView = new InnerListView(context);
		listView.setSelector(android.R.color.transparent);
		listView.setDivider(new ColorDrawable(Color.GRAY));
		listView.setDividerHeight(1);
		listView.setCacheColorHint(Color.TRANSPARENT);
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
		
		loadingView.post(new Runnable() {
			
			@Override
			public void run() {
				int half;
				if (headerView == null) {
					half = getHeight() / 2 + headerView.getHeight();
				}
				else {
					half = (getHeight() - headerView.getHeight()) / 2 + headerView.getHeight();
				}
				FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) loadingView.getLayoutParams();
				params.topMargin = half - loadingView.getHeight() / 2;
				params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
				loadingView.setLayoutParams(params);
			}
		});
		emptyView.post(new Runnable() {
			
			@Override
			public void run() {
				int half;
				if (headerView == null) {
					half = getHeight() / 2 + headerView.getHeight();
				}
				else {
					half = (getHeight() - headerView.getHeight()) / 2 + headerView.getHeight();
				}
				FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) emptyView.getLayoutParams();
				params.topMargin = half - emptyView.getHeight() / 2;
				params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
				emptyView.setLayoutParams(params);
			}
		});
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
	
	public void setLoadingViewTextVisibility(int visibility) {
		if (loadingView != null) {
			loadingView.tip.setVisibility(visibility);
		}
	}
	
	public void setLoadingViewText(int resid) {
		if (loadingView != null) {
			loadingView.tip.setText(resid);
		}
	}
	
	public void setLoadingViewText(CharSequence text) {
		if (loadingView != null) {
			loadingView.tip.setText(text);
		}
	}
	
	public void setLoadingViewTextColor(int color) {
		if (loadingView != null) {
			loadingView.tip.setTextColor(color);
		}
	}
	
	public void setLoadingViewTextSize(int size) {
		if (loadingView != null) {
			loadingView.tip.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
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

	public int getHeaderLoadLine() {
		return headerLoadLine;
	}

	public void setHeaderLoadLine(int headerLoadLine) {
		this.headerLoadLine = headerLoadLine;
	}

	public View getHeaderView() {
		return headerView;
	}

	public void setHeaderView(View view) {
		this.headerView = view;
		listView.addHeaderView(headerView);
		
		headerView.setOnTouchListener(this);
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
	
	public void setFooterHeight(int height) {
		footerHeight = height;
	}
	
	public void setAdapter(HSZListViewAdapter<E> adapter) {
		this.adapter = adapter;
		listView.setAdapter(adapter);
	}

	public void setHeaderListener(EventListener eventListener) {
		this.eventListener = eventListener;
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
		synchronized(adapter) {
			while (adapter.isDataSetChanged()) {
				try {TimeUnit.MILLISECONDS.sleep(400);} catch (Exception e) {}
			}
			ELog.i("");
			
			listView.layout(listView.getLeft(), 0, listView.getRight(), listView.getHeight());
			headerLoading = footerLoading = false;
		}
	}
	
	public boolean isHeaderLoading() {
		return headerLoading;
	}

	public boolean isFooterLoading() {
		return footerLoading;
	}
	
	public boolean isLoading() {
		return headerLoading || footerLoading || loadingView.getVisibility() == View.VISIBLE;
	}
	
	protected void onHeaderLoad() {
		synchronized (adapter) {
			headerLoading = true;
			adapter.onHeaderLoad();
		}
	}
	
	protected void onFooterLoad() {
		synchronized (adapter) {
			footerLoading = true;
			adapter.onFooterLoad();
		}
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
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		synchronized(adapter) {
			if (adapter.isDataSetChanged()) {
				return false;
			}
			
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
		
		boolean consumed;
		float currentY = event.getRawY();
		float offsetVertical = Math.round((currentY - lastY) / pullRatio);
		int top = (int)(listView.getTop() + offsetVertical);
		boolean isTop = listView.isTop(), isBottom = listView.isBottom();
		
//		ELog.i("Loading:" + headerLoading + "/" + footerLoading + " Pos:" + isTop + "/" + isBottom + " Vertical:" + offsetVertical + " Top:" + top + " Direction:" + direction);
		
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
		
		if (consumed) {
			listView.layout(listView.getLeft(), top, listView.getRight(), top + listView.getHeight());
			if (headerLoadLine != 0) {
				if (top > srcListY && top < headerLoadLine) {
					adapter.onHeaderPull(headerView, top);
				}
				else if (top >= headerLoadLine) {
					adapter.onHeaderPullOverLine(headerView, top);
				}
			}
		}

		direction = listView.getTop() - srcListY;
		lastY = currentY;
		
		if (headerView != null && !isLoading() && headerLoadLine > 0 && direction >= headerLoadLine) {
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
//		ELog.i("Load:" + headerLoading + "/" + footerLoading + " Prepare:" + prepareLoadHeader + "/" + prepareLoadFooter);
		
		pressed = false;
		
		listView.layout(listView.getLeft(), srcListY, listView.getRight(), srcListY + listView.getHeight());
		
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
		
		prepareLoadHeader = false;
		prepareLoadFooter = false;
		
		direction = 0;
		return false;
	}
	
	public boolean down(MotionEvent event) {
//		ELog.i("Load:" + headerLoading + "/" + footerLoading + " Prepare:" + prepareLoadHeader + "/" + prepareLoadFooter);
		pressed = true;
		lastY = event.getRawY();
		srcListY = listView.getTop();
		return false;
	}
	
	private class InnerListView extends ListView implements ListView.OnScrollListener {

		public InnerListView(Context context) {
			super(context, null, 0);
			
			setOnScrollListener(this);
		}

		public boolean isTop() {
			return computeVerticalScrollOffset() == 0;
		}

		public boolean isBottom() {
			return computeVerticalScrollOffset() + computeVerticalScrollExtent() == computeVerticalScrollRange();
		}
		
		public boolean isLast() {
			if (footerHeight <= 0)
				return isBottom();
			
			return computeVerticalScrollOffset() + computeVerticalScrollExtent() >= computeVerticalScrollRange() - footerHeight;
		}

		@Override
		public boolean onTouchEvent(MotionEvent event) {
			synchronized (adapter) {
				if (adapter.isDataSetChanged()) {
					return false;
				}
				
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
}
