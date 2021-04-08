package com.ilesson.ppim.entity;

import java.io.Serializable;

/**
 * Created by potato on 2020/3/10.
 */

public class RongUserInfo {
    private String id;
    private String name;
    private String icon;
    private String extra;

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

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }
}
