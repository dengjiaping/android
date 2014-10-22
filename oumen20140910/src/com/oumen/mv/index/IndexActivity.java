package com.oumen.mv.index;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.oumen.R;
import com.oumen.TitleBar;
import com.oumen.home.FloatViewController;
import com.oumen.home.FloatViewHostController;
import com.oumen.home.SoftKeyboardController;

public class IndexActivity extends FragmentActivity implements FloatViewHostController, View.OnTouchListener {
	private TitleBar titlebar;
	private Button btnLeft;
	
	private DaysIndexFragment fragDays = new DaysIndexFragment();
	
	private View popupLayer;
	private RelativeLayout rootContainer;//整个布局

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mv_index);
		
		popupLayer = findViewById(R.id.layer);
		rootContainer = (RelativeLayout) findViewById(R.id.root);
		rootContainer.setOnTouchListener(this);
		
		titlebar = (TitleBar) findViewById(R.id.titlebar);
		titlebar.getRightButton().setVisibility(View.GONE);
		titlebar.getTitle().setText(R.string.module_title_mv);
		btnLeft = titlebar.getLeftButton();
		btnLeft.setOnClickListener(clickListener);
		initAnimation();

		getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragDays).commit();
	}
	
	private final OnClickListener clickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if (v == btnLeft) {
				finish();
			}
		}
	};
	

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		FragmentManager manager = getSupportFragmentManager();
		Fragment current = manager.findFragmentById(R.id.fragment_container);

		if (current != null) {
			current.onActivityResult(requestCode, resultCode, data);
		}
		
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public Activity getActivity() {
		return this;
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
}
