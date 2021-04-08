package com.ilesson.ppim.entity;

import java.io.File;
import java.util.List;

public class NoteContent extends NoteBase{
    private List<NoteInfo> data;
    public List<NoteInfo> getData() {
        return data;
    }

    public void setData(List<NoteInfo> data) {
        this.data = data;
    }
}
