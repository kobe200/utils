package com.kobe.kobedemo.log;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author kobe
 **/
@SuppressLint("SimpleDateFormat")
public class DateUtil {

    private static SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMddHHmmss");

    private static SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static SimpleDateFormat sdf3 = new SimpleDateFormat("yyyyMMdd");

    public static String getSystemDate() {
        SimpleDateFormat sf = new SimpleDateFormat("yyyy:MM:dd");
        return sf.format(new Date(System.currentTimeMillis())).toString();
    }

    public static String getSystemDate1() {
        return sdf1.format(new Date(System.currentTimeMillis())).toString();
    }

    public static String getSystemDate2() {
        return sdf2.format(new Date(System.currentTimeMillis())).toString();
    }

    public static String getSystemDate3() {
        return sdf3.format(new Date(System.currentTimeMillis()));
    }

    public static long getTodaySystemDate() {
        return System.currentTimeMillis();
    }

    public static boolean isMoreThanOneDay(long recordTime,DayTime overTime) {
        if((getTodaySystemDate() - recordTime) > overTime.getDayTime()) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isMoreThanOneDay(long recordTime,long nowTime) {
        String time1 = sdf3.format(new Date(recordTime));
        String time2 = sdf3.format(new Date(nowTime));

        return time1.compareTo(time2) != 0;
    }

    public static boolean isSameDay(long time1,long time2) {
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
        String date1 = sf.format(time1);
        String date2 = sf.format(time2);

        if(date1.equals(date2)) {
            return true;
        }
        return false;
    }

    /**
     *
     */
    public enum DayTime {
        HALFDAY(12 * 60 * 60 * 1000L),
        ONEDAY(24 * 60 * 60 * 1000L),
        TWODAY(2 * 24 * 60 * 60 * 1000L);
        public long dayTime;

        DayTime(long dayTime) {
            this.dayTime = dayTime;
        }

        public long getDayTime() {
            return this.dayTime;
        }
    }

}
