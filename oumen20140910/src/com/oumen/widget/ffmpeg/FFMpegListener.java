package com.oumen.widget.ffmpeg;

public interface FFMpegListener {
	public enum Phase {RUNNING, PROGRESS, COMPLETED, FAILED};
	
	void onEvent(Phase phase, FFMpeg target, Object data);
}
