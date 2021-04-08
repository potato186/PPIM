package com.ilesson.ppim.entity;

import android.net.Uri;
import android.text.TextUtils;

import java.io.Serializable;


/**
 * Created by potato on 2020/3/10.
 */

public class PPUserInfo implements Serializable{
    private int id;
    private String phone;
    private boolean pay;
    private String token;
    private String name;
    private String icon;
    private int money;
    private String date;
    private String manager;
    private double similar;
    private Uri uri;
    public double getSimilar() {
        return similar;
    }

    public PPUserInfo() {
    }

    public PPUserInfo(String phone, String name, String icon) {
        this.phone = phone;
        this.name = name;
        this.icon = icon;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public void setSimilar(double similar) {
        this.similar = similar;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    private String prefix;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean isPay() {
        return pay;
    }

    public void setPay(boolean pay) {
        this.pay = pay;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
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

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        this.money = money;
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj){
            return true;//地址相等
        }

        if(obj == null){
            return false;//非空性：对于任意非空引用x，x.equals(null)应该返回false。
        }

        if(obj instanceof PPUserInfo){
            PPUserInfo other = (PPUserInfo) obj;
            //需要比较的字段相等，则这两个对象相等
            if(equalsStr(this.name, other.name)
                    && equalsStr(this.phone, other.phone)){
                return true;
            }
        }

        return false;
    }

    private boolean equalsStr(String str1, String str2){
        if(TextUtils.isEmpty(str1) && TextUtils.isEmpty(str2)){
            return true;
        }
        if(!TextUtils.isEmpty(str1) && str1.equals(str2)){
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + (name == null ? 0 : name.hashCode());
        result = 31 * result + (phone == null ? 0 : phone.hashCode());
        return result;
    }
}
