package com.oumen.mv.index;

import com.oumen.R;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.TextView;

public class AddPageView extends FrameLayout {
	protected TextView txt;

	public AddPageView(Context context) {
		this(context, null, 0);
	}

	public AddPageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public AddPageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		setBackgroundResource(R.color.mv_list_grey);
		
		txt = new TextView(context);
		txt.setGravity(Gravity.CENTER_HORIZONTAL);
		txt.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.mv_add_btn, 0, 0);
		txt.setCompoundDrawablePadding(getResources().getDimensionPixelSize(R.dimen.padding_large));
		txt.setTextColor(Color.WHITE);
		txt.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.default_editsize_big));
		txt.setText(R.string.mv_list_empty_add_tip);
		addView(txt, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER));
	}

}
