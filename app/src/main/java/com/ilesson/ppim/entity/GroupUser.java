package com.ilesson.ppim.entity;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

/**
 * @ClassName: GroupUser
 * @Description: java类作用描述
 * @Author: potato
 * @CreateDate: 2021/9/26 16:43
 * @UpdateUser: potato
 * @UpdateDate: 2021/9/26 16:43
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
@Table(name = "GroupUser")
public class GroupUser extends SearchInfo{
    @Column(name = "groupId",isId = true)
    private String groupId;
    @Column(name = "userId")
    private String userId;
    @Column(name = "userName")
    private String userName;
    @Column(name = "userIcon")
    private String userIcon;
    @Column(name = "groupName")
    private String groupName;
    @Column(name = "groupTagName")
    private String groupTagName;

    public String getGroupTagName() {
        return groupTagName;
    }

    public void setGroupTagName(String groupTagName) {
        this.groupTagName = groupTagName;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserIcon() {
        return userIcon;
    }

    public void setUserIcon(String userIcon) {
        this.userIcon = userIcon;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}
