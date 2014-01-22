package com.example.smartupdate.upgrade;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.Date;

import android.os.Environment;
import android.util.Log;

import com.example.smartupdate.MessageDigestHelper;
import com.example.smartupdate.http.NetworkUtils;

public class ApkDownloadTask implements Runnable {

	private static final String TAG = ApkDownloadTask.class.getName();
	private UpgradeInfo upgradeInfo;
	private ApkDownloadListener listener;
	private volatile boolean canceled = false;

	public ApkDownloadTask(UpgradeInfo upgradeInfo, ApkDownloadListener listener) {
		this.upgradeInfo = upgradeInfo;
		this.listener = listener;
		if (null == listener) {
			throw new RuntimeException("listener must be specified!");
		}
	}

	@Override
	public void run() {
		download();
	}

	private void download() {
		listener.onStart(upgradeInfo.url);
		// check if already downloaded and patched
		if (upgradeInfo.patch) {
			File patchDest = new File(upgradeInfo.patched_file);
			if (patchDest.exists()
					&& MessageDigestHelper.confirmSHA1Sum(patchDest,
							upgradeInfo.sha1)) {
				// already patched!
				upgradeInfo.patch = false;
				listener.onSuccess(patchDest);
				return;
			}
		}

		int filenameIndex = upgradeInfo.url.lastIndexOf("/") + 1;
		String filename;
		if (filenameIndex != 0 && filenameIndex != upgradeInfo.url.length()) {
			filename = upgradeInfo.url.substring(filenameIndex);
		} else {
			filename = new Date().getTime() + ".apk";
		}

		String path = Environment.getExternalStorageDirectory() + "/download/";
		File dir = new File(path);
		dir.mkdirs();
		File outputFile = new File(dir, filename);
		if (outputFile.exists()
				&& MessageDigestHelper.confirmSHA1Sum(outputFile,
						upgradeInfo.patch ? upgradeInfo.patch_sha1
								: upgradeInfo.sha1)) {
			listener.onSuccess(outputFile);
			return;
		}
		outputFile.deleteOnExit();
		File tmp = new File(outputFile.getAbsolutePath() + ".tmp");
		FileOutputStream fos = null;
		InputStream is = null;
		HttpURLConnection conn = null;
		try {
			conn = (HttpURLConnection) NetworkUtils
					.getConnection(upgradeInfo.url);
			if (null == conn
					|| conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
				listener.onError();
				return;
			}
			fos = new FileOutputStream(outputFile);
			is = conn.getInputStream();
			int totalSize = upgradeInfo.size;
			int currentSize = 0;
			byte[] buffer = new byte[1024];
			int len = -1;
			while ((len = is.read(buffer)) != -1) {
				if (canceled) {
					tmp.deleteOnExit();
					listener.onCancel();
					return;
				}
				fos.write(buffer, 0, len);
				currentSize += len;
				listener.onProgress(currentSize, totalSize);
			}
			tmp.renameTo(outputFile);
			Runtime.getRuntime().exec("chmod 777 " + outputFile);
		} catch (Exception e) {
			Log.e(TAG, "", e);
			listener.onError();
			return;
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
				}
			}
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
				}
			}
			if (conn != null) {
				conn.disconnect();
			}
		}
		listener.onSuccess(outputFile);
	}

	public void cancel() {
		this.canceled = true;
	}
}
