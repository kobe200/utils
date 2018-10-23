package com.kobe.kobedemo.log;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * @author: kobe
 * @date: 2018/10/22 16:11
 * @decribe:
 */
public class CopyThread extends BaseThread {
    private final String tag = CopyThread.class.getSimpleName();
    /**
     * 是否取消当前工作
     */
    private boolean isCancel;
    private String[] logFilePath;
    private List<File> copyFiles;
    private String usbPath;
    /**
     * 拷贝进度
     */
    private int progress;

    public CopyThread(List<File> copyFiles,String[] logFilePath,String usbPath) {
        super();
        this.copyFiles = copyFiles;
        this.logFilePath = logFilePath;
        if(!usbPath.endsWith(File.separator)) {
            usbPath += File.separator;
        }
        this.usbPath = usbPath;
    }

    @Override
    public void setCancel(boolean isCancel) {
        this.isCancel = isCancel;
    }

    @Override
    public void run() {
        super.run();
        if(copyFiles == null || usbPath == null) {
            return;
        }
        LogManager.getInstance().noticeStar(copyFiles.size());
        // 获取U盘内存空间
        CopyFileUtil.UsbDeviceInfo info = CopyFileUtil.fileSize(usbPath);
        Log.i(tag,"==UsbDeviceInfo size==" + info.total + "|" + info.free);
        // 获取拷贝文件大小
        double fileTotalSize = 0;
        for(String s : logFilePath) {
            double size = FileSizeUtil.getFileOrFilesSize(s,FileSizeUtil.SIZETYPE_B);
            fileTotalSize += size;
            Log.i(tag,"==file size==" + s + " = " + size + "|" + fileTotalSize);
        }
        Log.d(tag,"file   fileTotalSize : " + fileTotalSize);
        // 比较拷贝文件大小与U盘剩余空间大小
        if(fileTotalSize > (double) info.free) {
            LogManager.getInstance().noticeError(LogManager.ErrorId.USB_SURPLUS_SPACE_NOT_ENOUGH);
            return;
        }
        // 检查目标目录是否存在，不存在则创建
        String usbLogRootPath = usbPath + LogManager.getInstance().getUsbLogName();
        Log.i(tag,"==usbLogRootPath==" + usbLogRootPath);
        File usbLogRootFile = new File(usbLogRootPath);
        if(!usbLogRootFile.exists()) {
            if(!usbLogRootFile.mkdirs()) {
                LogManager.getInstance().noticeError(LogManager.ErrorId.CREATE_DEST_FOLDER_FAIL);
                LogManager.getInstance().setCopying(false);
                return;
            }
        } else {
            checkTargetFolder(usbLogRootFile);
        }
        String usbLogLevel1Path = usbLogRootPath + File.separator + DateUtil.getSystemDate1();
        Log.i(tag,"==usbLogLevel1Path==" + usbLogLevel1Path);
        File usbLogLevel1File = new File(usbLogLevel1Path);
        if(!usbLogLevel1File.exists()) {
            if(!usbLogLevel1File.mkdirs()) {
                LogManager.getInstance().noticeError(LogManager.ErrorId.CREATE_DEST_FOLDER_FAIL);
                LogManager.getInstance().setCopying(false);
                return;
            }
        }
        for(File oldFile : copyFiles) {
            if(isCancel) {
                LogManager.getInstance().noticeError(LogManager.ErrorId.COPY_LOG_CANCEL);
                LogManager.getInstance().setCopying(false);
                return;
            }
            String newFileName = oldFile.getParentFile().getName() + "_" + oldFile.getName();
            String newFilePath = usbLogLevel1Path + File.separator + newFileName;
            Log.i(tag,"====newFilePath====" + newFilePath);
            File newFile = new File(newFilePath);
            if(!newFile.exists()) {
                try {
                    if(!newFile.createNewFile()) {
                        LogManager.getInstance().noticeError(LogManager.ErrorId.CREATE_DEST_FOLDER_FAIL);
                        LogManager.getInstance().setCopying(false);
                        return;
                    }
                } catch(IOException e) {
                    e.printStackTrace();
                }
            }
            if(copyFile(oldFile,newFilePath)) {
                if(isCancel) {
                    LogManager.getInstance().noticeError(LogManager.ErrorId.COPY_LOG_CANCEL);
                    LogManager.getInstance().setCopying(false);
                    return;
                }
                progress++;
                LogManager.getInstance().noticeProgress(progress,copyFiles.size());
            } else {
                Log.i(tag,"====copy fail====" + oldFile.getPath());
            }
        }
        if(progress == copyFiles.size()) {
            LogManager.getInstance().noticeFinished();
        }
        LogManager.getInstance().setCopying(false);
        Log.i(tag,"====CoypThread run end====");
    }

    /**
     * 检查目标文件夹内包含log文件夹状态，如超出最大保存文件夹次数，则删除最早保存的日志文件夹
     * @param usbLogLevel1File 目标文件夹
     */
    private void checkTargetFolder(File usbLogLevel1File) {
        File[] files = usbLogLevel1File.listFiles();
        Log.i(tag,"======checkTargetFolder======" + files.length + "|" + LogManager.getInstance().getLogFolderCount());
        if(files.length >= LogManager.getInstance().getLogFolderCount()) {
            File file = files[0];
            for(int i = 1; i < files.length; i++) {
                if(Double.parseDouble(file.getName()) > Double.parseDouble(files[i].getName())) {
                    file = files[i];
                }
            }
            Log.i(tag,"======delete======" + file.getPath());
            for(File f : file.listFiles()) {
                f.delete();
            }
            file.delete();
        }
    }

    /**
     * 复制单个文件
     * @param oldFile String 原文件路径 如：c:/fqf.txt
     * @param newPath String 复制后路径 如：f:/fqf.txt
     * @return boolean
     */
    private boolean copyFile(File oldFile,String newPath) {
        boolean copySuccess = true;
        InputStream inStream = null;
        FileOutputStream fs = null;
        try {
            int byteread = 0;
            // 读入原文件
            inStream = new FileInputStream(oldFile);
            fs = new FileOutputStream(newPath);
            byte[] buffer = new byte[1444];
            while((byteread = inStream.read(buffer)) != -1) {
                fs.write(buffer,0,byteread);
                if(isCancel) {
                    LogManager.getInstance().noticeError(LogManager.ErrorId.COPY_LOG_CANCEL);
                    break;
                }
            }
            inStream.close();
        } catch(Exception e) {
            copySuccess = false;
            e.printStackTrace();
        } finally {
            try {
                if(inStream != null) {
                    inStream.close();
                }
                if(fs != null) {
                    fs.close();
                }
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
        return copySuccess;
    }
}