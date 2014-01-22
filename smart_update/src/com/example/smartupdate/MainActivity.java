package com.example.smartupdate;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.example.smartupdate.upgrade.UpgradeManager;

public class MainActivity extends BaseActivity implements OnClickListener {

	private static final String TAG = MainActivity.class.getName();
	private TextView mVersionView;
	private Button mCheckUpgradeBtn;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mVersionView = (TextView) findViewById(R.id.version);
		mVersionView.setText(getString(R.string.label_version)
				+ UpgradeManager.getInstance().getVersion());
		mCheckUpgradeBtn = (Button) findViewById(R.id.check);
		mCheckUpgradeBtn.setOnClickListener(this);
		findViewById(R.id.source_dir).setOnClickListener(this);
		if(UpgradeManager.getInstance().isDownloading()){
			mCheckUpgradeBtn.setText(getString(R.string.cancel_upgrade));
		}
	}

	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.source_dir){
			Log.d(TAG, getApplicationInfo().publicSourceDir);
			return;
		}
		String text = ((Button)v).getText().toString();
		if(text.equals(getString(R.string.check_upgrade))){
			checkUpgrade();
			return;
		}
		if(text.equals(getString(R.string.install_upgrade))){
			installUpgrade();
			return;
		}
		if(text.equals(getString(R.string.cancel_upgrade))){
			cancelUpgrade();
			return;
		}
		checkUpgrade();
	}

	private void checkUpgrade() {
		UpgradeManager.getInstance().checkVersion(false);
	}

	private void installUpgrade(){
		UpgradeManager.getInstance().install();
	}
	private void cancelUpgrade(){
		UpgradeManager.getInstance().cancel();
	}
}
