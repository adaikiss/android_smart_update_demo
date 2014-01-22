/**
 * 
 */
package com.example.smartupdate.http;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import com.example.smartupdate.MyApplication;

/**
 * @author hlw
 * 
 */
public class NetworkUtils {
	public static final String CHARSET = "UTF-8";
	static final String TAG = NetworkUtils.class.getName();

	public enum RequestType {
		POST, GET
	}

	private static ConnectivityManager connectivityManager;
	static{
		enableHttpResponseCache();
	}

	private static ConnectivityManager getConnectivityManager() {
		if (null == connectivityManager) {
			connectivityManager = (ConnectivityManager) MyApplication
					.getInstance().getSystemService(
							Context.CONNECTIVITY_SERVICE);
		}
		return connectivityManager;
	}

	public static boolean isConnected() {
		try {
			NetworkInfo netInfo = getConnectivityManager()
					.getActiveNetworkInfo();
			if (netInfo != null
					&& netInfo.getState() == NetworkInfo.State.CONNECTED) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	private static void enableHttpResponseCache() {
	    try {
	        long httpCacheSize = 10 * 1024 * 1024; // 10 MiB
	        File httpCacheDir = new File(MyApplication.getInstance().getCacheDir(), "http");
	        Class.forName("android.net.http.HttpResponseCache")
	            .getMethod("install", File.class, long.class)
	            .invoke(null, httpCacheDir, httpCacheSize);
	    } catch (Exception httpResponseCacheNotAvailable) {
	    }
	}

	public static void disableConnectionReuseIfNecessary() {
		// HTTP connection reuse which was buggy pre-froyo
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
			System.setProperty("http.keepAlive", "false");
		}
	}

	public static HttpURLConnection getConnection(String urlStr)
			throws IOException {
		URL url = new URL(urlStr);
		NetworkInfo activeNetworkInfo = connectivityManager
				.getActiveNetworkInfo();
		if (null == activeNetworkInfo) {
			Log.w(TAG, "No active network available!");
			return null;
		}
		Proxy p = getProxy();
		String extra = activeNetworkInfo.getExtraInfo();
		// chinese mobile network
		if (activeNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE
				&& p != null) {
			// cmwap, uniwap, 3gwap
			if (extra != null
					&& (extra.startsWith("cmwap") || extra.startsWith("uniwap") || extra
							.startsWith("3gwap"))) {
				HttpURLConnection conn = (HttpURLConnection) new URL("http://"
						+ p.address().toString() + url.getPath())
						.openConnection();
				conn.setRequestProperty("X-Online-Host", url.getHost() + ":"
						+ (url.getPort() == -1 ? "80" : url.getPort()));
				return conn;
			}
		}
		if (null != p) {
			// through proxy
			return (HttpURLConnection) url.openConnection(p);
		} else {
			return (HttpURLConnection) url.openConnection();
		}
	}

	/**
	 * get system proxy, or null if no proxy is set.
	 * 
	 * @return
	 */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	public static Proxy getProxy() {
		@SuppressWarnings("deprecation")
		String proxyName = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 ? Settings.Global.HTTP_PROXY
				: Settings.Secure.HTTP_PROXY;
		String proxyStr = Settings.Secure.getString(MyApplication.getInstance()
				.getContentResolver(), proxyName);
		if (null == proxyStr) {
			return null;
		}
		String[] proxyStringSplits = proxyStr.split(":");
		String proxyAddress = proxyStringSplits[0];
		int proxyPort = Integer.parseInt(proxyStringSplits[1]);
		return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyAddress,
				proxyPort));
	}

	public static boolean isWIFI() {
		ConnectivityManager cm = (ConnectivityManager) MyApplication
				.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		return netInfo.getType() == ConnectivityManager.TYPE_WIFI;
	}
}
