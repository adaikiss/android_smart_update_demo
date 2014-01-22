/**
 * 
 */
package com.example.smartupdate.http;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import android.util.Log;

import com.example.smartupdate.http.NetworkUtils.RequestType;

/**
 * @author hlw
 * 
 */
public abstract class AbstractHttpRequest implements HttpTaskListener {
	private static final String TAG = AbstractHttpRequest.class.getName();
	private HttpTask task;

	protected OnResponseListener listener;

	public HttpTask sendRequest(String url, Map<String, ?> params,
			boolean isPost) {
		return sendRequest(url, params, isPost,
				HttpTask.DEFAULT_CONNECT_TIMEOUT, false);
	}

	public HttpTask sendRequest(String url, Map<String, ?> params,
			boolean isPost, int timeout) {
		return sendRequest(url, params, isPost, timeout, false);
	}

	public HttpTask sendRequest(String url, Map<String, ?> params,
			boolean isPost, int timeout, boolean isThirdPartUrl) {
		if (isPost) {
			byte[] requestData = null;
			if (params != null && params.size() > 0) {
				StringBuilder sb = new StringBuilder();
				for (Map.Entry<String, ?> entry : params.entrySet()) {
					Object value = entry.getValue();
					if (value != null && !value.equals("")) {
						if (sb.length() > 0) {
							sb.append("&");
						}
						sb.append(entry.getKey());
						sb.append("=");
						sb.append(value);
					}
				}
				try {
					requestData = sb.toString().getBytes(NetworkUtils.CHARSET);
				} catch (UnsupportedEncodingException e) {
					Log.e(TAG, "", e);
				}
			}
			task = new HttpTask(url, requestData, this, timeout, isThirdPartUrl);
			task.setRequestType(RequestType.POST);
		} else {
			StringBuffer sb = new StringBuffer();
			int i = 0;
			if (params != null && params.size() > 0) {
				for (java.util.Map.Entry<String, ?> entry : params.entrySet()) {
					Object value = entry.getValue();
					String key = entry.getKey();
					if (key != null && !key.equals("") && value != null
							&& !value.equals("")) {
						if (i == 0) {
							sb.append("?");
						} else {
							sb.append("&");
						}
						sb.append(key);
						sb.append("=");
						sb.append(value);
						i++;
					}
				}
			}
			task = new HttpTask(url + sb.toString(), null, this, timeout,
					isThirdPartUrl);
			task.setRequestType(RequestType.GET);
		}
		HttpTaskManager.getInstance().post(task);
		return task;
	}

	public void cancel() {
		if (task != null) {
			task.cancel();
		}
	}

	public void setListener(OnResponseListener listener) {
		this.listener = listener;
	}
}
