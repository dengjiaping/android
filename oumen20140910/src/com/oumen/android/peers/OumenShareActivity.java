package com.oumen.android.peers;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.KeyEvent;

import com.oumen.R;
import com.oumen.android.App;
import com.oumen.tools.ELog;

public class OumenShareActivity extends FragmentActivity {
	private final int CURRENT_FRAGMENT_SHARE = 1;

	private int currentFragment = 1;
	private ShareFragment fragShare;
	
	protected App mBaseApplication;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.oumen_share);
		mBaseApplication = (App) getApplication();
		initFragment();
	}

	private void initFragment() {
		switch (currentFragment) {
			case CURRENT_FRAGMENT_SHARE:
				showFragment();
				break;
		}
	}

	private void showFragment() {
		FragmentManager manager = getSupportFragmentManager();
		if (fragShare == null) {
			fragShare = new ShareFragment();
		}
		manager.beginTransaction().replace(R.id.share_container, fragShare).commit();
		currentFragment = CURRENT_FRAGMENT_SHARE;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		ELog.i("");
		FragmentManager manager = getSupportFragmentManager();
		Fragment current = manager.findFragmentById(R.id.share_container);
		if (current != null) {
			current.onActivityResult(requestCode, resultCode, data);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (fragShare != null) {
				fragShare.onKeyDown();
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}
}
