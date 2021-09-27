package com.ilesson.ppim.entity;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

/**
 * @ClassName: ConversationInfo
 * @Description: java类作用描述
 * @Author: potato
 * @CreateDate: 2021/9/26 17:11
 * @UpdateUser: potato
 * @UpdateDate: 2021/9/26 17:11
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
@Table(name = "PPUserInfo")
public class ConversationInfo extends SearchInfo{
    @Column(name = "targetId",isId = true)
    public String targetId;
    @Column(name = "conversationTitle")
    public String conversationTitle;
    @Column(name = "portraitUrl")
    public String portraitUrl;
    @Column(name = "type")
    public int type;
    @Column(name = "date")
    public long date;

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public String getConversationTitle() {
        return conversationTitle;
    }

    public void setConversationTitle(String conversationTitle) {
        this.conversationTitle = conversationTitle;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getPortraitUrl() {
        return portraitUrl;
    }

    public void setPortraitUrl(String portraitUrl) {
        this.portraitUrl = portraitUrl;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
