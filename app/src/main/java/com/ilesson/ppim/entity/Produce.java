/**
  * Copyright 2021 json.cn 
  */
package com.ilesson.ppim.entity;

import java.io.Serializable;

/**
 * Auto-generated: 2021-03-31 11:18:40
 *
 * @author json.cn (i@json.cn)
 * @website http://www.json.cn/java2pojo/
 */
public class Produce  implements Serializable {

    private String name;
    private String icon;
    private int id;
    private String detail;
    private String image_intro;
    private int sid;
    public void setName(String name) {
         this.name = name;
     }
     public String getName() {
         return name;
     }

    public void setIcon(String icon) {
         this.icon = icon;
     }
     public String getIcon() {
         return icon;
     }

    public void setId(int id) {
         this.id = id;
     }
     public int getId() {
         return id;
     }

    public void setDetail(String detail) {
         this.detail = detail;
     }
     public String getDetail() {
         return detail;
     }

    public void setImage_intro(String image_intro) {
         this.image_intro = image_intro;
     }
     public String getImage_intro() {
         return image_intro;
     }

    public void setSid(int sid) {
         this.sid = sid;
     }
     public int getSid() {
         return sid;
     }

}