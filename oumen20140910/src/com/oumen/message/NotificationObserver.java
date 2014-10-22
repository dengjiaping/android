package com.oumen.message;

public interface NotificationObserver {
	public enum Page {CHAT, MESSAGE, OTHER}
	
	public int getTargetId();
	
	public Page getCurrentPage();
	
	public void receiveNotification(Object... params);
}
