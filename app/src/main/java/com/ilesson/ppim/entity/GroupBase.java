package com.ilesson.ppim.entity;

import java.util.List;

/**
 * Created by potato on 2020/4/24.
 */

public class GroupBase {

    private GroupInfo group;
    private List<PPUserInfo> members;
    private boolean isOwner;
    private int size;

    public GroupInfo getGroup() {
        return group;
    }

    public void setGroup(GroupInfo group) {
        this.group = group;
    }

    public List<PPUserInfo> getMembers() {
        return members;
    }

    public void setMembers(List<PPUserInfo> members) {
        this.members = members;
    }

    public boolean isOwner() {
        return isOwner;
    }

    public void setOwner(boolean owner) {
        isOwner = owner;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
