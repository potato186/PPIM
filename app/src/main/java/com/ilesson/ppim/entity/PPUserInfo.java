package com.ilesson.ppim.entity;

import android.net.Uri;
import android.text.TextUtils;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

import java.io.Serializable;


/**
 * Created by potato on 2020/3/10.
 */

@Table(name = "PPUserInfo")
public class PPUserInfo  extends SearchInfo implements Serializable{
    @Column(name = "id",isId = true)
    private int id;
    @Column(name = "phone")
    private String phone;
    @Column(name = "pay")
    private boolean pay;
    @Column(name = "joined")
    private boolean joined;
    @Column(name = "isFriend")
    private boolean isFriend;
    @Column(name = "token")
    private String token;
    @Column(name = "name")
    private String name;
    @Column(name = "nick")
    private String nick;
    @Column(name = "icon")
    private String icon;
    @Column(name = "money")
    private int money;
    @Column(name = "level")
    private int level;
    @Column(name = "date")
    private String date;
    @Column(name = "manager")
    private String manager;
    private String groupId;
    private double similar;
    private Uri uri;
    public double getSimilar() {
        return similar;
    }

    public boolean isFriend() {
        return isFriend;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setFriend(boolean friend) {
        isFriend = friend;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public PPUserInfo() {
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public boolean isJoined() {
        return joined;
    }

    public void setJoined(boolean joined) {
        this.joined = joined;
    }

    public String getManager() {
        return manager;
    }

    public void setManager(String manager) {
        this.manager = manager;
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
