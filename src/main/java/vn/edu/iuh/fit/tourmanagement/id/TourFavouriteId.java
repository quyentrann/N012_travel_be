package vn.edu.iuh.fit.tourmanagement.id;

import lombok.Data;
import vn.edu.iuh.fit.tourmanagement.models.Customer;
import vn.edu.iuh.fit.tourmanagement.models.Tour;

import java.io.Serializable;

@Data
public class TourFavouriteId implements Serializable {
    private Customer customer;
    private Tour tour;
}
