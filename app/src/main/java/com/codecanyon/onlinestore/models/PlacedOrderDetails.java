package com.codecanyon.onlinestore.models;

import com.google.firebase.firestore.Exclude;

public class PlacedOrderDetails {

    String cartID;
    String userID;
    String address;
    String paymentType;
    String grossAmountTotal;
    String discountAmountTotal;
    String deliveryCharges;
    String netAmountTotal;
    String orderDateTime;
    String ID;

    public String getOrderDateTime() {
        return orderDateTime;
    }

    public void setOrderDateTime(String orderDateTime) {
        this.orderDateTime = orderDateTime;
    }

    public PlacedOrderDetails() {
    }

    public PlacedOrderDetails(String cartID, String userID, String address, String paymentType,
                              String grossAmountTotal, String discountAmountTotal,
                              String deliveryCharges, String netAmountTotal, String orderDateTime) {
        this.cartID = cartID;
        this.userID = userID;
        this.address = address;
        this.paymentType = paymentType;
        this.grossAmountTotal = grossAmountTotal;
        this.discountAmountTotal = discountAmountTotal;
        this.deliveryCharges = deliveryCharges;
        this.netAmountTotal = netAmountTotal;
        this.orderDateTime = orderDateTime;
    }

    @Exclude
    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getCartID() {
        return cartID;
    }

    public void setCartID(String cartID) {
        this.cartID = cartID;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public String getGrossAmountTotal() {
        return grossAmountTotal;
    }

    public void setGrossAmountTotal(String grossAmountTotal) {
        this.grossAmountTotal = grossAmountTotal;
    }

    public String getDiscountAmountTotal() {
        return discountAmountTotal;
    }

    public void setDiscountAmountTotal(String discountAmountTotal) {
        this.discountAmountTotal = discountAmountTotal;
    }

    public String getDeliveryCharges() {
        return deliveryCharges;
    }

    public void setDeliveryCharges(String deliveryCharges) {
        this.deliveryCharges = deliveryCharges;
    }

    public String getNetAmountTotal() {
        return netAmountTotal;
    }

    public void setNetAmountTotal(String netAmountTotal) {
        this.netAmountTotal = netAmountTotal;
    }
}
