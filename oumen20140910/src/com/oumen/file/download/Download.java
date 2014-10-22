package com.oumen.file.download;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.oumen.tools.ELog;

import android.os.Handler;
import android.os.Message;

public class Download implements Handler.Callback, Runnable {
	private final int HANDLER_UPDATE = 0;
	private final int HANDLER_EXCEPTION = 1;
	
	public interface ProgressListener {
		public void onProgressUpdate(long progress, long total);
		
		public void onException(Exception e);
	}
	
	protected final Handler handler = new Handler(this);
	
	protected String url;
	protected String savePath;
	protected ProgressListener listener;
	protected ExecutorService thread;
	protected long period = 1000;//UI更新间隔时间，默认1秒
	
	protected long progress;
	protected long total;
	protected boolean downloading;
	
	protected Timer timer;

	public Download(String url, String savePath, ProgressListener listener, ExecutorService thread) {
		this.url = url;
		this.savePath = savePath;
		this.listener = listener;
		this.thread = thread;
	}
	
	public Download(String url, String savePath, ProgressListener listener) {
		this(url, savePath, listener, Executors.newSingleThreadExecutor());
	}

	@Override
	public boolean handleMessage(Message msg) {
		ELog.i(progress+"/"+total);
		if (msg.what == HANDLER_UPDATE) {
			listener.onProgressUpdate(progress, total);
		}
		else {
			listener.onException((Exception)msg.obj);
		}
		return false;
	}

	@Override
	public void run() {
		downloading = true;
		InputStream is = null;
		OutputStream os = null;
		try {
			startTimer();
			
			URL url = new URL(this.url);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			total = connection.getContentLength();
			
			is = connection.getInputStream();
			os = new FileOutputStream(savePath);
			
			byte[] buf = new byte[1024 * 64];
			int available = 0;
			
			while (downloading && (available = is.read(buf)) != -1) {
				os.write(buf, 0, available);
				progress += available;
			}
			stopTimer();
		}
		catch (Exception e) {
			ELog.e("Exception:" + e.getMessage());
			stopTimer();
			handler.sendMessage(handler.obtainMessage(HANDLER_EXCEPTION, e));
			e.printStackTrace();
		}
		finally {
			downloading = false;
			
			if (is != null) {
				try {is.close();}catch(Exception e){}
			}
			if (os != null) {
				try {os.close();}catch(Exception e){}
			}
		}
	}
	
	protected void startTimer() {
		timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			
			@Override
			public void run() {
				handler.sendEmptyMessage(HANDLER_UPDATE);
			}
		}, 0, period);
	}
	
	protected void stopTimer() {
		handler.sendEmptyMessage(HANDLER_UPDATE);
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
	}

	public boolean isDownloading() {
		return downloading;
	}

	public void start() {
		thread.execute(this);
	}
	
	public void stop() {
		downloading = false;
	}

	public long getPeriod() {
		return period;
	}

	public void setPeriod(long period) {
		this.period = period;
	}
}
