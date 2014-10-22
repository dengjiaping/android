package com.oumen.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.oumen.R;

public class ProgressDialog extends Dialog {
	private LinearLayout layContainer;
	private TextView txtTitle;
	private TextView txtMessage;
	private ProgressBar pgs;

	public ProgressDialog(Context context) {
		super(context);
		preInitialize(context, false);
	}

	public ProgressDialog(Context context, int theme) {
		super(context, theme);
		preInitialize(context, false);
	}

	public ProgressDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
		preInitialize(context, cancelable);
	}

	private void preInitialize(Context context, boolean cancelable) {
		setCancelable(cancelable);

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		layContainer = (LinearLayout) inflater.inflate(R.layout.dialog_progress, null);

		txtTitle = (TextView) layContainer.findViewById(R.id.nav_title);

		txtMessage = (TextView) layContainer.findViewById(R.id.progress_message);

		pgs = (ProgressBar) layContainer.findViewById(R.id.progress);

		setContentView(layContainer, new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		layContainer.getRootView().setBackgroundColor(Color.TRANSPARENT);
	}
	
	public TextView getTitleView() {
		return txtTitle;
	}
	
	public TextView getMessageView() {
		return txtMessage;
	}
	
	public ProgressBar getProgressView() {
		return pgs;
	}
}
