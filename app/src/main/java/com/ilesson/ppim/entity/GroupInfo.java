package com.ilesson.ppim.entity;

import java.io.Serializable;

/**
 * Created by potato on 2020/4/23.
 */

public class GroupInfo implements Serializable {
    private String id;
    private String name;
    private String icon;
    private String tag;
    private int size;

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
}
