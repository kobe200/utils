package com.kobe.kobedemo.log;

import android.os.StatFs;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author kobe
 * @date 2018.10.19 9:11
 *
 * 保存系统日志策略
 * 1、将系统保存在当前日志、关机前日志、ANR日志保存到U盘根目录下；
 * 2、U盘保存log目录名称可通过setUsbLogName接口设置；
 * 3、U盘保存log目录下最多可保存最近5次拷贝日志，每次拷贝日志以当前时间命名文件夹；
 *
 */
public class LogManager {

	private final String tag = LogManager.class.getSimpleName();

	private static LogManager logManager;
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
	private int logFolderCount = 5;
	/**
	 * 拷贝日志线程
	 */
	private CoypThread coypThread;

	/** 上次的 **/
	private String androidLog = "/storage/emulated/legacy/cookoolog_last/logcat.log";
	private String kernelLog = "/storage/emulated/legacy/cookoolog_last/kernel.log";

	/** 当前的 **/
	private String emulated_logcat = "/storage/emulated/legacy/cookoolog/logcat.log";
	private String emulated_kernel = "/storage/emulated/legacy/cookoolog/kernel.log";

	/** ANR日志 **/
	private String anr = "data/anr/traces.txt";

	private String[] defaultFilePath = { androidLog, kernelLog, emulated_logcat, emulated_kernel, anr };

	private String defaultUsbPath = "/storage/udisk";

	public static LogManager getInstance() {
		if (logManager == null) {
			synchronized (LogManager.class) {
				if (logManager == null) {
					logManager = new LogManager();
				}
			}
		}
		return logManager;
	}

	public void registCopyListener(LogCopyListener logCopyListener) {
		this.logCopyListener = logCopyListener;
	}

	public void unRegistCopyListener(LogCopyListener logCopyListener) {
		this.logCopyListener = null;
	}

	public void setUsbLogName(String usbLogName) {
		this.usbLogName = usbLogName;
	}

	public interface LogCopyListener {
		/**
		 * 开始拷贝
		 */
		public void onStar(int total);

		/**
		 * 拷贝进度
		 *
		 * @param progress
		 */
		public void onProgress(int progress, int total);

		/**
		 * 拷贝完成
		 */
		public void onFinished();

		/**
		 * 拷贝异常
		 */
		public void onError(int errorId);
	}

	public interface ErrorId {
		/** 日志拷贝中 **/
		public static final int USB_LOG_COPYING = 200;
		/** U盘不存在 **/
		public static final int USB_NO_EXIST = 201;
		/** 拷贝文件路径错误 **/
		public static final int COPY_FILE_PATH_ERROR = 202;
		/** 创建USB文件失败 **/
		public static final int CREATE_DEST_FOLDER_FAIL = 203;
		/** 拷贝日志中断 **/
		public static final int COPY_LOG_CANCEL = 204;
		/** USB剩余空间不足 USB surplus space is not enough **/
		public static final int USB_SURPLUS_SPACE_NOT_ENOUGH = 205;
	}

	private void noticeError(int errorId) {
		if (logCopyListener != null) {
			logCopyListener.onError(errorId);
		}
		Log.i(tag, "==noticeError==" + errorId);
	}

	private void noticeStar(int total) {
		if (logCopyListener != null) {
			logCopyListener.onStar(total);
		}
		isCopying = true;
		Log.i(tag, "==noticeStar==" + total);
	}

	private void noticeProgress(int progress, int total) {
		if (logCopyListener != null) {
			logCopyListener.onProgress(progress, total);
		}
		Log.i(tag, "==noticeProgress==" + progress + "|" + total);
	}

	private void noticeFinished() {
		if (logCopyListener != null) {
			logCopyListener.onFinished();
		}
		Log.i(tag, "==noticeFinished==");
	}

	/**
	 * 取消拷贝日志
	 */
	public void cancelCopy() {
		if (isCopying && coypThread != null) {
			coypThread.setCancel(true);
			coypThread = null;
		}
	}

	/**
	 * 拷贝默认路径下日志
	 */
	public void copyLogToUsb() {
		copyLogToUsb(defaultFilePath, defaultUsbPath, true);
	}

