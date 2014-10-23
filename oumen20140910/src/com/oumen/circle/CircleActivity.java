package com.oumen.circle;

import com.oumen.R;
import com.oumen.android.BaseFragment;
import com.oumen.home.FloatViewController;
import com.oumen.home.FloatViewHostController;
import com.oumen.home.SoftKeyboardController;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;

/**
 * 新的偶们圈
 * 
 */
public class CircleActivity extends FragmentActivity implements FloatViewHostController, View.OnTouchListener {
	private View popupLayer;
	private RelativeLayout rootContainer;//整个布局
	private CircleListFragment fragCircle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_default_container);

		initAnimation();
		popupLayer = findViewById(R.id.layer);
		rootContainer = (RelativeLayout) findViewById(R.id.root);

		if (fragCircle == null) {
			fragCircle = new CircleListFragment();
		}
		FragmentManager manager = getSupportFragmentManager();
		manager.beginTransaction().replace(R.id.circle_container, fragCircle).commit();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		FragmentManager manager = getSupportFragmentManager();
		Fragment current = manager.findFragmentById(R.id.circle_container);

		if (current != null) {
			current.onActivityResult(requestCode, resultCode, data);
		}
	}

	@Override
	public Activity getActivity() {
		return this;
	}
	
	@Override
	public void onBackPressed() {
		BaseFragment current = (BaseFragment) getSupportFragmentManager().findFragmentById(R.id.circle_container);
		if (current == null || !current.onBackPressed()) {
			super.onBackPressed();
		}
	}

	//----------------------- Animation -----------------------//
	private FloatViewController floatViewController;
	private Animation animBottomIn;
	private Animation animBottomOut;

	private void initAnimation() {
		animBottomIn = AnimationUtils.loadAnimation(getActivity(), R.anim.bottom_in);
		animBottomIn.setAnimationListener(animListener);
		animBottomOut = AnimationUtils.loadAnimation(getActivity(), R.anim.bottom_out);
		animBottomOut.setAnimationListener(animListener);
	}

	private final Animation.AnimationListener animListener = new Animation.AnimationListener() {

		@Override
		public void onAnimationStart(Animation animation) {
		}

		@Override
		public void onAnimationEnd(Animation animation) {
			floatViewController.setPlaying(false);
			if (animation == animBottomOut) {
				rootContainer.removeView(floatViewController.getRoot());
				floatViewController = null;

				popupLayer.setVisibility(View.GONE);
				popupLayer.setOnTouchListener(null);
			}
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
		}
	};

	//----------------------- FloatViewHostController -----------------------//
	@Override
	public boolean isFloatViewShowing() {
		return floatViewController != null && (floatViewController.isPlaying() || floatViewController.isShowing());
	}

	@Override
	public void showFloatView(FloatViewController controller) {
		if (isFloatViewShowing())
			return;

		floatViewController = controller;

		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

		View container = floatViewController.show();
		rootContainer.addView(container, params);
		container.startAnimation(animBottomIn);

		if (controller instanceof SoftKeyboardController) {
			SoftKeyboardController kb = (SoftKeyboardController) controller;
			kb.showSoftKeyboard();
		}

		popupLayer.setVisibility(View.VISIBLE);
		popupLayer.setOnTouchListener(this);
	}

	@Override
	public void hideFloatView() {
		if (!isFloatViewShowing())
			return;

		View container = floatViewController.hide();
		container.startAnimation(animBottomOut);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (v == popupLayer && event.getAction() == MotionEvent.ACTION_UP) {
			if (floatViewController != null && !floatViewController.isPlaying() && floatViewController.isShowing()) {
				hideFloatView();
			}
		}
		return true;
	}

	public View getPopupWindowLayer() {
		return popupLayer;
	}
}
