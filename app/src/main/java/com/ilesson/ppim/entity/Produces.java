/**
  * Copyright 2021 json.cn 
  */
package com.ilesson.ppim.entity;
import java.util.List;

/**
 * Auto-generated: 2021-03-13 18:4:29
 *
 * @author json.cn (i@json.cn)
 * @website http://www.json.cn/java2pojo/
 */
public class Produces {

    private int id;
    private int sid;
    private String produce;
    private String name;
    private String desc;
    private String icon;
    private int price;
    private String detail;
    private String imageIntro;
    private int tag;
    private List<Selections> selections;
    public void setId(int id) {
         this.id = id;
     }
     public int getId() {
         return id;
     }

    public void setSid(int sid) {
         this.sid = sid;
     }
     public int getSid() {
         return sid;
     }

    public void setProduce(String produce) {
         this.produce = produce;
     }
     public String getProduce() {
         return produce;
     }

    public void setName(String name) {
         this.name = name;
     }
     public String getName() {
         return name;
     }

    public void setDesc(String desc) {
         this.desc = desc;
     }
     public String getDesc() {
         return desc;
     }

    public void setIcon(String icon) {
         this.icon = icon;
     }
     public String getIcon() {
         return icon;
     }

    public void setPrice(int price) {
         this.price = price;
     }
     public int getPrice() {
         return price;
     }

    public void setDetail(String detail) {
         this.detail = detail;
     }
     public String getDetail() {
         return detail;
     }

    public void setImageIntro(String imageIntro) {
         this.imageIntro = imageIntro;
     }
     public String getImageIntro() {
         return imageIntro;
     }

    public void setTag(int tag) {
         this.tag = tag;
     }
     public int getTag() {
         return tag;
     }

    public void setSelections(List<Selections> selections) {
         this.selections = selections;
     }
     public List<Selections> getSelections() {
         return selections;
     }

}