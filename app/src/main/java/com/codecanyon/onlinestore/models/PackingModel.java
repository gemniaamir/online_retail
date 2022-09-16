package com.codecanyon.onlinestore.models;

import com.google.firebase.firestore.Exclude;

public class PackingModel {
    String TypeName;
    String typeID;

    public PackingModel(String typeName) {
        this.TypeName = typeName;
    }

    public PackingModel() {
    }

    public String getTypeName() {
        return TypeName;
    }

    public void setTypeName(String typeName) {
        TypeName = typeName;
    }

    @Exclude
    public String getTypeID() {
        return typeID;
    }

    public void setTypeID(String typeID) {
        this.typeID = typeID;
    }
}
