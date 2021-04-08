package com.ilesson.ppim.entity;

import java.util.List;

public class TargetCurrency {
    private String status;
    private String message;
    private List<Currency> data;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Currency> getData() {
        return data;
    }

    public void setData(List<Currency> data) {
        this.data = data;
    }
}
