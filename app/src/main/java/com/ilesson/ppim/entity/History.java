package com.ilesson.ppim.entity;

import java.io.Serializable;

/**
 * Created by potato on 2019/5/7.
 */

public class History implements Serializable{
    private int code;
    private String uid;
    private String title;
    private String uuid;
    private String src;
    private int grade;
    private int score;
    private int number;
    private String date;
    public void setCode(int code) {
        this.code = code;
    }
    public int getCode() {
        return code;
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

    public void setScore(int score) {
        this.score = score;
    }
    public int getScore() {
        return score;
    }

    public void setNumber(int number) {
        this.number = number;
    }
    public int getNumber() {
        return number;
    }

    public void setDate(String date) {
        this.date = date;
    }
    public String getDate() {
        return date;
    }
}
