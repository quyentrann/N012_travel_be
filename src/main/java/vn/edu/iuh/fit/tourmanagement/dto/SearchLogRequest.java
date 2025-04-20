package vn.edu.iuh.fit.tourmanagement.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SearchLogRequest {
    private String query;
    private Long tourId;
}
