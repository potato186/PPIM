package com.ilesson.ppim.entity;

/**
 * Created by potato on 2019/4/9.
 */

public class ComposeResultBase {
    private int code;
    private String message;
    private ComposeResultData data;
    public void setCode(int code) {
        this.code = code;
    }
    public int getCode() {
        return code;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    public String getMessage() {
        return message;
    }

    public void setData(ComposeResultData data) {
        this.data = data;
    }
    public ComposeResultData getData() {
        return data;
    }
}
