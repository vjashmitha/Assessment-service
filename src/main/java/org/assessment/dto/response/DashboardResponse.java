package org.assessment.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardResponse {
    private Long totalAssignments;
    private Long totalSubmissions;
    private Long pendingReviews;
    private Long gradedSubmissions;
    private Double averageScore;
    private Long passCount;
    private Long failCount;
}
