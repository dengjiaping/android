package com.oumen.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class SlidingPaneLayout extends android.support.v4.widget.SlidingPaneLayout {
	public enum Intercept {SUPER, TRUE, FALSE, SUPER_TRUE, SUPER_FALSE}
	
	protected Intercept intercept = Intercept.SUPER;

	public SlidingPaneLayout(Context context) {
		super(context);
	}

	public SlidingPaneLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SlidingPaneLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public Intercept getIntercept() {
		return intercept;
	}

	public void setIntercept(Intercept intercept) {
		this.intercept = intercept;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		switch (intercept) {
			case SUPER_TRUE:
				super.onInterceptTouchEvent(event);
				return true;
				
			case SUPER_FALSE:
				super.onInterceptTouchEvent(event);
				return false;
				
			case FALSE:
				return false;
			
			case TRUE:
				return true;
				
			case SUPER:
			default:
				return super.onInterceptTouchEvent(event);
		}
	}

}
