package com.oumen.mv;

import java.util.Calendar;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;

import com.oumen.R;

public class ComposeSelectDialog extends Dialog {
	public static final int RESULT_FROM_EDIT_VIEW = 1;

	private TextView btnImage;
	private TextView btnRecord;
	private TextView btnLocal;
	
	protected Calendar createAt;

	public ComposeSelectDialog(Context context) {
		super(context);
		preInitialize(context, true);
	}

	public ComposeSelectDialog(Context context, int theme) {
		super(context, theme);
		preInitialize(context, true);
	}

	protected ComposeSelectDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
		preInitialize(context, cancelable);
	}

	private void preInitialize(Context context, boolean cancelable) {
		setCancelable(cancelable);

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layContainer = inflater.inflate(R.layout.dialog_mv_selectmode, null);
		btnImage = (TextView) layContainer.findViewById(R.id.image);
		btnRecord = (TextView) layContainer.findViewById(R.id.record);
		btnLocal = (TextView) layContainer.findViewById(R.id.local);

		DisplayMetrics display = context.getResources().getDisplayMetrics();

		int padding = (int) (30 * display.density);
		setContentView(layContainer, new ViewGroup.LayoutParams(display.widthPixels - padding * 2, LayoutParams.WRAP_CONTENT));
		layContainer.getRootView().setBackgroundColor(Color.TRANSPARENT);
	}
	
	public void setClickListener(View.OnClickListener listener) {
		btnImage.setOnClickListener(listener);
		btnRecord.setOnClickListener(listener);
		btnLocal.setOnClickListener(listener);
	}

	public Calendar getCreateAt() {
		return createAt;
	}

	public void setCreateAt(Calendar createAt) {
		this.createAt = createAt;
	}
}
