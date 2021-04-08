/**
  * Copyright 2021 json.cn 
  */
package com.ilesson.ppim.entity;
import java.util.List;

public class Express {

    private String number;
    private String type;
    private String typename;
    private String logo;
    private List<ExpressInfo> list;
    private int deliverystatus;
    private int issign;

    public List<ExpressInfo> getList() {
        return list;
    }

    public void setList(List<ExpressInfo> list) {
        this.list = list;
    }

    public void setNumber(String number) {
         this.number = number;
     }
     public String getNumber() {
         return number;
     }

    public void setType(String type) {
         this.type = type;
     }
     public String getType() {
         return type;
     }

    public void setTypename(String typename) {
         this.typename = typename;
     }
     public String getTypename() {
         return typename;
     }

    public void setLogo(String logo) {
         this.logo = logo;
     }
     public String getLogo() {
         return logo;
     }


    public void setDeliverystatus(int deliverystatus) {
         this.deliverystatus = deliverystatus;
     }
     public int getDeliverystatus() {
         return deliverystatus;
     }

    public void setIssign(int issign) {
         this.issign = issign;
     }
     public int getIssign() {
         return issign;
     }

}