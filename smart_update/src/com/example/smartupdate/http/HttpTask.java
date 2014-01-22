package com.example.smartupdate.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;

import org.apache.http.HttpStatus;

import android.util.Log;

public class HttpTask implements Runnable {
	private static final String TAG = HttpTask.class.getName();
	public static final int DEFAULT_CONNECT_TIMEOUT = 10000; // 10seconds
	private String url;
	private HttpTaskListener listener;
	private boolean canceled = false;
	private int timeout;
	private byte[] data;
	/**
	 * third part url, we need to simulate a browser's header to prevent the
	 * robot-keep-away
	 */
	private boolean thirdPartUrl = false;
	private NetworkUtils.RequestType requestType;

	public HttpTask(String url, byte[] data, HttpTaskListener listener,
			int timeout) {
		this.url = url;
		this.data = data;
		this.timeout = timeout;
		this.listener = listener;
	}

	public HttpTask(String url, byte[] data, HttpTaskListener listener,
			int timeout, boolean thirdPartUrl) {
		this(url, data, listener, timeout);
		this.thirdPartUrl = thirdPartUrl;
	}

	public HttpTask(String url, byte[] data, HttpTaskListener listener) {
		this(url, data, listener, DEFAULT_CONNECT_TIMEOUT);
	}

	@Override
	public void run() {
		if (canceled) {
			return;
		}
		if (!NetworkUtils.isConnected()) {
			if (listener != null) {
				listener.onComplete(HttpTaskListener.STATUS_NOT_COMPLETE);
				listener.onError(-100,
						"connection is unavailable, please check your network！");
				return;
			}
		}
		HttpURLConnection conn = null;
		boolean completed = false;
		try {
			conn = NetworkUtils.getConnection(url);
			// this connection is aborted.
			if (canceled) {
				conn.disconnect();
				conn = null;
				return;
			}
			// simulate a browser header
			if (thirdPartUrl) {
				conn.setDoOutput(true);
				conn.setRequestProperty("Pragma", "no-cache");
				conn.setRequestProperty("Cache-Control", "no-cache");
				conn.setRequestProperty("User-Agent",
						"Mozilla/5.0 (Windows NT 5.1; rv:23.0) Gecko/20100101 Firefox/23.0");
			}
			conn.setConnectTimeout(timeout);
			if (requestType == NetworkUtils.RequestType.POST) {
				conn.setRequestMethod("POST");
				conn.setDoOutput(true);
				OutputStream ops = conn.getOutputStream();
				if (data != null && data.length > 0) {
					ops.write(data);
					ops.flush();
				}
			} else {
				conn.setRequestMethod("GET");
			}
			if (canceled) {
				conn.disconnect();
				conn = null;
				return;
			} else {
				conn.connect();
			}
			int statusCode = conn.getResponseCode();
			if (listener != null && !canceled) {
				listener.onComplete(statusCode);
				completed = true;
			}
			InputStream in = null;
			if (statusCode != HttpStatus.SC_OK
					&& statusCode != HttpStatus.SC_PARTIAL_CONTENT) {
				// error
				in = conn.getErrorStream();
				if (listener != null && !canceled) {
					listener.onError(statusCode, getResponseString(in));
				}
				return;
			}
			// success
			in = conn.getInputStream();
			if (listener != null && !canceled) {
				listener.onSuccess(getResponseString(in));
			}
		} catch (IOException e) {
			Log.e(TAG, "request failed!", e);
			if (listener != null && !canceled) {
				if (!completed) {
					listener.onComplete(HttpTaskListener.STATUS_NOT_COMPLETE);
				}
				listener.onError(-1, "request failed!");
			}
		}
	}

	private String getResponseString(InputStream in) throws IOException {
		byte[] buff = new byte[1024];
		int length = -1;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		while ((length = in.read(buff)) != -1) {
			baos.write(buff, 0, length);
		}
		return new String(baos.toByteArray(), NetworkUtils.CHARSET);
	}

	public void cancel() {
		this.canceled = true;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public void setThirdPartUrl(boolean thirdPartUrl) {
		this.thirdPartUrl = thirdPartUrl;
	}

	public void setRequestType(NetworkUtils.RequestType requestType) {
		this.requestType = requestType;
	}
}
