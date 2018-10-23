package com.kobe.kobedemo.gson;

/**
 * @author: kobe
 * @date: 2018/10/19 16:27
 * @decribe:
 */
public class JsonRootBean {

    private int rst;
    private String msg;
    private Data data;

    public int getRst() {
        return rst;
    }

    public void setRst(int rst) {
        this.rst = rst;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public class Data {

        private String cookie;

        public String getCookie() {
            return cookie;
        }

        public void setCookie(String cookie) {
            this.cookie = cookie;
        }

    }
}
