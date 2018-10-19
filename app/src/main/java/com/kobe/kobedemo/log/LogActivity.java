package com.kobe.kobedemo.log;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.kobe.kobedemo.R;

public class LogActivity extends Activity implements OnClickListener {
	private Button copyLog, cancelLog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_log);
		copyLog = (Button) findViewById(R.id.copylog);
		copyLog.setOnClickListener(this);
		cancelLog = (Button) findViewById(R.id.cancel_copylog);
		cancelLog.setOnClickListener(this);
		LogManager.getInstance().registCopyListener(logCopyListener);
		LogManager.getInstance().setUsbLogName("KobeLog");
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.copylog:
			LogManager.getInstance().copyLogToUsb();
			break;
		case R.id.cancel_copylog:
			LogManager.getInstance().cancelCopy();
			break;

		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		LogManager.getInstance().unRegistCopyListener(logCopyListener);
	}

	private LogManager.LogCopyListener logCopyListener = new LogManager.LogCopyListener() {

		@Override
		public void onStar(int total) {
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					copyLog.setText("拷贝日志（开始）");
				}
			});

		}

		@Override
		public void onProgress(final int progress, final int total) {
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					copyLog.setText("拷贝日志(" + progress + "/" + total + ")");
				}
			});
		}

		@Override
		public void onFinished() {
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					copyLog.setText("拷贝日志(??)");
				}
			});
		}

		@Override
		public void onError(final int errorId) {
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					copyLog.setText("拷贝日志(error:" + errorId + ")");
				}
			});

		}
	};
	
	

}
