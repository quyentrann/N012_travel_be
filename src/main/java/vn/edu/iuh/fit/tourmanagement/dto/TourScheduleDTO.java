package vn.edu.iuh.fit.tourmanagement.dto;

import lombok.*;
import vn.edu.iuh.fit.tourmanagement.models.TourSchedule;

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

    public TourScheduleDTO(TourSchedule schedule) {
        if (schedule != null) {
            this.scheduleId = schedule.getScheduleId();
            this.dayNumber = schedule.getDayNumber();
            this.location = schedule.getLocation();
            this.transport = schedule.getStransport();
            this.activities = schedule.getActivities();
        }
    }
}
