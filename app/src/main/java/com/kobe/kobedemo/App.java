package com.kobe.kobedemo;

import android.app.Application;

import com.kobe.kobedemo.full_screen_view.MyWindowManager;
import com.kobe.kobedemo.log.LogManager;
import com.kobe.kobedemo.system.UsbDeviceManager;

public class App extends Application {
	
	
	@Override
	public void onCreate() {
		super.onCreate();
		UsbDeviceManager.getInstance().init(getApplicationContext());
		MyWindowManager.getInstance().init(getApplicationContext());
		LogManager.getInstance().init(getApplicationContext());
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		UsbDeviceManager.getInstance().exit(getApplicationContext());
	}
}
                                                  