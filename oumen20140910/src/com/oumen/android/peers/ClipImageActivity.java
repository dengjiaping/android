package com.oumen.android.peers;

import com.oumen.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

public class ClipImageActivity extends FragmentActivity {
	private ClipImageFragment clipImageFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.clip_imageview);
		changeFragment();
	}

	private void changeFragment() {
		FragmentManager manager = getSupportFragmentManager();
		Fragment current = manager.findFragmentById(R.id.login_container);
		if (current == null) {
			clipImageFragment = new ClipImageFragment();
			manager.beginTransaction().add(R.id.login_container, clipImageFragment).commit();
			return;
		}
	}

}
