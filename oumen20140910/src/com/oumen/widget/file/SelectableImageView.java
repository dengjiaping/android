package com.oumen.widget.file;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.oumen.R;

public class SelectableImageView extends FrameLayout {
	private ImageView imgView;
	private ImageView imgCheck;

	private ImageData data;
	
	public SelectableImageView(Context context) {
		this(context, null, 0);
	}

	public SelectableImageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SelectableImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.selectable_image_view, this, true);
		
		imgView = (ImageView)findViewById(R.id.image);
		imgCheck = (ImageView)findViewById(R.id.checkbox);
	}
	
	public ImageView getImageView() {
		return imgView;
	}

	public void setImage(Bitmap img) {
		imgView.setImageBitmap(img);
	}
	
	public boolean isChecked() {
		return data.select;
	}

	public void update(ImageData data) {
		this.data = data;
		imgCheck.setImageResource(data.select ? R.drawable.icon_checked : R.drawable.icon_unchecked);
	}
	
	public ImageData getData() {
		return data;
	}
}
