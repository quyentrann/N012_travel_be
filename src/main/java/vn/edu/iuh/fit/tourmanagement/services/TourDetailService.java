package vn.edu.iuh.fit.tourmanagement.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.tourmanagement.models.TourDetail;
import vn.edu.iuh.fit.tourmanagement.repositories.TourDetailRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class TourDetailService {
    @Autowired
    private TourDetailRepository tourDetailRepository;

    public List<TourDetail> getAllTourDetail() {
        return tourDetailRepository.findAll();
    }

    public List<TourDetail> getTourDetailByTourId(Long tourId) {
        return tourDetailRepository.findTourDetailsByTourId(tourId);
    }

    public TourDetail createTourDetail(TourDetail tourDetail) {
        return tourDetailRepository.save(tourDetail);
    }

    public void partialUpdateTourDetailByTourId(Long tourId, Map<String, Object> updates) {
        TourDetail tourDetailToUpdate = tourDetailRepository.findTourDetailsByTourId(tourId).get(0);
//                .orElseThrow(() -> new RuntimeException("TourDetail not found"));
        updates.forEach((key, value) -> {
            switch (key) {
                case "includedServices" -> tourDetailToUpdate.setIncludedServices((String) value);
                case "excludedServices" -> tourDetailToUpdate.setExcludedServices((String) value);
                case "startDate" -> tourDetailToUpdate.setStartDate(LocalDate.parse((String) value));
                case "endDate" -> tourDetailToUpdate.setEndDate(LocalDate.parse((String) value));
            }
        });
        tourDetailRepository.save(tourDetailToUpdate);
    }
}
