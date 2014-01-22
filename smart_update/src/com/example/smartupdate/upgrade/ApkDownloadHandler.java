package com.example.smartupdate.upgrade;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.example.smartupdate.BaseActivity;
import com.example.smartupdate.MainActivity;
import com.example.smartupdate.R;

public class ApkDownloadHandler extends Handler {
	private static final String TAG = ApkDownloadHandler.class.getName();
	public static final int START = 0;
	public static final int SUCCESS = 1;
	public static final int ERROR = 2;
	public static final int PROGRESS = 3;
	public static final int CANCEL = 4;
	private static final int NOTIFICATION_ID = 1;
	public static final int NOTIFY_CANCEL = 1;
	public static final int NOTIFY_INSTALL = 2;
	private NotificationCompat.Builder builder;
	private NotificationManager notificationManager;
	private Context context;
	public ApkDownloadHandler(Context context){
		this.context = context;
	}

	private void createNotification(String url){
		builder = new NotificationCompat.Builder(
				context);
		builder.setSmallIcon(R.drawable.ic_launcher);
		builder.setContentTitle("downloading upgrade file.");
		builder.setTicker("downloading");
		builder.setContentText(url);
		Intent intent = new Intent(context, MainActivity.class);
		PendingIntent pIntent = PendingIntent
				.getService(context, 0, intent, 0);
		builder.setContentIntent(pIntent);
		builder.setAutoCancel(false);
		notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		builder.setSound(null);
	}

	@Override
	public void handleMessage(Message msg) {
		int textId = -1;
		switch(msg.what){
		case START:
			String url = (String)msg.obj;
			createNotification(url);
			textId = R.string.cancel_upgrade;
			break;
		case SUCCESS:
			builder.setContentTitle("download success!");
			builder.setProgress(0, 0, false);
			textId = R.string.install_upgrade;
			break;
		case ERROR:
			if(null != builder){
				builder.setProgress(0, 0, false);
				builder.setContentTitle("download failed!");
			}else{
				Toast.makeText(context, "download failed!", Toast.LENGTH_LONG).show();
			}
			textId = R.string.check_upgrade;
			break;
		case PROGRESS:
			Log.d(TAG, "progress:" + msg.arg1 + ", max:" + msg.arg2);
			builder.setProgress(msg.arg2, msg.arg1, false);
			textId = R.string.cancel_upgrade;
			break;
		case CANCEL:
			cancelNotification();
			break;
		}
		if(textId != -1 && MainActivity.class.isAssignableFrom(BaseActivity.getCurrentActivity().getClass())){
			((Button)BaseActivity.getCurrentActivity().findViewById(R.id.check)).setText(context.getString(textId));
		}
		if(null != builder){
			notificationManager.notify(NOTIFICATION_ID, builder.build());
		}
	}

	public void cancelNotification(){
		notificationManager.cancel(NOTIFICATION_ID);
	}
}
