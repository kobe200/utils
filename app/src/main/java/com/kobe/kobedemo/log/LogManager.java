package com.kobe.kobedemo.log;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.kobe.kobedemo.system.UsbDeviceManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author kobe
 * @date 2018.10.19 9:11
 * <p>
 * 保存系统日志策略
 * 1、将系统保存在当前日志、关机前日志、ANR日志保存到U盘根目录下；
 * 2、U盘保存log目录名称可通过setUsbLogName接口设置；
 * 3、U盘保存log目录下最多可保存最近5次拷贝日志，每次拷贝日志以当前时间命名文件夹；
 */
public class LogManager {

    private static LogManager logManager;
    private final String tag = LogManager.class.getSimpleName();
    /**
     * 拷贝状态监听
     */
    private LogCopyListener logCopyListener;
    /**
     * 是否拷贝日志中
     */
    private boolean isCopying;
    /**
     * 保存到U盘中在目录名称
     */
    private String usbLogName = "systemLog";
    /**
     * U盘中最多保存最近拷贝文件夹次数
     */
    private int logFolderCount = 3;
    /**
     * 拷贝日志线程
     */
    private BaseThread copyThread;

    /**
     * 上次的
     **/
    private String androidLog = "/storage/emulated/legacy/cookoolog_last/logcat.log";
    private String kernelLog = "/storage/emulated/legacy/cookoolog_last/kernel.log";

    /**
     * 当前的
     **/
    private String emulated_logcat = "/storage/emulated/legacy/cookoolog/logcat.log";
    private String emulated_kernel = "/storage/emulated/legacy/cookoolog/kernel.log";

    /**
     * ANR日志
     **/
    private String anr = "data/anr/traces.txt";

    private String[] defaultFilePath = {androidLog,kernelLog,emulated_logcat,emulated_kernel,anr};


    public static LogManager getInstance() {
        if(logManager == null) {
            synchronized(LogManager.class) {
                if(logManager == null) {
                    logManager = new LogManager();
                }
            }
        }
        return logManager;
    }

