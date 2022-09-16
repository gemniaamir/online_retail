package com.codecanyon.onlinestore.models;

import com.google.firebase.firestore.Exclude;

public class CartModel {

    String CartID;
    String ProductName;
    Integer PriceAfterDiscount;
    Integer DiscountAmount;
    String ProductImageURL;
    String CompanyName;
    Integer Weight;
    String WeightUnit;
    Integer Quantity = 1;
    String UserID;
    String ProductID;
    String TotalAmount;

    public CartModel() {
    }

    public CartModel(String productName, Integer priceAfterDiscount, Integer discountAmount, String productImageURL,
                     String companyName, Integer weight, String weightUnit, Integer quantity, String userID, String productID,
                     String totalAmount) {
        ProductName = productName;
        PriceAfterDiscount = priceAfterDiscount;
        DiscountAmount = discountAmount;
        ProductImageURL = productImageURL;
        CompanyName = companyName;
        Weight = weight;
        WeightUnit = weightUnit;
        Quantity = quantity;
        UserID = userID;
        ProductID = productID;
        TotalAmount = totalAmount;
    }

    @Exclude
    public String getCartID() {
        return CartID;
    }

    public void setCartID(String cartID) {
        CartID = cartID;
    }

    public String getProductName() {
        return ProductName;
    }

    public void setProductName(String productName) {
        ProductName = productName;
    }

    public Integer getPriceAfterDiscount() {
        return PriceAfterDiscount;
    }

    public void setPriceAfterDiscount(Integer priceAfterDiscount) {
        PriceAfterDiscount = priceAfterDiscount;
    }

    public String getTotalAmount() {
        return TotalAmount;
    }

    public void setTotalAmount(String totalAmount) {
        TotalAmount = totalAmount;
    }

    public Integer getDiscountAmount() {
        return DiscountAmount;
    }

    public void setDiscountAmount(Integer discountAmount) {
        DiscountAmount = discountAmount;
    }

    public String getProductImageURL() {
        return ProductImageURL;
    }

    public void setProductImageURL(String productImageURL) {
        ProductImageURL = productImageURL;
    }

    public String getCompanyName() {
        return CompanyName;
    }

    public void setCompanyName(String companyName) {
        CompanyName = companyName;
    }

    public Integer getWeight() {
        return Weight;
    }

    public void setWeight(Integer weight) {
        Weight = weight;
    }

    public String getWeightUnit() {
        return WeightUnit;
    }

    public void setWeightUnit(String weightUnit) {
        WeightUnit = weightUnit;
    }

    public Integer getQuantity() {
        return Quantity;
    }

    public void setQuantity(Integer quantity) {
        Quantity = quantity;
    }

    public String getUserID() {
        return UserID;
    }

    public void setUserID(String userID) {
        UserID = userID;
    }

    public String getProductID() {
        return ProductID;
    }

    public void setProductID(String productID) {
        ProductID = productID;
    }
}
