package org.assessment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.assessment.enums.AssignmentType;
import org.assessment.enums.DifficultyLevel;

import java.time.LocalDateTime;

@Data
public class CreateAssignmentRequest {

    @NotBlank
    private String title;

    private String description;

    @NotBlank
    private String courseId;

    @NotNull
    private AssignmentType assignmentType;

    @NotNull
    private DifficultyLevel difficultyLevel;

    @NotNull
    private Double totalMarks;

    private Double passingMarks;

    private LocalDateTime dueDate;

    private Boolean allowLateSubmission = false;
}