	/**
	 * 拷贝系统日志到U盘
	 *
	 * @param logFilePath
	 *            系统日志路径集合
	 * @param usbPath
	 *            u盘根目录
	 * @param isOverlay
	 *            拉目标路径已存在改文件，是否覆盖
	 */
	public void copyLogToUsb(String[] logFilePath, String usbPath, boolean isOverlay) {
		Log.i(tag, "==copyLogToUsb==" + logFilePath + "|" + usbPath + "|" + isOverlay);
		if (isCopying) {
			noticeError(ErrorId.USB_LOG_COPYING);
			return;
		}
		// 检查拷贝状态
		if (TextUtils.isEmpty(usbPath)) {
			noticeError(ErrorId.USB_NO_EXIST);
			return;
		}
		File usbFile = new File(usbPath);
		if (!usbFile.exists()) {
			noticeError(ErrorId.USB_NO_EXIST);
			return;
		}
		List<File> copyFiles = new ArrayList<File>();
		for (String s : logFilePath) {
			if (TextUtils.isEmpty(s)) {
				continue;
			}
			File file = new File(s);
			if (file.exists()) {
				copyFiles.add(file);
			}
		}
		if (copyFiles.size() < 1) {
			noticeError(ErrorId.COPY_FILE_PATH_ERROR);
			return;
		}
		Log.i(tag, "==copy file size==" + copyFiles.size());
		startCopy(copyFiles, logFilePath, usbPath);
	}

	/**
	 * 开始拷贝
	 *
	 * @param copyFiles
	 * @param usbPath
	 */
	private void startCopy(List<File> copyFiles, String[] logFilePath, String usbPath) {
		if (coypThread != null) {
			coypThread.setCancel(true);
		}
		coypThread = new CoypThread(copyFiles, logFilePath, usbPath);
		coypThread.start();
	}

	private class CoypThread extends Thread {
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

		public CoypThread(List<File> copyFiles, String[] logFilePath, String usbPath) {
			super();
			this.copyFiles = copyFiles;
			this.logFilePath = logFilePath;
			if (!usbPath.endsWith(File.separator)) {
				usbPath += File.separator;
			}
			this.usbPath = usbPath;
		}

		public void setCancel(boolean isCancel) {
			this.isCancel = isCancel;
		}

