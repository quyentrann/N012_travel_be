package vn.edu.iuh.fit.tourmanagement.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.tourmanagement.models.TourSchedule;
import vn.edu.iuh.fit.tourmanagement.repositories.TourScheduleRepository;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class TourScheduleService {
    @Autowired
    private TourScheduleRepository tourScheduleRepository;

    public List<TourSchedule> getAllTourSchedule() {
        return tourScheduleRepository.findAll();
    }

    public List<TourSchedule> getTourScheduleByTourId(Long tourId) {
        return tourScheduleRepository.findTourSchedulesByTourId(tourId);
    }

    public Optional<TourSchedule> getTourScheduleById(Long id) {
        return tourScheduleRepository.findById(id);
    }

    public TourSchedule createTourSchedule(TourSchedule tourSchedule) {
        return tourScheduleRepository.save(tourSchedule);
    }

    public void partialUpdateTourScheduleBYId(Long id, Map<String, Object> updates) {
        TourSchedule tourScheduleToUpdate = tourScheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("TourSchedule not found"));
        updates.forEach((key, value) -> {
            switch (key) {
                case "dayNumber" -> tourScheduleToUpdate.setDayNumber((Integer) value);
                case "location" -> tourScheduleToUpdate.setLocation((String) value);
                case "stransport" -> tourScheduleToUpdate.setStransport((String) value);
                case "activities" -> tourScheduleToUpdate.setActivities((String) value);
                case "meal" -> tourScheduleToUpdate.setMeal((String) value);
                case "arrivalTime" -> tourScheduleToUpdate.setArrivalTime(LocalTime.parse((String) value));
                case "departureTime" -> tourScheduleToUpdate.setDepartureTime(LocalTime.parse((String) value));
            }
        });
        tourScheduleRepository.save(tourScheduleToUpdate);
        }

        public boolean deleteTourScheduleById(Long id) {
            if (tourScheduleRepository.existsById(id)) {
                tourScheduleRepository.deleteById(id);
                return true;
            }
            return false;
        }
}
