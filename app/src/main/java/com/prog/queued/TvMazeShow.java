package com.prog.queued;

public class TvMazeShow {
    private final String name;
    private final double rating;
    private final String summary;
    private final String imageUrl;

    public TvMazeShow(String name, double rating, String summary, String imageUrl) {
        this.name = name;
        this.rating = rating;
        this.summary = summary;
        this.imageUrl = imageUrl;
    }

    public String getName() { return name; }
    public double getRating() { return rating; }
    public String getSummary() { return summary; }
    public String getImageUrl() { return imageUrl; }
}
