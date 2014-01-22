package com.example.smartupdate.http;

/**
 * listener for HttpTask event.
 * 
 * @author hlw
 * 
 */
public interface OnResponseListener {
	/**
	 * called when request completed.
	 * 
	 * @param statusCode
	 */
	void onComplete(int statusCode);

	/**
	 * called when request success.
	 * 
	 * @param response
	 * @param extra extra data put to the request.
	 */
	void onSuccess(String response, Object extra);

	/**
	 * called when request error.
	 * 
	 * @param errorCode
	 * @param errorMessage
	 * @param extra extra data put to the request.
	 */
	void onError(int errorCode, String errorMessage, Object extra);
}
