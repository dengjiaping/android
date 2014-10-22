package com.oumen.mv.index;

import com.oumen.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class DateView extends RelativeLayout {
	protected TextView txtDay;

	public DateView(Context context) {
		this(context, null, 0);
	}

	public DateView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public DateView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.mv_date_item, this, true);
		
		txtDay = (TextView) findViewById(R.id.day);
	}

}
