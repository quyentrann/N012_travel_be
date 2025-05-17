package vn.edu.iuh.fit.tourmanagement.exceptions;

public class TourNotFoundException extends RuntimeException {
    public TourNotFoundException(String message) {
        super(message);
    }
}