package com.oumen;

import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;

public class FloatWindowController implements Animation.AnimationListener, View.OnTouchListener {
	protected ViewGroup layerPopup;
	protected View target;
	protected boolean playing;
	protected boolean shown;

	public FloatWindowController(ViewGroup layerPopup) {
		this.layerPopup = layerPopup;
		layerPopup.setOnTouchListener(this);
	}

	public boolean isPlaying() {
		return playing;
	}

	public boolean isShown() {
		return shown;
	}
	
	public ViewGroup getLayerPopup() {
		return layerPopup;
	}

	public void reset() {
		layerPopup.setVisibility(View.GONE);
		playing = false;
		shown = false;
		target = null;
	}
	
	public void setTargetView(View target) {
		this.target = target;
		layerPopup.addView(target);
	}
	
	public void startAnimation(Animation anim) {
		layerPopup.setVisibility(View.VISIBLE);
		anim.setAnimationListener(this);
		target.startAnimation(anim);
	}

	@Override
	public void onAnimationStart(Animation animation) {
		playing = true;
	}

	@Override
	public void onAnimationEnd(Animation animation) {
		playing = false;
		shown = !shown;
		if (!shown) {
			layerPopup.setVisibility(View.GONE);
		}
	}

	@Override
	public void onAnimationRepeat(Animation animation) {}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		return true;
	}

}
