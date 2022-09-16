package com.codecanyon.onlinestore.models;

import com.google.firebase.firestore.Exclude;

public class DiscountCurrency {

    String CurrencyName;
    String CurrencyShort;
    String CurrencyID;

    public DiscountCurrency(String currencyName, String currencyShort) {
        CurrencyName = currencyName;
        CurrencyShort = currencyShort;
    }

    public DiscountCurrency() {
    }

    public String getCurrencyName() {
        return CurrencyName;
    }

    public void setCurrencyName(String currencyName) {
        CurrencyName = currencyName;
    }

    public String getCurrencyShort() {
        return CurrencyShort;
    }

    public void setCurrencyShort(String currencyShort) {
        CurrencyShort = currencyShort;
    }

    @Exclude
    public String getCurrencyID() {
        return CurrencyID;
    }

    public void setCurrencyID(String currencyID) {
        CurrencyID = currencyID;
    }
}
