package vn.edu.iuh.fit.tourmanagement.exceptions;

public class TourAlreadyFavoritedException extends RuntimeException {
    public TourAlreadyFavoritedException(String message) {
        super(message);
    }
}