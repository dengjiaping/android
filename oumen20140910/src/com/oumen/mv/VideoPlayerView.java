package com.oumen.mv;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import com.oumen.R;
import com.oumen.tools.ELog;
import com.oumen.tools.ImageTools;

public class VideoPlayerView extends RelativeLayout implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {
	protected VideoView video;
	protected ImageView imgCover;
	protected ImageView imgPlay;

	protected String path;

	protected PlayListener listener;

	public VideoPlayerView(Context context) {
		this(context, null, 0);
	}

	public VideoPlayerView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public VideoPlayerView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.video_player_base, this, true);
		video = (VideoView) findViewById(R.id.video);
		video.setOnTouchListener(touchListener);
		video.setOnCompletionListener(this);
		video.setOnErrorListener(this);
		video.setVisibility(View.GONE);

		imgCover = (ImageView) findViewById(R.id.cover);
		imgCover.setOnTouchListener(touchListener);

		imgPlay = (ImageView) findViewById(R.id.play);
		imgPlay.setImageResource(R.drawable.icon_video_play);
	}

	public void seetTo(int msec) {
		video.seekTo(msec);
	}

	public void setCover(int msec) {
		if (imgCover.getVisibility() != View.VISIBLE)
			return;

		MediaMetadataRetriever m = new MediaMetadataRetriever();
		m.setDataSource(path);

		Bitmap cover = m.getFrameAtTime(msec * 1000);
		m.release();
		imgCover.setImageBitmap(cover);
	}

	public void setPlayListener(PlayListener listener) {
		this.listener = listener;
	}

	public void setVideo(String videoPath, String coverPath) {
		if (videoPath.equals(this.path))
			return;

		this.path = videoPath;

		video.setVideoPath(videoPath);

		Bitmap cover;
		if (TextUtils.isEmpty(coverPath)) {
			MediaMetadataRetriever m = new MediaMetadataRetriever();
			m.setDataSource(videoPath);

			cover = m.getFrameAtTime();
			m.release();
			imgCover.setImageBitmap(cover);
		}
		else {
			cover = ImageTools.decodeSourceFile(coverPath);
			imgCover.setImageBitmap(cover);
		}

		imgCover.setVisibility(View.VISIBLE);

		imgPlay.setVisibility(View.VISIBLE);
	}

	public String getVideo() {
		return path;
	}

	public boolean isPlaying() {
		return video.isPlaying();
	}

	public void stop() {
		video.stopPlayback();
		imgCover.setVisibility(View.VISIBLE);
		imgPlay.setVisibility(View.VISIBLE);
	}

	public void pause() {
		video.pause();
	}

	public void resume() {
		video.resume();
	}

	public void setWidth(int width) {
		int height = width * 3 / 4;
		ViewGroup.LayoutParams params = getLayoutParams();
		params.width = width;
		params.height = height;
		setLayoutParams(params);

		params = video.getLayoutParams();
		params.width = width;
		params.height = height;
		video.setLayoutParams(params);

		params = imgCover.getLayoutParams();
		params.width = width;
		params.height = height;
		imgCover.setLayoutParams(params);
	}

	public void setWidthAndHeight(int width, int height) {
		ViewGroup.LayoutParams params = getLayoutParams();
		params.width = width;
		params.height = height;
		setLayoutParams(params);

		params = video.getLayoutParams();
		params.width = width;
		params.height = height;
		video.setLayoutParams(params);

		params = imgCover.getLayoutParams();
		params.width = width;
		params.height = height;
		imgCover.setLayoutParams(params);
	}

	@Override
	public void onCompletion(MediaPlayer player) {
		ELog.i("Play completed");
		if (listener != null)
			listener.onPlayerEvent(PlayListener.Type.STOP, path);

		if (player.isPlaying()) {
			player.stop();
		}
		imgCover.setVisibility(View.VISIBLE);
		imgPlay.setVisibility(View.VISIBLE);
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		ELog.e("Player err:" + what + "/" + extra + " Path:" + path);
		if (listener != null)
			listener.onPlayerEvent(PlayListener.Type.ERROR, path);

		video.resume();
		imgCover.setVisibility(View.VISIBLE);
		imgPlay.setVisibility(View.VISIBLE);
		return false;
	}
	
	public void setIconVisibility(int visibility) {
		imgPlay.setVisibility(visibility);
	}

	private GestureDetector gesture;

	private final View.OnTouchListener touchListener = new View.OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (gesture == null)
				gesture = new GestureDetector(v.getContext(), new SimpleGestureDetector());
			return gesture.onTouchEvent(event);
		}
	};

	private class SimpleGestureDetector extends GestureDetector.SimpleOnGestureListener {

		@Override
		public boolean onDown(MotionEvent e) {
			return true;
		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			ELog.i("");
			if (video.isPlaying()) {
				if (listener != null)
					listener.onPlayerEvent(PlayListener.Type.PAUSE, path);

				video.pause();
			}
			else if (video.getCurrentPosition() > 0) {
				videoToCenter();

				if (listener != null)
					listener.onPlayerEvent(PlayListener.Type.PLAYING, path);

				video.start();
				if (imgCover.getVisibility() == View.VISIBLE) {
					imgCover.setVisibility(View.GONE);
					imgPlay.setVisibility(View.GONE);
				}
			}
			else {
				videoToCenter();

				if (listener != null)
					listener.onPlayerEvent(PlayListener.Type.PLAYING, path);

				video.start();
				if (imgCover.getVisibility() == View.VISIBLE) {
					imgCover.setVisibility(View.GONE);
					imgPlay.setVisibility(View.GONE);
				}
			}
			return true;
		}

		@Override
		public boolean onDoubleTap(MotionEvent e) {
			ELog.i("");
			return true;
		}
	}

	private void videoToCenter() {
		ViewGroup.LayoutParams p = getLayoutParams();
		p.width = getWidth();
		p.height = getHeight();
		setLayoutParams(p);
		
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) video.getLayoutParams();
		params.width = imgCover.getWidth();
		params.height = imgCover.getHeight();
		video.setLayoutParams(params);
		video.setVisibility(View.VISIBLE);
	}

	public interface PlayListener {
		public enum Type {
			PLAYING, PAUSE, STOP, ERROR
		};

		void onPlayerEvent(Type type, String path);
	}
}
