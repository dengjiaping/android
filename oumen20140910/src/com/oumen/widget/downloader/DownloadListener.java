package com.oumen.widget.downloader;

public interface DownloadListener {
	void onProgressUpdate(DownloadTask task);
	
	void onStart(DownloadTask task);
	
	void onCompleted(DownloadTask task);
	
	void onDisconnect(DownloadTask task);
	
	void onFailed(DownloadTask task);
}
