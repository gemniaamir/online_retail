package com.codecanyon.onlinestore.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.GeoPoint;

public class AddressModel {

    String AddressType;
    String AddressId;
    String Street;
    String HouseNo;
    String AreaName;
    String NearBy;
    Timestamp CreatedTime;
    GeoPoint MapLocation;

    public AddressModel() {
    }

    public AddressModel(String addressType, String street, String houseNo,
                        String nearBy, Timestamp createdTime, GeoPoint mapLocation, String areaName) {
        AddressType = addressType;
        Street = street;
        HouseNo = houseNo;
        NearBy = nearBy;
        CreatedTime = createdTime;
        MapLocation = mapLocation;
        AreaName = areaName;
    }

    public String getAreaName() {
        return AreaName;
    }

    public void setAreaName(String areaName) {
        AreaName = areaName;
    }

    public String getAddressType() {
        return AddressType;
    }

    public void setAddressType(String addressType) {
        AddressType = addressType;
    }

    @Exclude
    public String getAddressId() {
        return AddressId;
    }

    public void setAddressId(String addressId) {
        AddressId = addressId;
    }

    public String getStreet() {
        return Street;
    }

    public void setStreet(String street) {
        Street = street;
    }

    public String getHouseNo() {
        return HouseNo;
    }

    public void setHouseNo(String houseNo) {
        HouseNo = houseNo;
    }

    public String getNearBy() {
        return NearBy;
    }

    public void setNearBy(String nearBy) {
        NearBy = nearBy;
    }

    public Timestamp getCreatedTime() {
        return CreatedTime;
    }

    public void setCreatedTime(Timestamp createdTime) {
        CreatedTime = createdTime;
    }

    public GeoPoint getMapLocation() {
        return MapLocation;
    }

    public void setMapLocation(GeoPoint mapLocation) {
        MapLocation = mapLocation;
    }
}
