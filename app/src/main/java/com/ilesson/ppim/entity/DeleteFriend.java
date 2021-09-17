package com.ilesson.ppim.entity;

/**
 * @ClassName: DeleteFriend
 * @Description: java类作用描述
 * @Author: potato
 * @CreateDate: 2021/9/17 17:25
 * @UpdateUser: potato
 * @UpdateDate: 2021/9/17 17:25
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class DeleteFriend {
    private String targetId;

    public DeleteFriend(String targetId) {
        this.targetId = targetId;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }
}
