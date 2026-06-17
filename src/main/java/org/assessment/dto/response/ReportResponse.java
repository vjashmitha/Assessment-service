package org.assessment.dto.response;

import lombok.Builder;
import lombok.Data;
import org.assessment.enums.AssignmentStatus;

import java.util.List;

@Data
@Builder
public class ReportResponse {
    private String assignmentId;
    private String assignmentTitle;
    private String dueDate;
    private AssignmentStatus status;
    private Long totalStudents;
    private Long submittedCount;
    private Long pendingCount;
    private Long gradedCount;
    private Float averageScore;
    private Float highestScore;
    private Float lowestScore;
    private Long passCount;
    private Long failCount;
    private List<SubmissionResponse> submissions;
}
