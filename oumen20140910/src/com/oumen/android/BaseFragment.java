package com.oumen.android;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;

import com.oumen.android.util.Constants;
import com.umeng.analytics.MobclickAgent;

public class BaseFragment extends Fragment implements Handler.Callback {
	protected final Handler handler = new Handler(this);

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public boolean handleMessage(Message msg) {
		return false;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if (!Constants.isDebug) {
			MobclickAgent.onResume(getActivity());
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if (!Constants.isDebug) {
			MobclickAgent.onPause(getActivity());
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	public boolean onBackPressed() {
		return false;
	}
}
