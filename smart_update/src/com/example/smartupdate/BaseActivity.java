package com.example.smartupdate;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class BaseActivity extends FragmentActivity {

	private static Activity CURRENT;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		CURRENT = this;
	}

	public static Activity getCurrentActivity(){
		return CURRENT;
	}
}
