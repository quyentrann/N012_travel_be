package vn.edu.iuh.fit.tourmanagement.services;

import vn.edu.iuh.fit.tourmanagement.models.Tour;

public class TourScore {
    private final Tour tour;
    private final double score;
    private final String query;

    public TourScore(Tour tour, double score, String query) {
        this.tour = tour;
        this.score = score;
        this.query = query;
    }

    public Tour getTour() {
        return tour;
    }

    public double getScore() {
        return score;
    }

    public String getQuery() {
        return query;
    }
}