package com.oumen.activity.list;

import com.oumen.R;
import com.oumen.activity.HuodongTypeUtil;
import com.oumen.android.App;
import com.oumen.android.BaseFragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

/**
 * 新的活动
 * 
 */
public class AmuseActivity extends FragmentActivity {
//	private ActivityFragment fragActivityList;
	private ActivityListFragment fragActivityList;
	private NearActivityFragment fragNear;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_default_container);
		int type = getIntent().getIntExtra(ActivityFragment.HUODONG_TYPE, App.INT_UNSET);
		
		if (type == HuodongTypeUtil.CONDITION_FUJIN) {// 附近活动
			if (fragNear == null) {
				fragNear = new NearActivityFragment();
			}
			FragmentManager manager = getSupportFragmentManager();
			manager.beginTransaction().replace(R.id.circle_container, fragNear).commit();
		}
		else {// 活动筛选界面
			Bundle bundle = new Bundle();
			bundle.putInt(ActivityFragment.HUODONG_TYPE, type);
			
			if (fragActivityList == null) {
//				fragActivityList = new ActivityFragment();
				fragActivityList = new ActivityListFragment();
			}
			fragActivityList.setArguments(bundle);
			FragmentManager manager = getSupportFragmentManager();
			manager.beginTransaction().replace(R.id.circle_container, fragActivityList).commit();
		}
		
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			BaseFragment current = (BaseFragment) getSupportFragmentManager().findFragmentById(R.id.circle_container);
			if (current != null) {
				current.onActivityResult(requestCode, resultCode, data);
			}
		}
		else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	@Override
	public void onBackPressed() {
		BaseFragment current = (BaseFragment) getSupportFragmentManager().findFragmentById(R.id.circle_container);
		if (current != null) {
			boolean processed = current.onBackPressed();
			if (processed) {
				return;
			}
		}
		super.onBackPressed();
	}
}
