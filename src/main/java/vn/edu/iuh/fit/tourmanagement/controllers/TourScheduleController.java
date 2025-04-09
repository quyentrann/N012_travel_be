package vn.edu.iuh.fit.tourmanagement.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.tourmanagement.models.Tour;
import vn.edu.iuh.fit.tourmanagement.models.TourSchedule;
import vn.edu.iuh.fit.tourmanagement.services.TourScheduleService;
import vn.edu.iuh.fit.tourmanagement.services.TourService;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/schedules")
public class TourScheduleController {
    @Autowired
    private TourScheduleService tourScheduleService;
    @Autowired
    private TourService tourService;

    @GetMapping
    public ResponseEntity<List<TourSchedule>> getAllSchedules() {
        List<TourSchedule> schedules = tourScheduleService.getAllTourSchedule();
        if (schedules.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(schedules,HttpStatus.OK);
    }

    @GetMapping("/tour/{tourId}")
    public ResponseEntity<?> getTourScheduleByTourId(@PathVariable Long tourId) {
        List<TourSchedule> schedule = tourScheduleService.getTourScheduleByTourId(tourId);
        if (schedule == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(schedule);
    }

    @PostMapping
    public ResponseEntity<TourSchedule> createTourSchedule(@RequestBody TourSchedule tourSchedule) {
        TourSchedule createdTourSchedule = tourScheduleService.createTourSchedule(tourSchedule);
        return new ResponseEntity<>(createdTourSchedule, HttpStatus.CREATED);
    }

    @PostMapping("/tour/{tourId}")
    public ResponseEntity<?> createTourSchedule(@PathVariable Long tourId, @RequestBody Map<String, Object> payload) {
        Tour tour = tourService.getTourById(tourId);
        if (tour == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Tour không tồn tại!");
        TourSchedule schedule = new TourSchedule();
        schedule.setTour(tour);

        schedule.setDayNumber((Integer) payload.get("dayNumber"));
        schedule.setLocation((String) payload.get("location"));
        schedule.setStransport((String) payload.get("stransport"));
        schedule.setActivities((String) payload.get("activities"));
        schedule.setMeal((String) payload.get("meal"));
        schedule.setArrivalTime(LocalTime.parse((String) payload.get("arrivalTime")));
        schedule.setDepartureTime(LocalTime.parse((String) payload.get("departureTime")));
        TourSchedule savedSchedule = tourScheduleService.createTourSchedule(schedule);
        return ResponseEntity.ok(savedSchedule);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<String> partialUpdateTourScheduleById(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        tourScheduleService.partialUpdateTourScheduleBYId(id, updates);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTourScheduleById(@PathVariable Long id) {
        return tourScheduleService.getTourScheduleById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTourScheduleById(@PathVariable Long id) {
        if (tourScheduleService.deleteTourScheduleById(id)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

}
