package com.oumen.android;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;

import com.oumen.android.util.Constants;
import com.oumen.android.util.DialogFactory;
import com.oumen.widget.dialog.ProgressDialog;
import com.umeng.analytics.MobclickAgent;

public class BaseFragment extends Fragment implements Handler.Callback {
	protected final Handler handler = new Handler(this);

	protected ProgressDialog dialog;

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
		dismissProgressDialog();
		super.onDestroy();
	}

	public boolean onBackPressed() {
		return false;
	}

	public boolean isShowingProgressDialog() {
		return dialog != null && dialog.isShowing();
	}

	public void showProgressDialog(String message) {
		if (dialog == null) {
			// dialog = DialogFactory.createProgressDialog(this, message,
			// false);
			dialog = DialogFactory.createProgressDialog(getActivity());
		}
		dialog.getMessageView().setText(message);

		if (!dialog.isShowing())
			dialog.show();
	}

	public void showProgressDialog() {
		if (dialog == null) {
			dialog = DialogFactory.createProgressDialog(getActivity());
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
