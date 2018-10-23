package com.kobe.kobedemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.kobe.kobedemo.full_screen_view.MyWindowManager;
import com.kobe.kobedemo.full_screen_view.TouchActivity;
import com.kobe.kobedemo.gson.GsonActivity;
import com.kobe.kobedemo.log.LogActivity;

public class MainActivity extends Activity implements OnClickListener {
	private final String tag = MainActivity.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		findViewById(R.id.openWindow).setOnClickListener(this);
		findViewById(R.id.log).setOnClickListener(this);
		findViewById(R.id.gson).setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.openWindow:
			MyWindowManager.getInstance().showScreensaver();
			startActivity(new Intent(MainActivity.this, TouchActivity.class));
			break;
		case R.id.log:
			startActivity(new Intent(MainActivity.this, LogActivity.class));
			break;
		case R.id.gson:
			startActivity(new Intent(MainActivity.this, GsonActivity.class));
			break;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}


}
