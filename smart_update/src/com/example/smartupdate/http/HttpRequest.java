/**
 * 
 */
package com.example.smartupdate.http;

/**
 * @author hlw
 * 
 */
public class HttpRequest extends AbstractHttpRequest {

	

	protected Object extra;

	public void notifyComplete(int statusCode) {
		if (listener != null) {
			if (listener instanceof OnResponseHandler) {
				((OnResponseHandler) listener).obtainMessage(
						OnResponseHandler.COMPLETE, statusCode).sendToTarget();
			} else {
				listener.onComplete(statusCode);
			}
		}
	}

	public void notifySuccess(String response) {
		if (listener != null) {
			if (listener instanceof OnResponseHandler) {
				((OnResponseHandler) listener).obtainMessage(
						OnResponseHandler.SUCESS,
						new Object[] { response, getExtra() }).sendToTarget();
			} else {
				listener.onSuccess(response, getExtra());
			}
		}
	}

	public void notifyError(int errorCode, String msg) {
		if (listener != null) {
			if (listener instanceof OnResponseHandler) {
				((OnResponseHandler) listener).obtainMessage(
						OnResponseHandler.ERROR, errorCode, 0,
						new Object[] { msg, getExtra() }).sendToTarget();
			} else {
				listener.onError(errorCode, msg, getExtra());
			}
		}
	}

	@Override
	public void onComplete(int statusCode) {
		notifyComplete(statusCode);

	}

	@Override
	public void onSuccess(String response) {
		notifySuccess(response);
	}

	@Override
	public void onError(int errorCode, String msg) {
		notifyError(errorCode, msg);
	}

	public Object getExtra() {
		return extra;
	}

	public void setExtra(Object extra) {
		this.extra = extra;
	}

}
