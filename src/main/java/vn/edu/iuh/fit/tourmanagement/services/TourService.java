package vn.edu.iuh.fit.tourmanagement.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.tourmanagement.models.Review;
import vn.edu.iuh.fit.tourmanagement.models.Tour;
import vn.edu.iuh.fit.tourmanagement.repositories.TourRepository;
import java.util.List;
import java.util.Optional;

@Service
public class TourService {

    @Autowired
    private TourRepository tourRepository;



    public List<Tour> getAllTours() {
        return tourRepository.findAll();
    }

    public Tour getTourById(Long id) {
        Optional<Tour> tour = tourRepository.findById(id);
        return tour.orElse(null);
    }

    @Transactional
    public List<Review> getTourReviews(Long tourId) {
        Tour tour = tourRepository.findById(tourId).orElseThrow(() -> new RuntimeException("Tour không tồn tại"));
        return tour.getReviews(); // Lúc này Hibernate mới load dữ liệu
    }

    public List<Tour> getSimilarTours(Long currentTourId) {
        // Lấy thông tin tour hiện tại
        Tour currentTour = tourRepository.findById(currentTourId)
                .orElseThrow(() -> new RuntimeException("Tour không tồn tại"));

        // Gọi repository để lấy danh sách các tour tương tự
        return tourRepository.findSimilarTours(
                currentTourId,  // Tour ID để loại trừ tour hiện tại
                currentTour.getName(),  // Tên tour hiện tại
                currentTour.getLocation()  // Địa điểm tour hiện tại
        );
    }



    public Tour createTour(Tour tour) {
        return tourRepository.save(tour);
    }

    public Tour updateTour(Tour tour) {
        return tourRepository.save(tour);
    }

    public boolean deleteTour(Long id) {
        if (!tourRepository.existsById(id)) {
            return false;
        }
        tourRepository.deleteById(id);
        return true;
    }



}
