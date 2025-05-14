package vn.edu.iuh.fit.tourmanagement.dto.tourbooking;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class TopTourBookingDTO {
    private String tourName;
    private List<BookingCountByDate> bookingData;

}
