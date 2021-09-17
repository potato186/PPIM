package com.ilesson.ppim.entity;

/**
 * @ClassName: ModifyGroupNike
 * @Description: java类作用描述
 * @Author: potato
 * @CreateDate: 2021/9/17 14:07
 * @UpdateUser: potato
 * @UpdateDate: 2021/9/17 14:07
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class ModifyGroupNike {
    private String nikeName;

    public ModifyGroupNike(String nikeName) {
        this.nikeName = nikeName;
    }

    public String getNikeName() {
        return nikeName;
    }

    public void setNikeName(String nikeName) {
        this.nikeName = nikeName;
    }
}
