package com.oumen.widget.dialog;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.oumen.R;

public class ClipView extends View {

	public ClipView(Context context) {
		super(context);
	}

	public ClipView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ClipView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		int width = this.getWidth();
		int height = this.getHeight();

		Paint paint = new Paint();
		paint.setColor(0xaa000000);
		int size = width;
		
		// left
//		canvas.drawRect(0, (height -  size) / 2, (width -  size) / 2, (height -  size) / 2 +  size, paint);
		// right
//		canvas.drawRect((width -  size) / 2 +  size, (height -  size) / 2, width, (height -  size) / 2 + size, paint);
		// bottom
		canvas.drawRect(0, (height - size) / 2 + size + 1, width, height, paint);

		paint.setColor(getResources().getColor(R.color.white));
		// top 
		canvas.drawLine(1, (height - size) / 2, size, (height - size) / 2, paint);
		//left
		canvas.drawLine(1, (height - size) / 2, 1, (height - size) / 2 + size, paint);
		//right
		canvas.drawLine(size, (height - size) / 2, size, (height - size) / 2 + size, paint);
		//buttom
		canvas.drawLine(1, (height - size) / 2 + size, size, (height - size) / 2 + size, paint);
	}

}
