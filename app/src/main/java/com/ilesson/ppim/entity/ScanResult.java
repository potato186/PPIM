package com.ilesson.ppim.entity;

/**
 * Created by potato on 2019/5/9.
 */

public class ScanResult {
    private int code;
    private String message;
    private String data;
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
