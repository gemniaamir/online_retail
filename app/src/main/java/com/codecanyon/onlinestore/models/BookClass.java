package com.codecanyon.onlinestore.models;

import com.google.firebase.firestore.Exclude;

public class BookClass {

    private String bookName;
    private String bookDescription;

    @Exclude
    public String getBookID() {
        return bookID;
    }

    public void setBookID(String bookID) {
        this.bookID = bookID;
    }

    private String bookID;

    public BookClass() {
    }

    public BookClass(String bookName, String bookDescription) {
        this.bookName = bookName;
        this.bookDescription = bookDescription;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public String getBookDescription() {
        return bookDescription;
    }

    public void setBookDescription(String bookDescription) {
        this.bookDescription = bookDescription;
    }
}
