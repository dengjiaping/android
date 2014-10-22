package com.oumen.app.callback;

public interface SelectCallback<T> {
	public void onSelect(T data);
	
	public void onCompleted(T data);
	
	public void onCancel(Object obj);
}
