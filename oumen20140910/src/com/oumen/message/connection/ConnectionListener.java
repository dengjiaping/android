package com.oumen.message.connection;

public interface ConnectionListener {
	public void input(Object in);
	
	public void onSendFailed(String reference, Exception e);
}
