package org.assessment.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardResponse {

    // --- Instructor dashboard ---
    private Long totalAssignments;
    private Long totalSubmissions;
    private Long pendingReviews;
    private Long gradedSubmissions;

    // --- Student dashboard (My Assignments stat cards) ---
    // Card 1: Total Assignments
    // Card 2: Pending/Overdue
    private Long pendingOverdue;    // assigned but not submitted AND past due date
    // Card 3: Submitted
    // Card 4: Reviewed
    private Long submittedCount;
    private Long reviewedCount;

    // Shared
    private Float averageScore;
    private Long passCount;
    private Long failCount;
}
