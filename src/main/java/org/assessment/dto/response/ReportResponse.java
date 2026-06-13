package org.assessment.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ReportResponse {
    private Long assignmentId;
    private String assignmentTitle;
    private Long totalStudents;
    private Long submittedCount;
    private Long pendingCount;
    private Long gradedCount;
    private Double averageScore;
    private Double highestScore;
    private Double lowestScore;
    private Long passCount;
    private Long failCount;
    private List<SubmissionResponse> submissions;
}
