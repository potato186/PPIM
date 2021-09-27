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
    private PPUserInfo userInfo;

    public DeleteFriend(PPUserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public PPUserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(PPUserInfo userInfo) {
        this.userInfo = userInfo;
    }
}