		@Override
		public void run() {
			super.run();
			if (copyFiles == null || usbPath == null) {
				return;
			}
			noticeStar(copyFiles.size());
			// 获取U盘内存空间
			UsbDeviceInfo info = fileSize(usbPath);
			Log.i(tag, "==UsbDeviceInfo size==" + info.total + "|" + info.free);
			// 获取拷贝文件大小
			double fileTotalSize = 0;
			for (String s : logFilePath) {
				double size = FileSizeUtil.getFileOrFilesSize(s, FileSizeUtil.SIZETYPE_B);
				fileTotalSize += size;
				Log.i(tag, "==file size==" + s + " = " + size + "|" + fileTotalSize);
			}
			Log.d(tag, "file   fileTotalSize : " + fileTotalSize);
			// 比较拷贝文件大小与U盘剩余空间大小
			if (fileTotalSize > (double) info.free) {
				noticeError(ErrorId.USB_SURPLUS_SPACE_NOT_ENOUGH);
				return;
			}
			// 检查目标目录是否存在，不存在则创建
			String usbLogRootPath = usbPath + usbLogName;
			Log.i(tag, "==usbLogRootPath==" + usbLogRootPath);
			File usbLogRootFile = new File(usbLogRootPath);
			if (!usbLogRootFile.exists()) {
				if (!usbLogRootFile.mkdirs()) {
					noticeError(ErrorId.CREATE_DEST_FOLDER_FAIL);
					isCopying = false;
					return;
				}
			}else{
				checkTargetFolder(usbLogRootFile);
			}
			String usbLogLevel1Path = usbLogRootPath + File.separator + DateUtil.getSystemDate1();
			Log.i(tag, "==usbLogLevel1Path==" + usbLogLevel1Path);
			File usbLogLevel1File = new File(usbLogLevel1Path);
			if (!usbLogLevel1File.exists()) {
				if (!usbLogLevel1File.mkdirs()) {
					noticeError(ErrorId.CREATE_DEST_FOLDER_FAIL);
					isCopying = false;
					return;
				}
			}
			for (File oldFile : copyFiles) {
				if (isCancel) {
					noticeError(ErrorId.COPY_LOG_CANCEL);
					isCopying = false;
					return;
				}
				String newFileName = oldFile.getParentFile().getName() + "_" + oldFile.getName();
				String newFilePath = usbLogLevel1Path + File.separator + newFileName;
				Log.i(tag, "====newFilePath====" + newFilePath);
				File newFile = new File(newFilePath);
				if (!newFile.exists()) {
					try {
						if (!newFile.createNewFile()) {
							noticeError(ErrorId.CREATE_DEST_FOLDER_FAIL);
							isCopying = false;
							return;
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (copyFile(oldFile, newFilePath)) {
					if (isCancel) {
						noticeError(ErrorId.COPY_LOG_CANCEL);
						isCopying = false;
						return;
					}
					progress++;
					noticeProgress(progress, copyFiles.size());
				} else {
					Log.i(tag, "====copy fail====" + oldFile.getPath());
				}
			}
			if (progress == copyFiles.size()) {
				noticeFinished();
			}
			isCopying = false;
			Log.i(tag, "====CoypThread run end====");
		}

		/**
		 * 检查目标文件夹内包含log文件夹状态，如超出最大保存文件夹次数，则删除最早保存的日志文件夹
		 * @param usbLogLevel1File 目标文件夹
		 */
		private void checkTargetFolder(File usbLogLevel1File) {
			File[] files = usbLogLevel1File.listFiles();
			Log.i(tag, "======checkTargetFolder======" + files.length + "|" + logFolderCount);
			if(files.length >= logFolderCount ){
				File file = files[0];
				for(int i = 1 ; i < files.length ; i++){
					if(Double.parseDouble(file.getName()) > Double.parseDouble(files[i].getName())){
						file = files[i];
					}
				}
				Log.i(tag, "======delete======" + file.getPath());
				for(File f :file.listFiles()){
					f.delete();
				}
				file.delete();
			}
		}

		/**
		 * 复制单个文件
		 *
		 * @param oldFile
		 *            String 原文件路径 如：c:/fqf.txt
		 * @param newPath
		 *            String 复制后路径 如：f:/fqf.txt
		 * @return boolean
		 */
		public boolean copyFile(File oldFile, String newPath) {
			boolean copySucces = true;
			InputStream inStream = null;
			FileOutputStream fs = null;
			try {
				int byteread = 0;
				inStream = new FileInputStream(oldFile); // 读入原文件
				fs = new FileOutputStream(newPath);
				byte[] buffer = new byte[1444];
				while ((byteread = inStream.read(buffer)) != -1) {
					fs.write(buffer, 0, byteread);
					if (isCancel) {
						noticeError(ErrorId.COPY_LOG_CANCEL);
						break;
					}
				}
				inStream.close();
			} catch (Exception e) {
				copySucces = false;
				e.printStackTrace();
			} finally {
				try {
					if (inStream != null) {
						inStream.close();
					}
					if (fs != null) {
						fs.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return copySucces;
		}
	}

	public UsbDeviceInfo fileSize(String path) {
		UsbDeviceInfo info = null;
		try {
			StatFs statfs = new StatFs(path);
			// 获取SDCard上BLOCK总数
			long nTotalBlocks = statfs.getBlockCountLong();
			Log.i(tag, "===nTotalBlocks===" + nTotalBlocks);
			// 获取SDCard上每个block的SIZE
			long nBlocSize = statfs.getBlockSizeLong();
			Log.i(tag, "===nBlocSize===" + nBlocSize);
			// 获取可供程序使用的Block的数量
			long nAvailaBlock = statfs.getAvailableBlocksLong();
			Log.i(tag, "===nAvailaBlock===" + nAvailaBlock);
			// 获取剩下的所有Block的数量(包括预留的一般程序无法使用的块)
			long nFreeBlock = statfs.getFreeBlocksLong();
			Log.i(tag, "===nFreeBlock===" + nFreeBlock);
			info = new UsbDeviceInfo();
			// 计算SDCard 总容量大小MB
			info.total = nTotalBlocks * nBlocSize;
			Log.i(tag, "===info.total===" + info.total);
			// 计算 SDCard 剩余大小MB
			info.free = nAvailaBlock * nBlocSize;
			Log.i(tag, "===info.free===" + info.free);
		} catch (IllegalArgumentException e) {
			Log.e(tag, e.toString());
		}
		return info;
	}

	private class UsbDeviceInfo {
		public long total;
		public long free;
	}

}
