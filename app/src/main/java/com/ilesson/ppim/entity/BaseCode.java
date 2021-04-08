package com.ilesson.ppim.entity;

/**
 * Created by potato on 2020/3/10.
 */

public class BaseCode<T> {

    private String status;
    private int code;
    private int tag;
    private String message;
    private String bak;
    private AddressInfo extra;
    private T data;

    public AddressInfo getExtra() {
        return extra;
    }

    public void setExtra(AddressInfo extra) {
        this.extra = extra;
    }

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

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public int getTag() {
        return tag;
    }

    public void setTag(int tag) {
        this.tag = tag;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBak() {
        return bak;
    }

    public void setBak(String bak) {
        this.bak = bak;
    }
}
