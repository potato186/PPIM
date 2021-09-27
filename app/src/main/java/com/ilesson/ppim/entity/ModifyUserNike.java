package com.ilesson.ppim.entity;

/**
 * @ClassName: ModifyUserNike
 * @Description: java类作用描述
 * @Author: potato
 * @CreateDate: 2021/9/24 9:54
 * @UpdateUser: potato
 * @UpdateDate: 2021/9/24 9:54
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class ModifyUserNike {
    private PPUserInfo ppUserInfo;

    public ModifyUserNike(PPUserInfo ppUserInfo) {
        this.ppUserInfo = ppUserInfo;
    }

    public PPUserInfo getPpUserInfo() {
        return ppUserInfo;
    }

    public void setPpUserInfo(PPUserInfo ppUserInfo) {
        this.ppUserInfo = ppUserInfo;
    }
}
