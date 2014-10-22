package com.oumen.mv;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.oumen.R;
import com.oumen.android.App;

public class SelectableVideoView extends FrameLayout {
	private ImageView imgView;
	private ImageView imgCheck;
	private TextView txtTime;

	private VideoData data;
	
	public SelectableVideoView(Context context) {
		this(context, null, 0);
	}

	public SelectableVideoView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SelectableVideoView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.selectable_video_view, this, true);
		
		imgView = (ImageView)findViewById(R.id.image);
		imgCheck = (ImageView)findViewById(R.id.checkbox);
		txtTime = (TextView)findViewById(R.id.time);
	}
	
	public ImageView getImageView() {
		return imgView;
	}

	public void setImage(Bitmap img) {
		imgView.setImageBitmap(img);
	}
	
	public boolean isChecked() {
		return data.selected;
	}

	public void update(VideoData data) {
		this.data = data;
		imgCheck.setVisibility(data.selected ? View.VISIBLE : View.GONE);
		imgView.setImageBitmap(data.frame);
		int duration = data.duration / 1000;
		int minutes = duration / 60, seconds = duration % 60;
		txtTime.setText(App.NUMBER_FORMAT.format(minutes) + ":" + App.NUMBER_FORMAT.format(seconds));
	}
	
	public VideoData getData() {
		return data;
	}
	
	public static class VideoData {
		String path;
		Bitmap frame;
		int duration;
		boolean selected;
	}
}
