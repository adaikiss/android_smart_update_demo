package com.example.smartupdate.http;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class JsonResponseHandler extends OnResponseHandler {
	private static final String TAG = JsonResponseHandler.class.getName();

	public static final String JSON_FIELD_SUCCESS = "success";
	public static final String JSON_FIELD_ERROR_TYPE = "errorType";
	public static final String JSON_FIELD_MSG = "msg";
	public static final int DEFAULT_ERROR_TYPE = -2;

	/** 
	 *  subclasses should not override this method.
	 */
	@Override
	public void onSuccess(String response, Object extra) {
		Log.d(TAG, "response:");
		Log.d(TAG, response);
		try {
			JSONObject json = new JSONObject(response);
			if (!json.optBoolean(JSON_FIELD_SUCCESS)) {
				onError(json.optInt(JSON_FIELD_ERROR_TYPE, DEFAULT_ERROR_TYPE),
						json.optString(JSON_FIELD_MSG), extra);
				return;
			}

			// handle other global data, such as sessionid.

			// handle data
			// treat as JSONObject, and if failed, JSONArray is the fallback.
			Object data = json.optJSONObject("data");
			if (null == data) {
				data = json.optJSONArray("data");
			}
			onRequestSuccess(data, extra);
		} catch (JSONException e) {
			Log.e(TAG, "malformed json data!", e);
			onError(DEFAULT_ERROR_TYPE, "malformed json data!", extra);
		}
	}

	/**
	 * subclasses should override this method to handle JSON data.
	 * 
	 * @param data
	 *            a JSONObject or JSONArray
	 * @param extra
	 */
	public void onRequestSuccess(Object data, Object extra) {

	}
}
