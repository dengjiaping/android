package com.oumen.widget.file;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.util.AttributeSet;
import android.view.View;

public class DirInfoView extends View {
	private Bitmap icon;

	private int gapIconAndInfo = 10;
	private int gapInfo1AndInfo2 = 10;

	private String info1;
	private int info1Height;
	private int info1TextSize = 34;
	private int info1TextColor = 0xFF000000;

	private String info2;
	private int info2Height;
	private int info2TextSize = 28;
	private int info2TextColor = 0xFF7E7E7E;

	private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

	public DirInfoView(Context context) {
		this(context, null, 0);
	}

	public DirInfoView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public DirInfoView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		int iconHeight = 0, infoHeight = measureInfoHeight();

		int x = getPaddingLeft();
		if (icon != null) {
			iconHeight = icon.getHeight();
			int offsetY = infoHeight > iconHeight ? (infoHeight - iconHeight) / 2 : 0;

			canvas.drawBitmap(icon, x, getPaddingTop() + offsetY, paint);

			x += gapIconAndInfo + icon.getWidth();
		}

		if (info1 != null && info2 != null) {
			paint.setTextSize(info1TextSize);
			paint.setColor(info1TextColor);
			FontMetricsInt fm = paint.getFontMetricsInt();
			int y = (getHeight() - infoHeight) / 2 - fm.ascent;
			canvas.drawText(info1, x, y, paint);
			y += fm.ascent;

			paint.setTextSize(info2TextSize);
			paint.setColor(info2TextColor);
			fm = paint.getFontMetricsInt();
			y += info2Height + gapInfo1AndInfo2 - fm.ascent;
			canvas.drawText(info2, x, y, paint);
		}
		else if (info1 != null) {
			paint.setTextSize(info1TextSize);
			paint.setColor(info1TextColor);
			FontMetricsInt fm = paint.getFontMetricsInt();
			int y = (getHeight() - infoHeight) / 2 - fm.ascent;
			canvas.drawText(info1, x, y, paint);
		}
		else if (info2 != null) {
			paint.setTextSize(info2TextSize);
			paint.setColor(info2TextColor);
			FontMetricsInt fm = paint.getFontMetricsInt();
			int y = (getHeight() - infoHeight) / 2 - fm.ascent;
			canvas.drawText(info2, x, y, paint);
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
	}

	private int measureWidth(int measureSpec) {
		int result = 0;
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);

		if (specMode == MeasureSpec.EXACTLY) {
			result = specSize;
		}
		else {
			result = getPaddingLeft() + getPaddingRight() + gapIconAndInfo + measureInfoWidth();
			if (icon != null) {
				result += Math.min(icon.getWidth(), icon.getHeight());
			}

			if (specMode == MeasureSpec.AT_MOST) {
				result = Math.min(result, specSize);
			}
		}

		return result;
	}

	private int measureHeight(int measureSpec) {
		int result = 0;
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);

		if (specMode == MeasureSpec.EXACTLY) {
			result = specSize;
		}
		else {
			result = getPaddingTop() + getPaddingBottom();

			int iconHeight = 0, infoHeight = measureInfoHeight();
			if (icon != null) {
				iconHeight = icon.getHeight();
				result += Math.max(iconHeight, infoHeight);
			}

			if (specMode == MeasureSpec.AT_MOST) {
				result = Math.min(result, specSize);
			}
		}

		return result;
	}

	private int measureInfoWidth() {
		int result = 0;
		float w1 = info1 == null ? 0 : paint.measureText(info1), w2 = info2 == null ? 0 : paint.measureText(info2);
		result = (int) Math.min(w1, w2);
		return result;
	}

	private int measureInfoHeight() {
		int result = 0;
		if (info1 != null) {
			paint.setTextSize(info1TextSize);
			FontMetricsInt fm = paint.getFontMetricsInt();
			info1Height = Math.abs(fm.ascent) + fm.descent;
			result += info1Height;
		}
		if (info2 != null) {
			paint.setTextSize(info2TextSize);
			FontMetricsInt fm = paint.getFontMetricsInt();
			info2Height = Math.abs(fm.ascent) + fm.descent;
			result += info2Height;
		}
		if (info1 != null && info2 != null)
			result += gapInfo1AndInfo2;

		return result;
	}

	public Bitmap getIcon() {
		return icon;
	}

	public void setIcon(Bitmap icon) {
		this.icon = icon;
	}

	public int getGapIconAndInfo() {
		return gapIconAndInfo;
	}

	public void setGapIconAndInfo(int gapIconAndInfo) {
		this.gapIconAndInfo = gapIconAndInfo;
	}

	public int getGapInfo1AndInfo2() {
		return gapInfo1AndInfo2;
	}

	public void setGapInfo1AndInfo2(int gapInfo1AndInfo2) {
		this.gapInfo1AndInfo2 = gapInfo1AndInfo2;
	}

	public String getInfo1() {
		return info1;
	}

	public void setInfo1(String info1) {
		this.info1 = info1;
	}

	public String getInfo2() {
		return info2;
	}

	public void setInfo2(String info2) {
		this.info2 = info2;
	}

	public int getInfo1Height() {
		return info1Height;
	}

	public void setInfo1Height(int info1Height) {
		this.info1Height = info1Height;
	}

	public int getInfo1TextSize() {
		return info1TextSize;
	}

	public void setInfo1TextSize(int info1TextSize) {
		this.info1TextSize = info1TextSize;
	}

	public int getInfo1TextColor() {
		return info1TextColor;
	}

	public void setInfo1TextColor(int info1TextColor) {
		this.info1TextColor = info1TextColor;
	}

	public int getInfo2Height() {
		return info2Height;
	}

	public void setInfo2Height(int info2Height) {
		this.info2Height = info2Height;
	}

	public int getInfo2TextSize() {
		return info2TextSize;
	}

	public void setInfo2TextSize(int info2TextSize) {
		this.info2TextSize = info2TextSize;
	}

	public int getInfo2TextColor() {
		return info2TextColor;
	}

	public void setInfo2TextColor(int info2TextColor) {
		this.info2TextColor = info2TextColor;
	}
}
