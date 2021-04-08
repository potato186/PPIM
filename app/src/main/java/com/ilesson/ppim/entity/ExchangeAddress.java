package com.ilesson.ppim.entity;

public class ExchangeAddress {
    private AddressInfo addressInfo;

    public ExchangeAddress(AddressInfo addressInfo) {
        this.addressInfo = addressInfo;
    }

    public AddressInfo getAddressInfo() {
        return addressInfo;
    }

    public void setAddressInfo(AddressInfo addressInfo) {
        this.addressInfo = addressInfo;
    }
}
