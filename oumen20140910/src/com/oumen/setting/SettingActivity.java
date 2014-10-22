package com.oumen.setting;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import com.oumen.R;
import com.oumen.android.BaseFragment;

/**
 * 设置界面
 * 
 */
public class SettingActivity extends FragmentActivity {
	public final int FRAGMENT_SETTING = 1;
	public final int FRAGMENT_ABOUT = 2;
	
	private SettingFragment fragSetting;
	private AboutOumenFragment fragAbout;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		switchFragment(FRAGMENT_SETTING);
	}
	
	public void switchFragment(int currentFragment) {
		FragmentManager manager = getSupportFragmentManager();
		switch (currentFragment) {
			case FRAGMENT_SETTING:
				if (fragSetting == null) {
					fragSetting = new SettingFragment();
				}
				manager.beginTransaction().replace(R.id.setting_container, fragSetting).commitAllowingStateLoss();
				fragAbout = null;
				break;

			case FRAGMENT_ABOUT:
				if (fragAbout == null) {
					fragAbout = new AboutOumenFragment();
				}
				manager.beginTransaction().replace(R.id.setting_container, fragAbout).commit();
				fragSetting = null;
				break;
		}
	}
	
	@Override
	public void onBackPressed() {
		BaseFragment current = (BaseFragment) getSupportFragmentManager().findFragmentById(R.id.setting_container);
		if (current != null && current instanceof AboutOumenFragment) {
			switchFragment(FRAGMENT_SETTING);
		}
		else {
			super.onBackPressed();
		}
	}

}
