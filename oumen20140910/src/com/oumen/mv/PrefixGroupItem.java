package com.oumen.mv;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.TextView;

public class PrefixGroupItem extends TextView {
	protected int type;
	protected CharSequence title;

	public PrefixGroupItem(Context context) {
		this(context, null, 0);
	}

	public PrefixGroupItem(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public PrefixGroupItem(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		setTextSize(TypedValue.COMPLEX_UNIT_PX, 50);
		setTextColor(Color.WHITE);
		setGravity(Gravity.CENTER);
	}

	public void update(int type, CharSequence title) {
		this.type = type;
		this.title = title;
		
		setText(title);
		
		switch (type) {
			case 0://热门推荐
				setBackgroundColor(0xFFF99079);
				break;
				
			case 1://人生第一次
				setBackgroundColor(0xFFF3B674);
				break;
				
			case 2://节日
				setBackgroundColor(0xFF69C49F);
				break;
				
			case 3://户外
				setBackgroundColor(0xFF63B6E7);
				break;
				
			case 4://室内
				setBackgroundColor(0xFFC663E7);
				break;
				
			default:
				setBackgroundColor(Color.BLACK);
		}
	}
}
