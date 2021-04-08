package com.ilesson.ppim.entity;

import java.io.Serializable;

public class InvoiceInfo implements Serializable {
    private String date;
    private String number;
    private String mediumName;
    private String iid;
    private String typeName;
    private String name;
    private String medium;
    private String type;
    private String email;

    public InvoiceInfo() {
    }

    public InvoiceInfo(String number, String mediumName, String typeName, String name, String medium, String type, String email) {
        this.number = number;
        this.mediumName = mediumName;
        this.typeName = typeName;
        this.name = name;
        this.medium = medium;
        this.type = type;
        this.email = email;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getMediumName() {
        return mediumName;
    }

    public void setMediumName(String mediumName) {
        this.mediumName = mediumName;
    }

    public String getIid() {
        return iid;
    }

    public void setIid(String iid) {
        this.iid = iid;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMedium() {
        return medium;
    }

    public void setMedium(String medium) {
        this.medium = medium;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "InvoiceInfo{" +
                "date='" + date + '\'' +
                ", number='" + number + '\'' +
                ", mediumName='" + mediumName + '\'' +
                ", iid='" + iid + '\'' +
                ", typeName='" + typeName + '\'' +
                ", name='" + name + '\'' +
                ", medium='" + medium + '\'' +
                ", type='" + type + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
