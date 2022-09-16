package com.codecanyon.onlinestore.models;

public class DiscountedProduct {

    int iconImage;
    String title;
    String discount;

    public DiscountedProduct(int iconImage, String title, String discount) {
        this.iconImage = iconImage;
        this.title = title;
        this.discount = discount;
    }

    public int getBackgroundColor() {
        return iconImage;
    }

    public void setBackgroundColor(int iconImage) {
        this.iconImage = iconImage;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDiscount() {
        return discount;
    }

    public void setDiscount(String discount) {
        this.discount = discount;
    }
}
