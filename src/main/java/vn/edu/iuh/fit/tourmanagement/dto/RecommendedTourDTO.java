package vn.edu.iuh.fit.tourmanagement.dto;

import vn.edu.iuh.fit.tourmanagement.models.Tour;

public class RecommendedTourDTO {
    private Tour tour;
    private String source; // "Click" hoặc từ khóa như "Đà Lạt"

    public RecommendedTourDTO(Tour tour, String source) {
        this.tour = tour;
        this.source = source;
    }

    // Getters
    public Tour getTour() {
        return tour;
    }

    public String getSource() {
        return source;
    }
}
