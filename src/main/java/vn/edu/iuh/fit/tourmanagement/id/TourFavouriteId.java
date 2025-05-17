package vn.edu.iuh.fit.tourmanagement.id;

import java.io.Serializable;
import java.util.Objects;

public class TourFavouriteId implements Serializable {
    private Long customer;
    private Long tour;

    public TourFavouriteId() {}

    public TourFavouriteId(Long customer, Long tour) {
        this.customer = customer;
        this.tour = tour;
    }

    public Long getCustomer() {
        return customer;
    }

    public void setCustomer(Long customer) {
        this.customer = customer;
    }

    public Long getTour() {
        return tour;
    }

    public void setTour(Long tour) {
        this.tour = tour;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TourFavouriteId that = (TourFavouriteId) o;
        return Objects.equals(customer, that.customer) && Objects.equals(tour, that.tour);
    }

    @Override
    public int hashCode() {
        return Objects.hash(customer, tour);
    }
}