package com.kobe.kobedemo.full_screen_view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import com.kobe.kobedemo.full_screen_view.WindowView.OnViewTouch;

public class MyWindowManager implements OnViewTouch {
	private final String TAG = MyWindowManager.class.getSimpleName();
	private static MyWindowManager naviManager = new MyWindowManager();
	private WindowView windowView = null;
	private int width,heigth;
	private static LayoutParams dialogWindowParams;
	/**系统窗口管理工具**/
	private static WindowManager mWindowManager;

	private MyWindowManager() {
	}

	public static MyWindowManager getInstance() {
		return naviManager;
	}

	/**初始�?**/
	public void init(Context context) {
		windowView = new WindowView(context);
		windowView.setOnViewTouch(this);
		mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics dm = context.getResources().getDisplayMetrics();
		heigth = dm.heightPixels;
		width = dm.widthPixels;
		Log.i(TAG, TAG + "=== init === " + context.getPackageName() + "|" + this + "|" + heigth + "|" + width);
	}

	/**打开导航提示:1**/
	private final int HANDLE_OPEN_SCREEN_SAVER = 0x1;
	/**取消导航提示:2**/
	private final int HANDLE_CANCEL_SCREEN_SAVER = 0x2;

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			Log.i(TAG, TAG + "===" + msg.what + "|" + msg.obj);
			switch (msg.what) {
			case HANDLE_OPEN_SCREEN_SAVER:
				handleShowScreensaver();
				break;
			case HANDLE_CANCEL_SCREEN_SAVER:
				handleCancelScreensaver();
				break;
			}
		}
	};

	/**显示window**/
	public void showScreensaver() {
		mHandler.removeMessages(HANDLE_OPEN_SCREEN_SAVER);
		mHandler.obtainMessage(HANDLE_OPEN_SCREEN_SAVER, 0).sendToTarget();
	}

	/**隐藏window**/
	public void cancelScreensaver() {
		mHandler.sendEmptyMessage(HANDLE_CANCEL_SCREEN_SAVER);
	}

	private void handleShowScreensaver() {
		Log.i(TAG, TAG + "=== show ======" + dialogWindowParams);
		if (dialogWindowParams == null) {
			dialogWindowParams = new LayoutParams();
			dialogWindowParams.type = LayoutParams.TYPE_SYSTEM_OVERLAY;
			dialogWindowParams.format = PixelFormat.RGBA_8888;
			dialogWindowParams.flags = LayoutParams.FLAG_LAYOUT_IN_SCREEN;
			dialogWindowParams.gravity = Gravity.CENTER;
			dialogWindowParams.width = width;
			dialogWindowParams.height = heigth;
			dialogWindowParams.x = 0;
			dialogWindowParams.y = 0;
			mWindowManager.addView(windowView, dialogWindowParams);
		}
	}

	private void handleCancelScreensaver() {
		Log.i(TAG, TAG + "=== dismiss " + dialogWindowParams);
		mHandler.removeMessages(HANDLE_CANCEL_SCREEN_SAVER);
		if (dialogWindowParams == null) {
			return;
		}
		mWindowManager.removeView(windowView);
		dialogWindowParams = null;
	}

	@Override
	public void onTouch() {
		cancelScreensaver();
	}
	
	


}
