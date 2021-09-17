package com.ilesson.ppim.entity;

/**
 * @ClassName: ModifyGroupName
 * @Description: java类作用描述
 * @Author: potato
 * @CreateDate: 2021/9/17 16:35
 * @UpdateUser: potato
 * @UpdateDate: 2021/9/17 16:35
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class ModifyGroupName {
    private String groupName;

    public ModifyGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}
