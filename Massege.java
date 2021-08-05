package com.example.chatapp;

public class Massege {
    private String author;
    private String text;
    private long data;
    private String imageURL;

    public Massege(String author, String text, long data, String imageURL) {
        this.author = author;
        this.text = text;
        this.data = data;
        this.imageURL = imageURL;
    }

    public Massege() {
    }

    public long getData() {
        return data;
    }

    public void setData(long data) {
        this.data = data;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
