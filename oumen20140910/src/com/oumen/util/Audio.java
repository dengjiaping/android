package com.oumen.util;

import android.media.MediaPlayer;
import android.media.MediaRecorder;

import com.oumen.tools.ELog;

public class Audio implements MediaRecorder.OnInfoListener, MediaRecorder.OnErrorListener {
	private final int recordDurationMax = 60 * 1000;
	
	private final MediaRecorder recorder = new MediaRecorder();
	private boolean recording;
	
	private MediaRecorder.OnInfoListener onInfoListener;
	private MediaRecorder.OnErrorListener onErrorListener;
	
	public boolean startRecord(String targetPath) {
		try {
			recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
			recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
			recorder.setOutputFile(targetPath);
			recorder.setMaxDuration(recordDurationMax);
			recorder.setOnInfoListener(onInfoListener);
			recorder.setOnErrorListener(onErrorListener);
			recorder.prepare();
			recorder.start();
			recording = true;
		}
		catch (Exception e) {
			ELog.e("Exception:" + e.getMessage());
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public boolean stopRecord() {
		try {
			recording = false;
			recorder.stop();
			recorder.reset();
		}
		catch (Exception e) {
			ELog.e("Exception:" + e.getMessage());
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public void release() {
		recorder.release();
		player.release();
	}

	public boolean isRecording() {
		return recording;
	}

	public void setOnInfoListener(MediaRecorder.OnInfoListener onInfoListener) {
		this.onInfoListener = onInfoListener;
	}

	public void setOnErrorListener(MediaRecorder.OnErrorListener onErrorListener) {
		this.onErrorListener = onErrorListener;
	}

	@Override
	public void onInfo(MediaRecorder mr, int what, int extra) {
		if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
			recording = false;
			recorder.reset();
		}
	}

	@Override
	public void onError(MediaRecorder mr, int what, int extra) {
		if (what == MediaRecorder.MEDIA_RECORDER_ERROR_UNKNOWN) {
			recording = false;
			recorder.reset();
		}
	}
	
	private final MediaPlayer player = new MediaPlayer();
	
	public boolean playAudio(String targetPath) {
		try {
			player.setDataSource(targetPath);
			player.prepare();
			player.start();
		}
		catch (Exception e) {
			ELog.e("Exception:" + e.getMessage());
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public void stopAudio() {
		player.stop();
		player.reset();
	}
	
	public boolean isPlaying() {
		return player.isPlaying();
	}
}