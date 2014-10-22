package com.oumen.widget.calander;

import java.util.Calendar;

import com.oumen.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.LinearLayout.LayoutParams;

public class DateWidgetDayCell extends View {
	// types
	public interface OnItemClick {
		public void OnClick(DateWidgetDayCell item);
	}

	public static int ANIM_ALPHA_DURATION = 100;
	// fields
	private final static float fTextSize = 28;
	private final static int iMargin = 1;
	private final static int iAlphaInactiveMonth = 0x88;

	// fields
	private int iDateYear = 0;
	private int iDateMonth = 0;
	private int iDateDay = 0;
	private int iDayOfWeek = 0;

	// fields
	private OnItemClick itemClick = null;
	private Paint paint = new Paint();
	private RectF rect = new RectF();
	private String sDate = "";

	// fields
	private boolean bSelected = false;
	private boolean bIsActiveMonth = false;
	private boolean bToday = false;
	private boolean bHoliday = false;
	private boolean bTouchedDown = false;

	// methods
	public DateWidgetDayCell(Context context, int iWidth, int iHeight) {
		super(context);
		setFocusable(true);
		setLayoutParams(new LayoutParams(iWidth, iHeight));
	}

	public boolean getSelected() {
		return this.bSelected;
	}

	@Override
	public void setSelected(boolean bEnable) {
		if (this.bSelected != bEnable) {
			this.bSelected = bEnable;
			this.invalidate();
		}
	}

	public void setData(int iYear, int iMonth, int iDay, boolean bToday, boolean bHoliday, int iActiveMonth, int iDayOfWeek) {
		iDateYear = iYear;
		iDateMonth = iMonth;
		iDateDay = iDay;

		this.sDate = Integer.toString(iDateDay);
		this.bIsActiveMonth = (iDateMonth == iActiveMonth);
		this.bToday = bToday;
		this.bHoliday = bHoliday;
		this.iDayOfWeek = iDayOfWeek;
	}

	public void setItemClick(OnItemClick itemClick) {
		this.itemClick = itemClick;
	}

	private int getTextHeight() {
		return (int) (-paint.ascent() + paint.descent());
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		boolean bResult = super.onKeyDown(keyCode, event);
		if ((keyCode == KeyEvent.KEYCODE_DPAD_CENTER) || (keyCode == KeyEvent.KEYCODE_ENTER)) {
			doItemClick();
		}
		return bResult;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		boolean bResult = super.onKeyUp(keyCode, event);
		return bResult;
	}

	public void doItemClick() {
		if (itemClick != null)
			itemClick.OnClick(this);
	}

	@Override
	protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
		super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
		invalidate();
	}

	public Calendar getDate() {
		Calendar calDate = Calendar.getInstance();
		calDate.clear();
		calDate.set(Calendar.YEAR, iDateYear);
		calDate.set(Calendar.MONTH, iDateMonth);
		calDate.set(Calendar.DAY_OF_MONTH, iDateDay);
		return calDate;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		// init rectangles
		rect.set(0, 0, this.getWidth(), this.getHeight());
		rect.inset(1, 1);

		// drawing
		drawDayView(canvas);
		drawDayNumber(canvas);
	}

	private void drawDayView(Canvas canvas) {
		if (bSelected) {
			LinearGradient lGradBkg = null;

			if (bSelected) {
				lGradBkg = new LinearGradient(rect.left, 0, rect.right, 0, getResources().getColor(R.color.calander_blue), getResources().getColor(R.color.default_bg), Shader.TileMode.CLAMP);
			}

			if (lGradBkg != null) {
				paint.setShader(lGradBkg);
//				canvas.drawRect(rect, paint);
				canvas.drawCircle(rect.centerX(), rect.centerY(), rect.width() / 2, paint);
			}

			paint.setShader(null);

		}
		else {

//			paint.setColor(DayStyle.getColorBkg(bHoliday, bToday));
			if (!bIsActiveMonth)
				paint.setAlpha(iAlphaInactiveMonth);

			paint.setColor(getResources().getColor(R.color.white));
			canvas.drawRect(rect, paint);
		}
	}

	public void drawDayNumber(Canvas canvas) {
		// draw day number
		paint.setTypeface(null);
		paint.setAntiAlias(true);
		paint.setShader(null);
		paint.setFakeBoldText(true);
		paint.setTextSize(fTextSize);

		paint.setUnderlineText(false);
//		if (bToday)
//			paint.setUnderlineText(true);

		int iTextPosX = (int) rect.right - (int) paint.measureText(sDate);
		int iTextPosY = (int) rect.bottom + (int) (-paint.ascent()) - getTextHeight();

		iTextPosX -= ((int) rect.width() >> 1) - ((int) paint.measureText(sDate) >> 1);
		iTextPosY -= ((int) rect.height() >> 1) - (getTextHeight() >> 1);

		// draw text
		if (bSelected) {
			if (bSelected)
				paint.setColor(getResources().getColor(R.color.calander_title_text));
		}
		else {
			if (bToday) {
				paint.setColor(getResources().getColor(R.color.red));
			}
			else {
				paint.setColor(getResources().getColor(R.color.calander_day_text));
			}
		}

		if (!bIsActiveMonth)
			paint.setAlpha(iAlphaInactiveMonth);

		canvas.drawText(sDate, iTextPosX, iTextPosY + iMargin, paint);

		paint.setUnderlineText(false);
	}

	public boolean IsViewFocused() {
		return (this.isFocused() || bTouchedDown);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean bHandled = false;
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			bHandled = true;
			bTouchedDown = true;
			invalidate();
			startAlphaAnimIn(DateWidgetDayCell.this);
		}
		if (event.getAction() == MotionEvent.ACTION_CANCEL) {
			bHandled = true;
			bTouchedDown = false;
			invalidate();
		}
		if (event.getAction() == MotionEvent.ACTION_UP) {
			bHandled = true;
			bTouchedDown = false;
			invalidate();
			doItemClick();
		}
		return bHandled;
	}

	public static void startAlphaAnimIn(View view) {
		AlphaAnimation anim = new AlphaAnimation(0.5F, 1);
		anim.setDuration(ANIM_ALPHA_DURATION);
		anim.startNow();
		view.startAnimation(anim);
	}

	public int getiDateMonth() {
		return iDateMonth;
	}

	public void setiDateMonth(int iDateMonth) {
		this.iDateMonth = iDateMonth;
	}

}
