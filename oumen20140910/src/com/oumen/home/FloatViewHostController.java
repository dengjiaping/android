package com.oumen.home;

import android.app.Activity;

public interface FloatViewHostController {
	public Activity getActivity();
	
	public boolean isFloatViewShowing();
	
	public void showFloatView(FloatViewController controller);
	
	public void hideFloatView();
}
