package com.kobe.kobedemo.log;

/**
 * @author: kobe
 * @date: 2018/10/22 16:53
 * @decribe:
 */
public abstract class BaseThread extends Thread{
    /**
     * 取消当前工作线程
     * @param isCancel
     */
    abstract void setCancel(boolean isCancel);

}
