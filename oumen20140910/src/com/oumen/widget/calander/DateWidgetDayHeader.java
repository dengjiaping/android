package com.oumen.widget.calander;

import java.util.*;

import com.oumen.R;

import android.content.*;
import android.view.*;
import android.widget.LinearLayout.LayoutParams;
import android.graphics.*;

public class DateWidgetDayHeader extends View {
	// fields
	private final static int iDayHeaderFontSize = 28;

	// fields
	private Paint paint = new Paint();
	private RectF rect = new RectF();
	private int iWeekDay = -1;
	private boolean bHoliday = false;

	private final static String[] vecStrWeekDayNames = getWeekDayNames();
	
	// methods
	public DateWidgetDayHeader(Context context, int iWidth, int iHeight) {
		super(context);
		setLayoutParams(new LayoutParams(iWidth, iHeight));
	}

	public void setData(int iWeekDay) {
		this.iWeekDay = iWeekDay;
		this.bHoliday = false;
		if ((iWeekDay == Calendar.SATURDAY) || (iWeekDay == Calendar.SUNDAY))
			this.bHoliday = true;
	}

	private void drawDayHeader(Canvas canvas) {
		if (iWeekDay != -1) {
			// background
			paint.setColor(getResources().getColor(R.color.calander_bg));
			canvas.drawRect(rect, paint);

			// text
			paint.setTypeface(null);
			paint.setTextSize(iDayHeaderFontSize);
			paint.setAntiAlias(true);
			paint.setFakeBoldText(true);
			
			if (bHoliday) {
				if (iWeekDay == Calendar.SATURDAY)
					paint.setColor(getResources().getColor(R.color.calander_blue));
				else
					paint.setColor(getResources().getColor(R.color.red));
			}
			else {
				paint.setColor(getResources().getColor(R.color.calander_title_text));
			}

			final int iTextPosY = getTextHeight();
			final String sDayName = getWeekDayName(iWeekDay);

			// draw day name
			final int iDayNamePosX = (int) rect.left + ((int) rect.width() >> 1) - ((int) paint.measureText(sDayName) >> 1);
			canvas.drawText(sDayName, iDayNamePosX, rect.top + iTextPosY + 2, paint);
		}
	}

	private int getTextHeight() {
		return (int) (-paint.ascent() + paint.descent());
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		// init rectangles
		rect.set(0, 0, this.getWidth(), this.getHeight());
		// drawing
		drawDayHeader(canvas);
	}
	
	
	private static String[] getWeekDayNames() {
		String[] vec = new String[10];

		vec[Calendar.SUNDAY] = "日";
		vec[Calendar.MONDAY] = "一";
		vec[Calendar.TUESDAY] = "二";
		vec[Calendar.WEDNESDAY] = "三";
		vec[Calendar.THURSDAY] = "四";
		vec[Calendar.FRIDAY] = "五";
		vec[Calendar.SATURDAY] = "六";
		return vec;
	}

	public static String getWeekDayName(int iDay) {
		return vecStrWeekDayNames[iDay];
	}

}
