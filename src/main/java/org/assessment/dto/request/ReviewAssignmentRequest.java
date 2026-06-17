package org.assessment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviewAssignmentRequest {

    @NotBlank
    private String submissionId;

    private String feedback;

    @NotNull
    private Float marksAwarded;
}
