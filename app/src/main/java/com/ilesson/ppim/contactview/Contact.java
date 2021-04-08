package com.ilesson.ppim.contactview;

import com.ilesson.ppim.entity.PPUserInfo;

import java.io.Serializable;

public class Contact implements Serializable {
    private int type;
    private PPUserInfo userInfo;

    public PPUserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(PPUserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public Contact(PPUserInfo userInfo,int type) {
        this.type = type;
        this.userInfo = userInfo;
    }


    public int getType() {
        return type;
    }

}
