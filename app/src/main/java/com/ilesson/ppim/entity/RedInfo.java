/**
  * Copyright 2020 bejson.com 
  */
package com.ilesson.ppim.entity;
import java.util.List;

/**
 * Auto-generated: 2020-05-30 10:27:7
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
public class RedInfo {

    private RedpacketDetail info;
    private int money;
    private List<SplitList> list;
    public void setInfo(RedpacketDetail info) {
         this.info = info;
     }
     public RedpacketDetail getInfo() {
         return info;
     }

    public void setMoney(int money) {
         this.money = money;
     }
     public int getMoney() {
         return money;
     }

    public List<SplitList> getList() {
        return list;
    }

    public void setList(List<SplitList> list) {
        this.list = list;
    }
}