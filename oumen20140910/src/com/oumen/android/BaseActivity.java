package com.oumen.android;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.oumen.android.util.Constants;
import com.oumen.android.util.DialogFactory;
import com.oumen.widget.dialog.ProgressDialog;
import com.umeng.analytics.MobclickAgent;

public class BaseActivity extends Activity implements Handler.Callback {
	protected final Handler handler = new Handler(this);
	protected App mBaseApplication;

	protected ProgressDialog dialog;
	// 是否有网络标记
	protected boolean NetFlag = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mBaseApplication = (App) getApplication();
	}

	@Override
	public boolean handleMessage(Message msg) {
		return false;
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		dismissProgressDialog();
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!Constants.isDebug) {
			MobclickAgent.onResume(this);
		}
	}

	public void onPause() {
		super.onPause();
		if (!Constants.isDebug) {
			MobclickAgent.onPause(this);
		}
	}

	public boolean isShowingProgressDialog() {
		return dialog != null && dialog.isShowing();
	}

	public void showProgressDialog(String message) {
		if (dialog == null) {
			// dialog = DialogFactory.createProgressDialog(this, message,
			// false);
			dialog = DialogFactory.createProgressDialog(this);
		}
		dialog.getMessageView().setText(message);

		if (!dialog.isShowing())
			dialog.show();
	}

	public void showProgressDialog() {
		if (dialog == null) {
			dialog = DialogFactory.createProgressDialog(this);
		}
		else {
			if (!dialog.isShowing())
				dialog.show();
		}
	}

	public void dismissProgressDialog() {
		if (dialog != null && dialog.isShowing())
			dialog.dismiss();
	}
}
