package com.codecanyon.onlinestore.models;

import com.google.firebase.firestore.Exclude;

public class ProductModel {

    String ProductID;
    Boolean IsDiscount;
    Boolean IsFeatured;
    String ProductCat;
    String ProductDiscountAmt;
    String Currency;
    String ProductImage;
    String ProductName;
    String ProductPacking;
    String ProductPrice;
    String WeightUnit;
    String Weight;
    String Desc;
    String CompanyName;

    public ProductModel(){

    }

    public ProductModel(Boolean isDiscount, Boolean isFeatured, String productCat,
                        String productDiscountAmt, String currency, String productImage, String productName,
                        String productPacking, String productPrice, String productUnit, String weight,
                        String desc, String companyName) {
        IsDiscount = isDiscount;
        IsFeatured = isFeatured;
        ProductCat = productCat;
        ProductDiscountAmt = productDiscountAmt;
        Currency = currency;
        ProductImage = productImage;
        ProductName = productName;
        ProductPacking = productPacking;
        ProductPrice = productPrice;
        WeightUnit = productUnit;
        Weight = weight;
        Desc = desc;
        CompanyName = companyName;
    }

    @Exclude
    public String getProductID() {
        return ProductID;
    }

    public void setProductID(String productID) {
        ProductID = productID;
    }

    public String getDesc() {
        return Desc;
    }

    public void setDesc(String desc) {
        Desc = desc;
    }

    public String getCompanyName() {
        return CompanyName;
    }

    public void setCompanyName(String companyName) {
        CompanyName = companyName;
    }

    public String getCurrency() {
        return Currency;
    }

    public void setCurrency(String currency) {
        Currency = currency;
    }

    public String getWeight() {
        return Weight;
    }

    public void setWeight(String weight) {
        Weight = weight;
    }

    public Boolean getDiscount() {
        return IsDiscount;
    }

    public void setDiscount(Boolean discount) {
        IsDiscount = discount;
    }

    public Boolean getFeatured() {
        return IsFeatured;
    }

    public void setFeatured(Boolean featured) {
        IsFeatured = featured;
    }

    public String getProductCat() {
        return ProductCat;
    }

    public void setProductCat(String productCat) {
        ProductCat = productCat;
    }

    public String getProductDiscountAmt() {
        return ProductDiscountAmt;
    }

    public void setProductDiscountAmt(String productDiscountAmt) {
        ProductDiscountAmt = productDiscountAmt;
    }

    public String getPriceCurrency() {
        return Currency;
    }

    public void setPriceCurrency(String priceCurrency) {
        Currency = priceCurrency;
    }

    public String getProductImage() {
        return ProductImage;
    }

    public void setProductImage(String productImage) {
        ProductImage = productImage;
    }

    public String getProductName() {
        return ProductName;
    }

    public void setProductName(String productName) {
        ProductName = productName;
    }

    public String getProductPacking() {
        return ProductPacking;
    }

    public void setProductPacking(String productPacking) {
        ProductPacking = productPacking;
    }

    public String getProductPrice() {
        return ProductPrice;
    }

    public void setProductPrice(String productPrice) {
        ProductPrice = productPrice;
    }

    public String getWeightUnit() {
        return WeightUnit;
    }

    public void setWeightUnit(String weightUnit) {
        WeightUnit = weightUnit;
    }
}
