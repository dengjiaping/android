package com.oumen.http;

import com.oumen.tools.ELog;


public class DefaultHttpCallback implements HttpCallback {
	private final EventListener mListener;
	
	private boolean busy;
	
	public DefaultHttpCallback(EventListener listener) {
		mListener = listener;
	}

	@Override
	public void setBusy(boolean busy) {
		this.busy = busy;
	}

	@Override
	public boolean isBusy() {
		return busy;
	}

	@Override
	public void onEvent(HttpResult response) {
		if (mListener == null) {
			ELog.w("EventListener is null");
			return;
		}
		
		if (response instanceof ExceptionHttpResult) {
			ExceptionHttpResult result = (ExceptionHttpResult)response;
			if (result.isForceClose()) {
				mListener.onForceClose(result);
			}
			else {
				ELog.e("Exception:" + result.getException().getMessage());
				mListener.onException(result);
			}
		}
		else if (response instanceof HttpResult) {
			mListener.onSuccess(response);
		}
	}

	public interface EventListener {
		void onSuccess(HttpResult result);
		
		void onException(ExceptionHttpResult result);
		
		void onForceClose(ExceptionHttpResult result);
	}
}
