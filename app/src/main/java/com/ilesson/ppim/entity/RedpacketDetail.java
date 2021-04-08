/**
  * Copyright 2020 bejson.com 
  */
package com.ilesson.ppim.entity;

/**
 * Auto-generated: 2020-05-30 10:27:7
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
public class RedpacketDetail {

    private int money;
    private int count;
    private String sender;
    private String info;
    private long date;
    private boolean finished;
    public void setMoney(int money) {
         this.money = money;
     }
     public int getMoney() {
         return money;
     }

    public void setCount(int count) {
         this.count = count;
     }
     public int getCount() {
         return count;
     }

    public void setSender(String sender) {
         this.sender = sender;
     }
     public String getSender() {
         return sender;
     }

    public void setInfo(String info) {
         this.info = info;
     }
     public String getInfo() {
         return info;
     }

    public void setDate(long date) {
         this.date = date;
     }
     public long getDate() {
         return date;
     }

    public void setFinished(boolean finished) {
         this.finished = finished;
     }
     public boolean getFinished() {
         return finished;
     }

}