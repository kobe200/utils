package com.kobe.kobedemo.full_screen_view;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;

public class TouchActivity extends Activity implements OnClickListener {
	private RelativeLayout layout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		layout = new RelativeLayout(this);
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT,
				RelativeLayout.LayoutParams.MATCH_PARENT);
		layout.setLayoutParams(layoutParams);
		layout.setOnClickListener(this);
		setContentView(layout);

	}

	@Override
	public void onClick(View view) {
		if(view == layout){
			this.finish();
		}
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		MyWindowManager.getInstance().cancelScreensaver();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		MyWindowManager.getInstance().cancelScreensaver();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		MyWindowManager.getInstance().cancelScreensaver();
	}

}
