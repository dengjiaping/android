package com.oumen.mv;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.oumen.R;
import com.oumen.mv.VideoPlayerView.PlayListener;

public class VideoListItem extends FrameLayout {
	private TextView txtTitle;
	private TextView txtDate;
	private VideoPlayerView player;
	private ImageButton btnLeftBottom;
	private Button btnRightBottom;
	private ProgressBar progress;

	public VideoListItem(Context context) {
		this(context, null, 0);
	}

	public VideoListItem(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public VideoListItem(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.video_list_item, this, true);

		txtTitle = (TextView) findViewById(R.id.nav_title);
		txtDate = (TextView) findViewById(R.id.date);
		player = (VideoPlayerView) findViewById(R.id.player);
		btnLeftBottom = (ImageButton) findViewById(R.id.btn_left_bottom);
		btnRightBottom = (Button) findViewById(R.id.btn_right_bottom);
		progress = (ProgressBar) findViewById(R.id.pb_progress);
		progress.setVisibility(View.GONE);
	}

	public void setProgressVisible(boolean isVisible) {
		if (isVisible) {
			progress.setVisibility(View.VISIBLE);
		}
		else {
			progress.setVisibility(View.GONE);
		}
	}

	public void setProgress(int length) {
		progress.setProgress(length);
	}

	public String getTitle() {
		return txtTitle.getText().toString();
	}

	public String getDate() {
		return txtDate.getText().toString();
	}

	public void setWidth(int width) {
		player.setWidth(width);
	}

	public void setWidthAndHeight(int width, int height) {
		player.setWidthAndHeight(width, height);
	}

	public void setPlayListener(PlayListener listener) {
		player.setPlayListener(listener);
	}

	public void setLeftBottomListener(View.OnClickListener listener) {
		btnLeftBottom.setOnClickListener(listener);
	}

	public void setRightBottomListener(View.OnClickListener listener) {
		btnRightBottom.setOnClickListener(listener);
	}

	public void setRightBottomButtonText(int resId) {
		btnRightBottom.setText(resId);
	}

	public String getRightBottomButtonText() {
		return btnRightBottom.getText().toString();
	}

	public void setVideo(MvInfo info) {
		String path = info.getPath();
		txtTitle.setText(info.title);
		txtDate.setText(info.getCreateAtString());
		btnLeftBottom.setTag(info);
		btnRightBottom.setTag(info);
		player.setVideo(path, info.prefix.getCoverLocalUrl());
	}

	public String getVideo() {
		return player.getVideo();
	}

	public boolean isPlaying() {
		return player.isPlaying();
	}

	public void stop() {
		player.stop();
	}

	public void pause() {
		player.pause();
	}

	public void resume() {
		player.resume();
	}
}
