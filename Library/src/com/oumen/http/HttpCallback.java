package com.oumen.http;

public interface HttpCallback {
	void setBusy(boolean busy);
	
	public boolean isBusy();
	
	public void onEvent(HttpResult response);
}
