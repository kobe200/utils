package com.kobe.kobedemo.system;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: kobe
 * @date: 2018/10/22 13:44
 * @decribe:
 */
public class UsbDeviceManager {

    private static UsbDeviceManager usbDeviceManager;
    /**
     * 内置SD卡
     */
    public final String USB_INNER_SD_PATH = "storage/emulated/0";
    /**
     * 外置SD卡
     */
    public final String USB_OUTER_SD_PATH = "/storage/extsd";
    /**
     * 默认USB根目录android4.4以下版本
     */
    public final String USB_DEFAULT_PATH = "/mnt/media_rw/udisk";

    private final String tag = UsbDeviceManager.class.getSimpleName();
    /**
     * 存储mount信息的文件
     */
    private final String MOUNTS_FILE = "/proc/mounts";
    private List<String> devices = new ArrayList<>();
    private UsbBroadcast usbBroadcast = null;

    private UsbDeviceManager() {
    }

    public static UsbDeviceManager getInstance() {
        if(usbDeviceManager == null) {
            synchronized(UsbDeviceManager.class) {
                if(usbDeviceManager == null) {
                    usbDeviceManager = new UsbDeviceManager();
                }
            }
        }
        return usbDeviceManager;
    }

    /**
     * 检查UDisk是否挂载
     * @return 如果已挂载则返回挂载设备路径，否则返回null
     * /dev/fuse /storage/B4FE-5315 fuse rw,nosuid,nodev,noexec,noatime,user_id=1023,group_id=1023,default_permissions,allow_other 0 0
     */
    public List<String> getUsbDiskMountedVersion6() {
        List<String> blnRet = new ArrayList<>();
        String udiskPath1 = "/storage/";
        String udiskPath2 = "/storage/emulated";
        String pathEnd = " fuse";
        String strLine = null;
        BufferedReader reader = null;
        try {
            Log.i("UsbMount","=====isUsbDiskMountedVersion6()=====" + udiskPath1 + "|" + udiskPath2);
            reader = new BufferedReader(new FileReader(MOUNTS_FILE));
            while((strLine = reader.readLine()) != null) {
                Log.i("UsbMount","=====isUsbDiskMountedVersion6()=====" + strLine);
                if(strLine.contains(udiskPath1) && !strLine.contains(udiskPath2)) {
                    int start = strLine.indexOf(udiskPath1);
                    int end = strLine.indexOf(pathEnd);
                    Log.i("UsbMount","=====isUsbDiskMountedVersion6()==Mounted===" + start + "|" + end);
                    if(start < 0 || end < 0 || start > end) {
                        continue;
                    }
                    blnRet.add(strLine.substring(start,end));
                    Log.i("UsbMount","=====isUsbDiskMountedVersion6()==Mounted===" + blnRet);
                    break;
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            if(reader != null) {
                try {
                    reader.close();
                } catch(IOException e) {
                    e.printStackTrace();
                }
                reader = null;
            }
        }
        return blnRet;
    }

    public void init(Context context) {
        Log.d(tag,"===UsbDeviceManager init===" + context);
        registBroadcast(context);
        initUsbDiskMounted(context);
    }

    public void exit(Context context) {
        unRegisterBroadcast(context);
    }

    /**
     * 获取已挂载保存日志Usb根节点
     * @return 默认返回第一个挂载的USB设备节点，如无设备则返回null
     */
    public String getLogSaveUsb() {
        String usbPath = null;
        Log.d(tag,"===getLogSaveUsb devices.size()===" + devices.size());
        if(devices.size() == 0) {
            return usbPath;
        } else {
            for(int i = 0 ; i < devices.size() ; i++){
                if(devices.get(i).contains(USB_INNER_SD_PATH)||devices.get(i).contains(USB_OUTER_SD_PATH)){
                    continue;
                }
                usbPath = devices.get(i);
                break;
            }
        }
        Log.d(tag,"===getLogSaveUsb===" + usbPath);
        return usbPath;
    }

    private void registBroadcast(Context context) {
        usbBroadcast = new UsbBroadcast();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_EJECT);
        filter.addDataScheme("file");
        context.registerReceiver(usbBroadcast,filter);
    }

    private void unRegisterBroadcast(Context context) {
        if(usbBroadcast != null) {
            context.unregisterReceiver(usbBroadcast);
        }
    }

    private void setMountDevice(int index,String device) {
        if(!devices.contains(device) && devices.size() > index) {
            devices.add(index,device);
        }
    }

    private void setMountDevice(String device) {
        if(!devices.contains(device)) {
            devices.add(device);
        }
    }

    private void setUnMountDevice(String device) {
        if(devices.contains(device)) {
            devices.remove(device);
        }
    }

    /**
     * 检查UDisk是否挂载。
     * @return true:UDisk已经挂载, false:UDisk未挂载
     */
    public void initUsbDiskMounted(Context context) {
        //默认三路径
        String strLine = null;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(MOUNTS_FILE));
            while((strLine = reader.readLine()) != null) {
                Log.i(tag,"--- reader.readLine()---" + strLine);
                if(strLine.contains(USB_INNER_SD_PATH)) {
                    setMountDevice(0,USB_INNER_SD_PATH);
                } else if(strLine.contains(USB_OUTER_SD_PATH)) {
                    setMountDevice(0,USB_OUTER_SD_PATH);
                } else if(strLine.contains(USB_DEFAULT_PATH)) {
                    setMountDevice(USB_DEFAULT_PATH);
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            if(reader != null) {
                try {
                    reader.close();
                } catch(IOException e) {
                    e.printStackTrace();
                }
                reader = null;
            }
        }

        int currentVersion = android.os.Build.VERSION.SDK_INT;
        Log.d(tag,"--- UsbDiskUtil_initUsbDiskMounted ---currentVersion---" + currentVersion);
        //6.0系统
        if(currentVersion >= Build.VERSION_CODES.M) {
            //上电启动检测到有设备
            List<String> otherPath = getUsbDiskMountedVersion6();
            for(int i = 0; i < otherPath.size(); i++) {
                Log.d(tag,"--- UsbDiskUtil_initUsbDiskMounted_Path ---" + otherPath.toString());
                setMountDevice(otherPath.get(i));
            }
        }
    }

    public class UsbBroadcast extends BroadcastReceiver {

        @Override
        public void onReceive(Context context,Intent intent) {
            String action = intent.getAction();
            String dataString = intent.getDataString();
            Log.i(tag,"--- UsbDiskReceiver_action ---" + action);
            Log.i(tag,"===dataString====" + dataString);
            // file:///storage/00B1-6906
            if(Intent.ACTION_MEDIA_MOUNTED.equals(action)) {
                if(dataString != null) {
                    dataString = dataString.replace("file://","").trim();
                    setMountDevice(dataString);
                }
            } else if(Intent.ACTION_MEDIA_EJECT.equals(action)) {
                if(dataString != null) {
                    dataString = dataString.replace("file://","").trim();
                    setUnMountDevice(dataString);
                }
            }
        }
    }


}
