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
public class ScoreData {

    private ScoreShop shop;
    private List<AddressInfo> address;
    private List<Produces> produces;
    public void setShop(ScoreShop shop) {
         this.shop = shop;
     }
     public ScoreShop getShop() {
         return shop;
     }

    public void setAddress(List<AddressInfo> address) {
         this.address = address;
     }
     public List<AddressInfo> getAddress() {
         return address;
     }

    public void setProduces(List<Produces> produces) {
         this.produces = produces;
     }
     public List<Produces> getProduces() {
         return produces;
     }

}