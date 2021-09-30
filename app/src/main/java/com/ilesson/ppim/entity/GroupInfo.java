package com.ilesson.ppim.entity;

import com.lidroid.xutils.db.annotation.Finder;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

import java.io.Serializable;
import java.util.List;

/**
 * Created by potato on 2020/4/23.
 */

@Table(name = "GroupInfo")
public class GroupInfo extends SearchInfo implements Serializable {
    @Column(name = "id",isId = true)
    private String id;
    @Column(name = "name")
    private String name;
    @Column(name = "icon")
    private String icon;
    @Column(name = "tag")
    private String tag;
    private String broadcast;
    private String userName;
    @Column(name = "size")
    private int size;
    @Finder(valueColumn = "userId", targetColumn = "groupId")
    private List<PPUserInfo> ppUserInfos;
    public String getBroadcast() {
        return broadcast;
    }

    public void setBroadcast(String broadcast) {
        this.broadcast = broadcast;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public List<PPUserInfo> getPpUserInfos() {
        return ppUserInfos;
    }

    public void setPpUserInfos(List<PPUserInfo> ppUserInfos) {
        this.ppUserInfos = ppUserInfos;
    }
}
