package com.example.smartupdate.http;

public interface HttpTaskListener {
	public static final int STATUS_NOT_COMPLETE = -1;

	/**
	 * called when HttpTask complete.
	 * 
	 * @param statusCode
	 */
	public void onComplete(int statusCode);

	/**
	 * called when HttpTask success.
	 * 
	 * @param response
	 */
	public void onSuccess(String response);

	/**
	 * called when HttpTask error.
	 * 
	 * @param errorCode
	 * @param msg
	 */
	public void onError(int errorCode, String msg);
}
