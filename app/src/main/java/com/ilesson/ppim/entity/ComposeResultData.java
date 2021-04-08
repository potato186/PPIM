package com.ilesson.ppim.entity;

/**
 * Created by potato on 2019/4/9.
 */

public class ComposeResultData {

    private int code;
    private String msg;
    private String errmsg;
    private String desctext;
    private String fmtext;
    private String pgtext;
    private String xgtext;
    private int pgstate;
    private String uid;
    private String title;
    private int score;
    private String uuid;
    private String src;
    private int grade;

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getXgtext() {
        return xgtext;
    }

    public void setXgtext(String xgtext) {
        this.xgtext = xgtext;
    }

    public int getPgstate() {
        return pgstate;
    }

    public void setPgstate(int pgstate) {
        this.pgstate = pgstate;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    public void setErrmsg(String errmsg) {
        this.errmsg = errmsg;
    }

    public String getErrmsg() {
        return errmsg;
    }

    public void setDesctext(String desctext) {
        this.desctext = desctext;
    }

    public String getDesctext() {
        return desctext;
    }

    public void setFmtext(String fmtext) {
        this.fmtext = fmtext;
    }

    public String getFmtext() {
        return fmtext;
    }

    public void setPgtext(String pgtext) {
        this.pgtext = pgtext;
    }

    public String getPgtext() {
        return pgtext;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUid() {
        return uid;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public String getSrc() {
        return src;
    }

    public void setGrade(int grade) {
        this.grade = grade;
    }

    public int getGrade() {
        return grade;
    }

}
