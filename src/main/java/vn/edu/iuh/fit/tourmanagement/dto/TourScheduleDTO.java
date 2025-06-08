package vn.edu.iuh.fit.tourmanagement.dto;

import lombok.*;
import vn.edu.iuh.fit.tourmanagement.models.TourSchedule;

import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TourScheduleDTO {
    private Long scheduleId;
    private int dayNumber;
    private String location;
    private String transport;
    private String activities;
    private String meal;
    private LocalTime arrivalTime;
    private LocalTime departureTime;

    public TourScheduleDTO(TourSchedule schedule) {
        if (schedule != null) {
            this.scheduleId = schedule.getScheduleId();
            this.dayNumber = schedule.getDayNumber();
            this.location = schedule.getLocation();
            this.transport = schedule.getStransport();
            this.activities = schedule.getActivities();
            this.meal = schedule.getMeal();
            this.arrivalTime = schedule.getArrivalTime();
            this.departureTime = schedule.getDepartureTime();
        }
    }
}
