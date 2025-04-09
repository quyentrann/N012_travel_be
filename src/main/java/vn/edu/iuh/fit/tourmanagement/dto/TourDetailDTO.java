package vn.edu.iuh.fit.tourmanagement.dto;

import lombok.*;
import vn.edu.iuh.fit.tourmanagement.models.TourDetail;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TourDetailDTO {
    private Long detailId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String includedServices;
    private String excludedServices;

    public TourDetailDTO(TourDetail detail) {
        if (detail != null) {
            this.detailId = detail.getDetailId();
            this.startDate = detail.getStartDate();
            this.endDate = detail.getEndDate();
            this.includedServices = detail.getIncludedServices();
            this.excludedServices = detail.getExcludedServices();
        }
    }
}
