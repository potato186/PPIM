package com.ilesson.ppim.entity;

import java.io.File;
import java.io.Serializable;

public class NoteBase implements Serializable {
    private int type;
    private String tag;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
