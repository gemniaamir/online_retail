package com.codecanyon.onlinestore.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;

public class UserClass {
    String ID;
    String UID;
    String fullName;
    String email;
    String password;
    Timestamp CreationDate;
    String Phone;

    public UserClass(String UID, String fullName, String email, String password, Timestamp creationDate, String phone) {
        this.UID = UID;
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        CreationDate = creationDate;
        Phone = phone;
    }

    public UserClass(){

    }

    @Exclude
    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Timestamp getCreationDate() {
        return CreationDate;
    }

    public void setCreationDate(Timestamp creationDate) {
        CreationDate = creationDate;
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }
}
