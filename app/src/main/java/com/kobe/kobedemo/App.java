package com.kobe.kobedemo;

import android.app.Application;

import com.kobe.kobedemo.full_screen_view.MyWindowManager;

public class App extends Application {
	
	
	@Override
	public void onCreate() {
		super.onCreate();
		MyWindowManager.getInstance().init(getApplicationContext());
	}

}
                                                  