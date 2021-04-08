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
public class Shop  implements Serializable {

    private String shopkeeper;
    private String name;
    private String logo;
    private int id;
    private String desc;
    private String group;
    public void setShopkeeper(String shopkeeper) {
         this.shopkeeper = shopkeeper;
     }
     public String getShopkeeper() {
         return shopkeeper;
     }

    public void setName(String name) {
         this.name = name;
     }
     public String getName() {
         return name;
     }

    public void setLogo(String logo) {
         this.logo = logo;
     }
     public String getLogo() {
         return logo;
     }

    public void setId(int id) {
         this.id = id;
     }
     public int getId() {
         return id;
     }

    public void setDesc(String desc) {
         this.desc = desc;
     }
     public String getDesc() {
         return desc;
     }

    public void setGroup(String group) {
         this.group = group;
     }
     public String getGroup() {
         return group;
     }

}