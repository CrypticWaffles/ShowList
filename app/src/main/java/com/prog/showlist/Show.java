package com.prog.showlist;

public class Show {
    private String title;
    private double rating;
    private String description;
    private int imageResourceId;

    public Show(String title, double rating, String description, int imageResourceId) {
        this.title = title;
        this.rating = rating;
        this.description = description;
        this.imageResourceId = imageResourceId;
    }

    public String getTitle() {
        return title;
    }

    public String getRating() {
        return rating + "/10";
    }

    public String getDescription() {
        return description;
    }

    public int getImageResourceId() {
        return imageResourceId;
    }

    public String toString() {
        return this.title;
    }
}
