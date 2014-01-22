package com.example.smartupdate.upgrade;

import java.io.File;

public interface ApkDownloadListener {
	public void onStart(String url);
	public void onSuccess(File file);
	public void onError();
	public void onCancel();
	public void onProgress(int currentSize, int totalSize);
}
