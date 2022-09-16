package com.codecanyon.onlinestore.models;

import com.google.firebase.firestore.Exclude;

public class ProductUnitModel {

    String UnitID;
    String UnitName;

    public ProductUnitModel() {
    }

    public ProductUnitModel(String unitName) {
        UnitName = unitName;
    }

    @Exclude
    public String getUnitID() {
        return UnitID;
    }

    public void setUnitID(String unitID) {
        UnitID = unitID;
    }

    public String getUnitName() {
        return UnitName;
    }

    public void setUnitName(String unitName) {
        UnitName = unitName;
    }
}
