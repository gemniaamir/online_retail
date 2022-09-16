package com.codecanyon.onlinestore.models;

import com.google.firebase.firestore.Exclude;

public class CategoryClass {

    String CatName;
    String CatID;
    String CatImage;

    public CategoryClass(String catName, String catImage) {
        CatName = catName;
        CatImage = catImage;
    }

    public CategoryClass() {
    }

    public String getCatImage() {
        return CatImage;
    }

    public void setCatImage(String catImage) {
        CatImage = catImage;
    }

    public String getCatName() {
        return CatName;
    }

    public void setCatName(String catName) {
        CatName = catName;
    }

    @Exclude
    public String getCatID() {
        return CatID;
    }

    public void setCatID(String catID) {
        CatID = catID;
    }
}
