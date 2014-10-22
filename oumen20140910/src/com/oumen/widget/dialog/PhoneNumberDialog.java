package com.oumen.widget.dialog;

import com.oumen.R;

import android.content.Context;
import android.text.InputFilter;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

public class PhoneNumberDialog extends AbstractDialog {

	private EditText etInput;
	private TextView tvTip;

	public PhoneNumberDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
	}

	public PhoneNumberDialog(Context context, int theme) {
		super(context, theme);
	}

	public PhoneNumberDialog(Context context) {
		super(context);
	}

	@Override
	protected void onCreateView() {
		int margin = getContext().getResources().getDimensionPixelSize(R.dimen.default_gap);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		params.leftMargin = params.rightMargin = margin;
		etInput = new EditText(getContext());
		etInput.setSingleLine();
		etInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(11)});// 设置输入的长度
		etInput.setTextColor(getContext().getResources().getColor(R.color.phone_dialog_button_text));
		etInput.setBackgroundResource(R.drawable.phone_dialog_edit);
		etInput.setHint(getContext().getResources().getString(R.string.phone_confirm_dialog_hint));
		etInput.setInputType(EditorInfo.TYPE_CLASS_PHONE);
		layRoot.addView(etInput, 1, params);

		params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		tvTip = new TextView(getContext());
		tvTip.setText(getContext().getResources().getString(R.string.phone_confirm_dialog_msg));
		tvTip.setTextColor(getContext().getResources().getColor(R.color.red));
		params.leftMargin = params.rightMargin = margin;
		params.topMargin = getContext().getResources().getDimensionPixelSize(R.dimen.padding_small);
		layRoot.addView(tvTip, 2, params);
		tvTip.setVisibility(View.GONE);
	}

	public EditText getEditView() {
		return etInput;
	}
	public TextView getTipView() {
		return tvTip;
	}
}
