package com.oumen.near;

import com.oumen.R;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

/**
 * 新的活动
 * 
 */
public class NearActivity extends FragmentActivity {
	private NearFragment fragCircle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_near);
		
		if (fragCircle == null) {
			fragCircle = new NearFragment();
		}
		
		FragmentManager manager = getSupportFragmentManager();
		manager.beginTransaction().replace(R.id.near_container, fragCircle).commit();
	}
}
