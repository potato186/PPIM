package com.ilesson.ppim.entity;

/**
 * @ClassName: PublishNote
 * @Description: java类作用描述
 * @Author: potato
 * @CreateDate: 2021/9/24 17:17
 * @UpdateUser: potato
 * @UpdateDate: 2021/9/24 17:17
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class PublishNote {
    private String note;

    public PublishNote(String note) {
        this.note = note;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
