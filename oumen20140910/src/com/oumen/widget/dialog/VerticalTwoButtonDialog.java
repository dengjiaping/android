package com.oumen.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.oumen.R;

public class VerticalTwoButtonDialog extends Dialog {
	private LinearLayout layContainer;
	private TextView txtTitle;
	private TextView txtMessage;
	private Button btnTop;
	private Button btnButtom;
	
	private Object tag;

	public VerticalTwoButtonDialog(Context context) {
		super(context);
		preInitialize(context, false);
	}

	public VerticalTwoButtonDialog(Context context, int theme) {
		super(context, theme);
		preInitialize(context, false);
	}

	public VerticalTwoButtonDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
		preInitialize(context, cancelable);
	}

	private void preInitialize(Context context, boolean cancelable) {
		setCancelable(cancelable);
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		layContainer = (LinearLayout) inflater.inflate(R.layout.dialog_vertical_two_button, null);

		setContentView(layContainer, new ViewGroup.LayoutParams(context.getResources().getDimensionPixelSize(R.dimen.default_dialog_width), LayoutParams.WRAP_CONTENT));
		txtTitle = (TextView) layContainer.findViewById(R.id.nav_title);

		txtMessage = (TextView) layContainer.findViewById(R.id.message);

		btnTop = (Button) layContainer.findViewById(R.id.btn_top);

		btnButtom = (Button) layContainer.findViewById(R.id.btn_buttom);

		layContainer.getRootView().setBackgroundColor(Color.TRANSPARENT);
	}
	
	public void setSize(int width, int height) {
		ViewGroup.LayoutParams params = layContainer.getLayoutParams();
		params.width = width;
		params.height = height;
		layContainer.setLayoutParams(params);
	}
	
	public TextView getTitleView() {
		return txtTitle;
	}
	
	public TextView getMessageView() {
		return txtMessage;
	}
	
	public Button getTopButton() {
		return btnTop;
	}
	
	public Button getButtomButton() {
		return btnButtom;
	}

	public Object getTag() {
		return tag;
	}

	public void setTag(Object tag) {
		this.tag = tag;
	}
}
