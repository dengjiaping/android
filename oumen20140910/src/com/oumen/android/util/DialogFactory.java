package com.oumen.android.util;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.oumen.R;
import com.oumen.widget.dialog.ProgressDialog;


public class DialogFactory {

	public static Dialog creatRequestDialog(final Context context, String tip) {

		final Dialog dialog = new Dialog(context, R.style.dialog);
		dialog.setContentView(R.layout.dialog_layout);
		Window window = dialog.getWindow();
		WindowManager.LayoutParams lp = window.getAttributes();
		int width = context.getResources().getDisplayMetrics().widthPixels;
		lp.width = (int) (0.6 * width);

		TextView titleTxtv = (TextView) dialog.findViewById(R.id.tvLoad);
		if (tip == null || tip.length() == 0) {
			titleTxtv.setText(R.string.sending_request);
		} else {
			titleTxtv.setText(tip);
		}

		return dialog;
	}

	public static void ToastDialog(Context context, String title, String msg) {
		new AlertDialog.Builder(context).setTitle(title).setMessage(msg)
				.setPositiveButton("确定", null).create().show();
	}
	
	public static ProgressDialog createProgressDialog(Context context) {
		ProgressDialog dialog = new ProgressDialog(context, android.R.style.Theme_Panel);
		dialog = new ProgressDialog(context, android.R.style.Theme_Panel);
		dialog.getTitleView().setVisibility(View.GONE);
//		dialog.setMessage("正在加载数据...");
		dialog.getMessageView().setText("正在加载数据...");
		dialog.show();
		return dialog;
	}
	
	public static ProgressDialog createProgressDialog(Context context, String message, boolean cancelable) {
		ProgressDialog dialog = new ProgressDialog(context, android.R.style.Theme_Panel);
		dialog.getMessageView().setText(message);
		dialog.getTitleView().setVisibility(View.GONE);
//		dialog.setMessage(message);
		dialog.setCancelable(cancelable);
		dialog.show();
		return dialog;
	}
}
