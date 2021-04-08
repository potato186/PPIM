package com.ilesson.ppim.entity;

import java.io.Serializable;

public class Currency implements Serializable {
    private String currency;
    private double balance;
    private double balanceInTrade;
    private boolean isDefault;

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public double getBalanceInTrade() {
        return balanceInTrade;
    }

    public void setBalanceInTrade(double balanceInTrade) {
        this.balanceInTrade = balanceInTrade;
    }
}
