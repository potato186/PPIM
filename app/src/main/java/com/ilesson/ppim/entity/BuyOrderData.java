package com.ilesson.ppim.entity;

import java.util.List;

public class BuyOrderData {
    private Shop shop;
    private List<AddressInfo> address;
    private List<Options> options;
    private List<InvoiceInfo> invoice;
    private Produce produce;

    public Shop getShop() {
        return shop;
    }

    public void setShop(Shop shop) {
        this.shop = shop;
    }

    public List<AddressInfo> getAddress() {
        return address;
    }

    public void setAddress(List<AddressInfo> address) {
        this.address = address;
    }

    public List<Options> getOptions() {
        return options;
    }

    public void setOptions(List<Options> options) {
        this.options = options;
    }

    public List<InvoiceInfo> getInvoice() {
        return invoice;
    }

    public void setInvoice(List<InvoiceInfo> invoice) {
        this.invoice = invoice;
    }

    public Produce getProduce() {
        return produce;
    }

    public void setProduce(Produce produce) {
        this.produce = produce;
    }
}
