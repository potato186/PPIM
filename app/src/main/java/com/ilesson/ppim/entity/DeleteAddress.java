package com.ilesson.ppim.entity;

public class DeleteAddress {
    private AddressInfo addressInfo;

    public DeleteAddress(AddressInfo addressInfo) {
        this.addressInfo = addressInfo;
    }

    public AddressInfo getAddressInfo() {
        return addressInfo;
    }

    public void setAddressInfo(AddressInfo addressInfo) {
        this.addressInfo = addressInfo;
    }
}
