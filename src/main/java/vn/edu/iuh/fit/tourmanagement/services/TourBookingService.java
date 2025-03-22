package vn.edu.iuh.fit.tourmanagement.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.tourmanagement.models.TourBooking;
import vn.edu.iuh.fit.tourmanagement.repositories.TourBookingRepository;

import java.util.List;

@Service
public class TourBookingService {
    @Autowired
    private TourBookingRepository tourBookingRepository;

    public List<TourBooking> getListTourBooking() {
        return tourBookingRepository.findAll();
    }

    public List<TourBooking> getTourBookingByCustomerId(Long customerId) {
        return tourBookingRepository.findByCustomerId(customerId);
    }

    public TourBooking getTourBookingById(Long id) {
        return tourBookingRepository.findById(id).orElse(null);
    }
}
