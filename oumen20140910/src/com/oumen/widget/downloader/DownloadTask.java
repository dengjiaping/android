package com.oumen.widget.downloader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

import com.oumen.tools.ELog;

public class DownloadTask implements Runnable {
	protected final String url;
	protected final String local;
	protected HttpURLConnection connection;
	
	protected boolean disconnect = false;
	protected boolean downloading = false;
	protected boolean completed = false;
	protected boolean failed = false;
	protected long seek = 0;
	protected long total = 0;
	
	protected DownloadListener listener;
	
	public DownloadTask(String url, String local, long total) {
		this.url = url;
		this.local = local;
		this.total = total;
		
		File f = new File(local);
		if (f.exists() && f.length() == total) {
			completed = true;
		}
	}
	
	public DownloadTask(String url, String local) {
		this.url = url;
		this.local = local;
		
		File f = new File(local);
		if (f.exists()) {
			seek = f.length();
		}
	}

	@Override
	public void run() {
		RandomAccessFile file = null;
		InputStream is = null;
		
		if (completed)
			return;
		
		try {
			downloading = true;
			completed = false;
			disconnect = false;
			failed = false;
			
			File f = new File(local);
			if (!f.getParentFile().exists())
				f.getParentFile().mkdirs();
			if (!f.exists()) {
				try {
					f.createNewFile();
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			file = new RandomAccessFile(local, "rw");
			seek = file.length();
			file.seek(seek);
			
			URL url = new URL(this.url);
			connection = (HttpURLConnection)url.openConnection();
			connection.setRequestProperty("User-Agent","NetFox"); 
			connection.setRequestProperty("RANGE","bytes=" + seek + "-");
			
			if (total == 0) {
				total = connection.getContentLength();
				if (total == 0) {
					total = file.length();
				}
			}
			
			if (listener != null) {
				listener.onStart(this);
			}
			
			ELog.i("Url:" + url + " Local:" + local + " Current:" + seek + " Total:" + total);
			
			byte[] buf = new byte[1024 * 64];
			is = connection.getInputStream();
			
			int avaiable = 0;
			while ((avaiable = is.read(buf)) != -1) {
				file.write(buf, 0, avaiable);
				seek += avaiable;
				if (listener != null) {
					listener.onProgressUpdate(this);
				}
			}
			completed = true;
			total = f.length();
			
			ELog.i("Compeleted");
			
			if (listener != null) {
				listener.onCompleted(this);
			}
		}
		catch (IOException e) {
			ELog.e("Exception:" + e.getMessage());
			e.printStackTrace();
			if (disconnect) {
				if (listener != null) {
					listener.onDisconnect(this);
				}
			}
			else {
				failed = true;
				if (listener != null) {
					listener.onFailed(this);
				}
			}
		}
		finally {
			downloading = false;
			try {
				if (is != null) {
					is.close();
				}
				if (file != null) {
					file.close();
				}
				if (connection != null) {
					connection.disconnect();
					connection = null;
				}
			}
			catch (Exception e){}
		}
	}
	
	public void disconnect() {
		if (connection != null) {
			disconnect = true;
			connection.disconnect();
			connection = null;
		}
	}
	
	public boolean isDownloading() {
		return downloading;
	}
	
	public boolean isCompleted() {
		return completed;
	}
	
	public boolean isDisconnect() {
		return disconnect;
	}
	
	public boolean isFailed() {
		return failed;
	}

	public String getUrl() {
		return url;
	}

	public String getLocal() {
		return local;
	}
	
	public long getCurrent() {
		return seek;
	}

	public long getTotal() {
		return total;
	}
	
	public void setTotal(long total) {
		this.total = total;
	}
	
	public void reset() {
		seek = 0;
		failed = false;
		disconnect = false;
		downloading = false;
		completed = false;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof DownloadTask) {
			DownloadTask target = (DownloadTask) o;
			return url.equals(target.url);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return url.hashCode();
	}

	@Override
	public String toString() {
		return "Url:" + url + " Local:" + local + " isDisconnect:" + disconnect;
	}
	
	public void setListener(DownloadListener listener) {
		this.listener = listener;
	}
}
