package com.oumen.http;

import org.apache.http.HttpResponse;

public class ExceptionHttpResult extends HttpResult {
	private Exception exception;
	
	private boolean forceClose;

	public ExceptionHttpResult(HttpResponse response, Exception exception, boolean isForceClose) {
		super(response);
		this.exception = exception;
		this.forceClose = isForceClose;
	}

	public Exception getException() {
		return exception;
	}

	public boolean isForceClose() {
		return forceClose;
	}
}
