package com.example.smartupdate.http;

import android.os.Handler;
import android.os.Message;

/**
 * handler implements OnResponseListener, for UI update usage.
 * 
 * @author hlw
 * 
 */
public class OnResponseHandler extends Handler implements OnResponseListener {

	public static final int SUCESS = 1;
	public static final int ERROR = 2;
	public static final int COMPLETE = 3;

	@Override
	public void onComplete(int statusCode) {

	}

	@Override
	public void onSuccess(String response, Object extra) {

	}

	@Override
	public void onError(int errorCode, String errorMessage, Object extra) {

	}

	@Override
	public void handleMessage(Message msg) {
		switch (msg.what) {
		case SUCESS: {
			onSuccess((String)((Object[]) msg.obj)[0], ((Object[]) msg.obj)[1]);
			break;
		}
		case ERROR: {
			onError(msg.arg1, ((Object[]) msg.obj)[0].toString(),
					((Object[]) msg.obj)[1]);
			break;
		}
		case COMPLETE: {
			onComplete((Integer) msg.obj);
			break;
		}
		}
	}

}
