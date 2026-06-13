package org.assessment.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SubmitAssignmentRequest {

    @NotBlank
    private String assignmentId;

    private String content;

    private String fileUrl;
}
