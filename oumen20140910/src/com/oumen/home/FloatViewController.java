package com.oumen.home;

import android.view.View;

public interface FloatViewController {
	public boolean isShowing();
	
	public boolean isPlaying();
	
	public void setPlaying(boolean playing);
	
	public View getRoot();
	
	public View show();
	
	public View hide();
}
