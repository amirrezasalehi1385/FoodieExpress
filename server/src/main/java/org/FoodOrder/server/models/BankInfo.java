package org.FoodOrder.server.models;

import jakarta.persistence.Embeddable;

@Embeddable
public class BankInfo {
    private String bank_name;
    private String account_number;
    public BankInfo(){}
    public BankInfo(String bank_name, String account_number) {
        this.bank_name = bank_name;
        this.account_number = account_number;
    }
    public String getBank_name() {
        return bank_name;
    }
    public void setBank_name(String bank_name) {
        this.bank_name = bank_name;
    }
    public String getAccount_number() {
        return account_number;
    }
    public void setAccount_number(String account_number) {
        this.account_number = account_number;
    }
}
