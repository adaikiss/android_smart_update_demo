package com.example.smartupdate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * restart after upgrade
 * @author hlw
 *
 */
public class OnUpgradeReceiver extends BroadcastReceiver {
	public OnUpgradeReceiver() {
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_PACKAGE_REPLACED)) {
			Intent LaunchIntent = context.getPackageManager()
					.getLaunchIntentForPackage(
							context.getApplicationInfo().packageName);
			context.startActivity(LaunchIntent);
		}
	}
}
