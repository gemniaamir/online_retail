package com.codecanyon.onlinestore.models;

import com.google.firebase.firestore.Exclude;

public class GeneralProduct {

    String price;
    String title;
    String unit;
    int backgroundColor;
    String weight;
    String id;
    String backgroundImage = "";

    public GeneralProduct(String price, String title, String unit, String weight, int backgroundColor, String productImage) {
        this.price = price;
        this.title = title;
        this.weight = weight;
        this.unit = unit;
        this.backgroundColor = backgroundColor;
        this.backgroundImage = productImage;
    }

    @Exclude
    public String getId() {
        return id;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getBackgroundImage() {
        return backgroundImage;
    }

    public void setBackgroundImage(String backgroundImage) {
        this.backgroundImage = backgroundImage;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }
}