    public void init(Context context){
        Log.i(tag,"====LogManager init====" + Build.VERSION.SDK_INT);
        //如果是6.0以上系统日志保存路径
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            defaultFilePath[0] = "/storage/emulated/0/cookoolog_last/logcat.log";
            defaultFilePath[1] = "/storage/emulated/0/cookoolog_last/kernel.log";
            defaultFilePath[2] = "/storage/emulated/0/cookoolog/logcat.log";
            defaultFilePath[3] = "/storage/emulated/0/cookoolog/kernel.log";
            defaultFilePath[4] = "data/anr/traces.txt";
        }
    }

    public void registCopyListener(LogCopyListener logCopyListener) {
        this.logCopyListener = logCopyListener;
    }

    public void unRegistCopyListener(LogCopyListener logCopyListener) {
        this.logCopyListener = null;
    }

    public void deleteUsbCopy() {
        String usbLogRootPath = UsbDeviceManager.getInstance().getLogSaveUsb() + File.separator + usbLogName;
        Log.i(tag,"=====deleteUsbCop=====" + usbLogRootPath);
        File file = new File(usbLogRootPath);
        if(file.exists()) {
            try {
                Runtime.getRuntime().exec("rm -r " + usbLogRootPath);
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void noticeError(int errorId) {
        if(logCopyListener != null) {
            logCopyListener.onError(errorId);
        }
        Log.i(tag,"==noticeError==" + errorId);
    }

    public void noticeStar(int total) {
        if(logCopyListener != null) {
            logCopyListener.onStar(total);
        }
        isCopying = true;
        Log.i(tag,"==noticeStar==" + total);
    }

    public void noticeProgress(int progress,int total) {
        if(logCopyListener != null) {
            logCopyListener.onProgress(progress,total);
        }
        Log.i(tag,"==noticeProgress==" + progress + "|" + total);
    }

    public void noticeFinished() {
        if(logCopyListener != null) {
            logCopyListener.onFinished();
        }
        Log.i(tag,"==noticeFinished==");
    }

    /**
     * 取消拷贝日志
     */
    public void cancelCopy() {
        if(isCopying && copyThread != null) {
            copyThread.setCancel(true);
            copyThread = null;
        }
    }

    /**
     * 拷贝默认路径下日志
     */
    public void copyLogToUsb() {
        copyLogToUsb(defaultFilePath,UsbDeviceManager.getInstance().getLogSaveUsb(),true,CopyMode.FILE_IO);
    }

    /**
     * 拷贝日志
     * @param copyMode 拷贝日志模式
     */
    public void copyLogToUsb(int copyMode) {
        copyLogToUsb(defaultFilePath,UsbDeviceManager.getInstance().getLogSaveUsb(),true,copyMode);
    }

    /**
     * 拷贝系统日志到U盘
     * @param logFilePath 系统日志路径集合
     * @param usbPath u盘根目录
     * @param isOverlay 拉目标路径已存在改文件，是否覆盖
     */
    public void copyLogToUsb(String[] logFilePath,String usbPath,boolean isOverlay,int copyMode) {
        Log.i(tag,"==copyLogToUsb==" + logFilePath + "|" + usbPath + "|" + isOverlay + "|" + copyMode);
        if(isCopying) {
            noticeError(ErrorId.USB_LOG_COPYING);
            return;
        }
        // 检查拷贝状态
        if(TextUtils.isEmpty(usbPath)) {
            noticeError(ErrorId.USB_NO_EXIST);
            return;
        }
        File usbFile = new File(usbPath);
        if(!usbFile.exists()) {
            noticeError(ErrorId.USB_NO_EXIST);
            return;
        }
        List<File> copyFiles = new ArrayList<File>();
        for(String s : logFilePath) {
            if(TextUtils.isEmpty(s)) {
                continue;
            }
            File file = new File(s);
            if(file.exists()) {
                copyFiles.add(file);
            }
        }
        if(copyFiles.size() < 1) {
            noticeError(ErrorId.COPY_FILE_PATH_ERROR);
            return;
        }
        Log.i(tag,"==copy file size==" + copyFiles.size());
        if(CopyMode.FILE_IO == copyMode) {
            startCopy(copyFiles,logFilePath,usbPath);
        } else if(CopyMode.RUNTIME_EXEC == copyMode) {
            startCopy2(copyFiles,logFilePath,usbPath);
        }
    }

    /**
     * 开始拷贝
     * @param copyFiles
     * @param usbPath
     */
    private void startCopy(List<File> copyFiles,String[] logFilePath,String usbPath) {
        if(copyThread != null) {
            copyThread.setCancel(true);
            copyThread = null;
        }
        copyThread = new CopyThread(copyFiles,logFilePath,usbPath);
        copyThread.start();
    }

    /**
     * 开始拷贝
     * @param copyFiles
     * @param usbPath
     */
    private void startCopy2(List<File> copyFiles,String[] logFilePath,String usbPath) {
        if(copyThread != null) {
            copyThread.setCancel(true);
            copyThread = null;
        }
        copyThread = new CopyThread2(copyFiles,logFilePath,usbPath);
        copyThread.start();
    }

    public boolean isCopying() {
        return isCopying;
    }

    public void setCopying(boolean copying) {
        isCopying = copying;
    }

    public String getUsbLogName() {
        return usbLogName;
    }

    public void setUsbLogName(String usbLogName) {
        this.usbLogName = usbLogName;
    }

    public int getLogFolderCount() {
        return logFolderCount;
    }

    public void setLogFolderCount(int logFolderCount) {
        this.logFolderCount = logFolderCount;
    }

    public interface LogCopyListener {
        /**
         * 开始拷贝
         */
        void onStar(int total);

        /**
         * 拷贝进度
         * @param progress
         */
        void onProgress(int progress,int total);

        /**
         * 拷贝完成
         */
        void onFinished();

        /**
         * 拷贝异常
         */
        void onError(int errorId);
    }

    public interface ErrorId {
        /**
         * 日志拷贝中
         **/
        int USB_LOG_COPYING = 200;
        /**
         * U盘不存在
         **/
        int USB_NO_EXIST = 201;
        /**
         * 拷贝文件路径错误
         **/
        int COPY_FILE_PATH_ERROR = 202;
        /**
         * 创建USB文件失败
         **/
        int CREATE_DEST_FOLDER_FAIL = 203;
        /**
         * 拷贝日志中断
         **/
        int COPY_LOG_CANCEL = 204;
        /**
         * USB剩余空间不足 USB surplus space is not enough
         **/
        int USB_SURPLUS_SPACE_NOT_ENOUGH = 205;
    }

    public interface CopyMode {
        int FILE_IO = 1;
        int RUNTIME_EXEC = 2;
    }
}
