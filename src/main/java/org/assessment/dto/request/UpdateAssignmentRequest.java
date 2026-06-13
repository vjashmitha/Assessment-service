package org.assessment.dto.request;

import lombok.Data;
import org.assessment.enums.AssignmentStatus;
import org.assessment.enums.AssignmentType;
import org.assessment.enums.DifficultyLevel;

import java.time.LocalDateTime;

@Data
public class UpdateAssignmentRequest {

    private String title;

    private String description;

    private AssignmentType assignmentType;

    private DifficultyLevel difficultyLevel;

    private AssignmentStatus status;

    private Double totalMarks;

    private Double passingMarks;

    private LocalDateTime dueDate;

    private Boolean allowLateSubmission;
}
