package com.example.smartupdate.upgrade;

import ie.wombat.jbdiff.JBPatch;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Locale;

import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.example.smartupdate.BaseActivity;
import com.example.smartupdate.MyApplication;
import com.example.smartupdate.R;
import com.example.smartupdate.http.HttpRequest;
import com.example.smartupdate.http.JsonResponseHandler;

public class UpgradeManager {
	private static final String TAG = UpgradeManager.class.getName();
	private static final String VERSION_URL = "http://192.168.1.104/upgrade";
	
	private Context context;
	private PackageManager packageManager;
	private String VERSION;
	private boolean checking;
	private boolean auto;
	private boolean downloading;
	private File apkFile;
	UpgradeInfo upgradeInfo;

	private JsonResponseHandler checkUpgradeResonseHandler;
	private HttpRequest checkUpgradeRequest;

	private ApkDownloadHandler apkDownloadHandler;
	private ApkDownloadTask task;

	private static UpgradeManager instance;

	private UpgradeManager() {
		context = MyApplication.getInstance();
		packageManager = context.getPackageManager();
		checkUpgradeResonseHandler = new JsonResponseHandler() {

			@Override
			public void onRequestSuccess(Object data, Object extra) {
				final JSONObject json = (JSONObject) data;
				if (json.optBoolean("upgrade")) {
					upgradeInfo = new UpgradeInfo();
					upgradeInfo.version = json.optString("version");
					upgradeInfo.url = json.optString("url");
					upgradeInfo.patch = json.optBoolean("patch");
					upgradeInfo.sha1 = json.optString("sha1").getBytes();
					upgradeInfo.size = json.optInt("size");
					if(upgradeInfo.patch){
						upgradeInfo.patch_sha1 = json.optString("patch_sha1").getBytes();
						upgradeInfo.patched_file = Environment.getExternalStorageDirectory() + "/download/" + context.getPackageName() + "." + upgradeInfo.version + ".apk";
					}
					AlertDialog dialog = new AlertDialog.Builder(
							BaseActivity.getCurrentActivity()).setCancelable(false)
							.setTitle("Prompt")
							.setMessage(
									"new version of "
											+ upgradeInfo.version
											+ " is available, do you want to update to the newest version?")
							.setNegativeButton("Yes",
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											upgrade();
											dialog.dismiss();
										}
									})
							.setPositiveButton("No",
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											dialog.dismiss();
										}
									}).create();
					dialog.show();
				} else {
					if (!auto) {
						Toast.makeText(BaseActivity.getCurrentActivity(),
								"You are already at the newest version!",
								Toast.LENGTH_LONG).show();
					}
				}
			}

			@Override
			public void onComplete(int statusCode) {
				if (!auto) {
					dismissLoadingDialog();
				}
			}

			@Override
			public void onError(int errorCode, String errorMessage, Object extra) {
				Toast.makeText(
						context,
						"Checking upgrade failed, please try it later! "
								+ errorMessage, Toast.LENGTH_LONG).show();
			}

		};
	}

	public static UpgradeManager getInstance() {
		if (null == instance) {
			instance = new UpgradeManager();
		}
		return instance;
	}

	public String getVersion() {
		if (null == VERSION) {
			try {
				PackageInfo pInfo = packageManager.getPackageInfo(
						context.getPackageName(), 0);
				VERSION = pInfo.versionName;
			} catch (NameNotFoundException e) {
				Log.e(TAG, "error getting version info", e);
			}
		}
		return VERSION;
	}

	public void checkVersion(boolean auto) {
		this.auto = auto;
		if (checking) {
			return;
		}
		if (null != checkUpgradeRequest) {
			checkUpgradeRequest.cancel();
			checkUpgradeRequest = null;
		}
		checkUpgradeRequest = new HttpRequest();
		checkUpgradeRequest.setListener(checkUpgradeResonseHandler);
		checkUpgradeRequest.sendRequest(VERSION_URL,
				Collections.singletonMap("version", getVersion()), true);
		if (!auto) {
			showLoadingDialog();
		}
	}

	public String getApk() {
		return context.getApplicationInfo().publicSourceDir;
	}

	private ProgressDialog mProgressDialog;

	private void showLoadingDialog() {
		if (mProgressDialog == null) {
			mProgressDialog = new ProgressDialog(
					BaseActivity.getCurrentActivity());
			mProgressDialog.setMessage(context
					.getString(R.string.progress_loading));
			mProgressDialog.setCanceledOnTouchOutside(false);
			mProgressDialog.setCancelable(true);
		}
		mProgressDialog.show();
	}

	private void dismissLoadingDialog() {
		if (mProgressDialog != null && mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
	}

	private void upgrade() {
		download();
	}

	private void download(){
		String formedUrl = upgradeInfo.url.toLowerCase(Locale.getDefault());
		if (formedUrl.endsWith(".apk") || formedUrl.endsWith(".patch")) {
			apkDownloadHandler = new ApkDownloadHandler(context);
			startDownloadApkFileTask(apkDownloadHandler);
		} else {
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_VIEW);
			Uri uri = Uri.parse(upgradeInfo.url);
			intent.setData(uri);
			BaseActivity.getCurrentActivity().startActivity(intent);
		}
	}

	private void startDownloadApkFileTask(final ApkDownloadHandler handler) {
		task = new ApkDownloadTask(upgradeInfo, new ApkDownloadListener() {

			@Override
			public void onStart(String url) {
				downloading = true;
				handler.obtainMessage(ApkDownloadHandler.START, url).sendToTarget();
			}

			@Override
			public void onSuccess(File apkFile) {
				UpgradeManager.this.apkFile = apkFile;
				handler.obtainMessage(ApkDownloadHandler.SUCCESS).sendToTarget();
				clear();
			}

			@Override
			public void onProgress(int downloadSize, int totalSize) {
				handler.obtainMessage(ApkDownloadHandler.PROGRESS, downloadSize, totalSize).sendToTarget();
			}

			@Override
			public void onError() {
				handler.obtainMessage(ApkDownloadHandler.ERROR).sendToTarget();
				clear();
			}

			@Override
			public void onCancel() {
				handler.obtainMessage(ApkDownloadHandler.CANCEL).sendToTarget();
				clear();
			}

		});
		new Thread(task).start();
	}

	public void install(){
		if(apkDownloadHandler != null){
			apkDownloadHandler.cancelNotification();
		}
		if(!upgradeInfo.patch || (upgradeInfo.patch && patch())){
			_install();
		}
	}

	private boolean patch(){
		File old = new File(getApk());
		File newFile = new File(upgradeInfo.patched_file);
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(newFile);
			JBPatch.bspatch(old, fos, apkFile);
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			if(null != fos){
				try {
					fos.close();
				} catch (IOException e) {
				}
			}
		}
		if(!newFile.exists()){
			Toast.makeText(context, "patching failed!", Toast.LENGTH_LONG).show();
			return false;
		}else{
			apkFile = newFile;
			return true;
		}
	}

	private void _install(){
		Intent installIntent = new Intent(Intent.ACTION_VIEW)
		.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
		BaseActivity.getCurrentActivity().startActivity(installIntent);
	}

	private void clear(){
		task = null;
		downloading = false;
	}

	public boolean isDownloading(){
		return downloading;
	}

	public void cancel(){
		if(task != null){
			task.cancel();
		}
		if(apkDownloadHandler != null){
			apkDownloadHandler.cancelNotification();
		}
	}
}
