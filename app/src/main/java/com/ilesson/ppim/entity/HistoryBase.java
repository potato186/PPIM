package com.ilesson.ppim.entity;

import java.util.List;

/**
 * Created by potato on 2019/5/7.
 */

public class HistoryBase {
    private int code;
    private String message;
    private List<History> data;

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

    public List<History> getData() {
        return data;
    }

    public void setData(List<History> data) {
        this.data = data;
    }
}
