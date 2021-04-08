/**
  * Copyright 2021 json.cn 
  */
package com.ilesson.ppim.entity;
import java.util.HashMap;
import java.util.List;

/**
 * Auto-generated: 2021-03-31 11:18:40
 *
 * @author json.cn (i@json.cn)
 * @website http://www.json.cn/java2pojo/
 */
public class WaresDetialData {

    private Shop shop;
    private List<Options> options;
    private HashMap<String,String> attributes;
    private Produce produce;
    private List<InvoiceInfo> invoice;
    private List<AddressInfo> address;

    public List<InvoiceInfo> getInvoice() {
        return invoice;
    }

    public void setInvoice(List<InvoiceInfo> invoice) {
        this.invoice = invoice;
    }

    public List<AddressInfo> getAddress() {
        return address;
    }

    public void setAddress(List<AddressInfo> address) {
        this.address = address;
    }

    public void setShop(Shop shop) {
         this.shop = shop;
     }
     public Shop getShop() {
         return shop;
     }

    public void setOptions(List<Options> options) {
         this.options = options;
     }
     public List<Options> getOptions() {
         return options;
     }

    public HashMap<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(HashMap<String, String> attributes) {
        this.attributes = attributes;
    }

    public void setProduce(Produce produce) {
         this.produce = produce;
     }
     public Produce getProduce() {
         return produce;
     }

}