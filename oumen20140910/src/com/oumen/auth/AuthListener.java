package com.oumen.auth;


public interface AuthListener {
	public void onComplete();
	
	public void onCancel();
	
	public void onFailed(Object obj);
}
