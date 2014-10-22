package com.oumen.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.oumen.R;

public abstract class AbstractDialog extends Dialog {
	protected LinearLayout layRoot;
	protected TextView txtTitle;
	protected Button btnPositive;
	protected Button btnNegative;
	
	abstract protected void onCreateView();

	public AbstractDialog(Context context) {
		super(context);
		preInitialize(context, false);
	}

	public AbstractDialog(Context context, int theme) {
		super(context, theme);
		preInitialize(context, false);
	}

	public AbstractDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
		preInitialize(context, false);
	}

	private void preInitialize(Context context, boolean cancelable) {
		setCancelable(cancelable);
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		layRoot = (LinearLayout) inflater.inflate(R.layout.dialog_abstract, null);

		setContentView(layRoot, new ViewGroup.LayoutParams(context.getResources().getDimensionPixelSize(R.dimen.default_dialog_width), LayoutParams.WRAP_CONTENT));
		txtTitle = (TextView) layRoot.findViewById(R.id.nav_title);
		btnPositive = (Button) layRoot.findViewById(R.id.positive);
		btnNegative = (Button) layRoot.findViewById(R.id.negative);
		
		onCreateView();
	}

	public TextView getTitleView() {
		return txtTitle;
	}
	
	public Button getPosititveButton() {
		return btnPositive;
	}
	
	public Button getNegativeButton() {
		return btnNegative;
	}
	
	public void setOnClickListener(View.OnClickListener listener) {
		btnPositive.setOnClickListener(listener);
		btnNegative.setOnClickListener(listener);
	}
	
	protected Object data;
	
	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}
}
