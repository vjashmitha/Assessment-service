package org.assessment.dto.response;

import lombok.Builder;
import lombok.Data;
import org.assessment.enums.ResultStatus;
import org.assessment.enums.SubmissionStatus;

@Data
@Builder
public class SubmissionResponse {
    private String id;
    private String assignmentId;
    private String studentId;
    private String content;
    private String fileUrl;
    private SubmissionStatus status;
    private ResultStatus resultStatus;
    private Double obtainedMarks;
    private String submittedAt;
    private String createdAt;
    private String updatedAt;
}
