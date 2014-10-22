package com.oumen.widget.image.shape;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;

public class RoundRectangleImageView extends BaseImageView {
	protected int radius;

	public RoundRectangleImageView(Context context) {
		super(context);
	}

	public RoundRectangleImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public RoundRectangleImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

    public static Bitmap getBitmap(int width, int height, int raidus) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.BLACK);
        canvas.drawRoundRect(new RectF(0.0f, 0.0f, width, height), raidus, raidus, paint);
        return bitmap;
    }

	@Override
	public Bitmap getBitmap() {
        return getBitmap(getWidth(), getHeight(), radius);
	}

	public int getRadius() {
		return radius;
	}

	public void setRadius(int radius) {
		this.radius = radius;
	}
}
