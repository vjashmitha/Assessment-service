package org.assessment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.assessment.enums.AssignmentType;
import org.assessment.enums.DifficultyLevel;

import java.time.LocalDate;

@Data
public class CreateAssignmentRequest {

    @NotBlank
    private String title;

    private String description;

    @NotBlank
    private String courseId;

    @NotNull
    private Float totalMarks;

    @NotNull
    private Float passMarks;

    private AssignmentType assignmentType;

    private DifficultyLevel difficultyLevel;

    private LocalDate dueDate;
}
