package org.assessment.dto.response;

import lombok.Builder;
import lombok.Data;
import org.assessment.enums.ResultStatus;

@Data
@Builder
public class ReviewResponse {
    private String id;
    private String submissionId;
    private String reviewerId;
    private String feedback;
    private Double marksAwarded;
    private ResultStatus resultStatus;
    private String reviewedAt;
    private String createdAt;
    private String updatedAt;
}
