package com.oumen.widget.dialog;

import com.oumen.R;

import android.content.Context;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.LinearLayout;

public class SingleEditorDialog extends AbstractDialog {
	protected EditText edtInput;

	public SingleEditorDialog(Context context) {
		super(context);
	}

	public SingleEditorDialog(Context context, int theme) {
		super(context, theme);
	}

	public SingleEditorDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
	}

	@Override
	protected void onCreateView() {
		int margin = getContext().getResources().getDimensionPixelSize(R.dimen.default_gap);
		
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		params.leftMargin = params.rightMargin = margin;
		edtInput = new EditText(getContext());
		layRoot.addView(edtInput, 1, params);
	}
	
	public EditText getEditView() {
		return edtInput;
	}
}
