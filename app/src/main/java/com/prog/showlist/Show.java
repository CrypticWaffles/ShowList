package com.prog.showlist;

public class Show {
    private int id;
    private String title;
    private double rating;
    private String description;
    private int imageResourceId;
    private String imageUrl;
    private String status;
    private boolean favorite;

    public Show(int id, String title, double rating, String description,
                int imageResourceId, String imageUrl, String status, boolean favorite) {
        this.id = id;
        this.title = title;
        this.rating = rating;
        this.description = description;
        this.imageResourceId = imageResourceId;
        this.imageUrl = imageUrl;
        this.status = (status != null) ? status : "Plan to Watch";
        this.favorite = favorite;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public double getRatingValue() { return rating; }
    public String getRating() { return rating + "/10"; }
    public String getDescription() { return description; }
    public int getImageResourceId() { return imageResourceId; }
    public String getImageUrl() { return imageUrl; }
    public String getStatus() { return status; }
    public boolean isFavorite() { return favorite; }
    public void setFavorite(boolean favorite) { this.favorite = favorite; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() { return title; }
}
