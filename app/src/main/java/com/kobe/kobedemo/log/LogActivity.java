package com.kobe.kobedemo.log;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.kobe.kobedemo.R;

public class LogActivity extends Activity implements OnClickListener {
    private final String tag = LogActivity.class.getSimpleName();

    private Button copyLog, cancelLog, copyLog1, deleteUsbLog;
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
        public void onProgress(final int progress,final int total) {
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
                    copyLog.setText("拷贝日志(完成)");
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);
        copyLog = findViewById(R.id.copylog);
        copyLog.setOnClickListener(this);
        cancelLog = findViewById(R.id.cancel_copylog);
        cancelLog.setOnClickListener(this);
        copyLog1 = findViewById(R.id.copylog1);
        copyLog1.setOnClickListener(this);
        deleteUsbLog = findViewById(R.id.delete_copylog);
        deleteUsbLog.setOnClickListener(this);
        findViewById(R.id.create_anr_file).setOnClickListener(this);
        LogManager.getInstance().registCopyListener(logCopyListener);
        LogManager.getInstance().setUsbLogName("KobeLog");
    }

    @Override
    public void onClick(View view) {
        if(!checkPermission()) {
            return;
        }
        switch(view.getId()) {
            case R.id.copylog:
                LogManager.getInstance().copyLogToUsb();
                break;
            case R.id.cancel_copylog:
                LogManager.getInstance().cancelCopy();
                break;
            case R.id.copylog1:
                LogManager.getInstance().copyLogToUsb(LogManager.CopyMode.RUNTIME_EXEC);
                break;
            case R.id.delete_copylog:
                LogManager.getInstance().deleteUsbCopy();
                break;
            case R.id.create_anr_file:
                createAnrText();
                break;

        }
    }

    private void createAnrText() {
        try {
            Thread.sleep(15000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogManager.getInstance().unRegistCopyListener(logCopyListener);
    }

    private boolean checkPermission() {
        boolean isHavePermission = true;
        //检查权限（NEED_PERMISSION）是否被授权 PackageManager.PERMISSION_GRANTED表示同意授权
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //用户已经拒绝过一次，再次弹出权限申请对话框需要给用户一个解释
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(this,"请开通相关权限，否则无法正常使用本应用！",Toast.LENGTH_SHORT).show();
            }
            //申请权限
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
            isHavePermission = false;
        } else {
            Toast.makeText(this,"授权成功！",Toast.LENGTH_SHORT).show();
            Log.e(tag,"checkPermission: 已经授权！");
        }
        return isHavePermission;
    }

}
