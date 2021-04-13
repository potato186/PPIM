package com.ilesson.ppim.entity;

public class BitLoginInfo {
    private String phone;
    private String name;
    private String icon;
    private String token;
    private String rToken;
    private String bToken;
    private String realName;
    private String realNameSymbol;
    private String sex;
    private String birthday;

    private boolean pay;
    private String date;
    private long timeout;

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getRealNameSymbol() {
        return realNameSymbol;
    }

    public void setRealNameSymbol(String realNameSymbol) {
        this.realNameSymbol = realNameSymbol;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getrToken() {
        return rToken;
    }

    public void setrToken(String rToken) {
        this.rToken = rToken;
    }

    public String getbToken() {
        return bToken;
    }

    public void setbToken(String bToken) {
        this.bToken = bToken;
    }

    public boolean isPay() {
        return pay;
    }

    public void setPay(boolean pay) {
        this.pay = pay;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }
}
