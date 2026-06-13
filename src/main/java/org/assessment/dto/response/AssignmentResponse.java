package org.assessment.dto.response;

import lombok.Builder;
import lombok.Data;
import org.assessment.enums.AssignmentStatus;
import org.assessment.enums.AssignmentType;
import org.assessment.enums.DifficultyLevel;

import java.time.LocalDateTime;

@Data
@Builder
public class AssignmentResponse {
    private String id;
    private String title;
    private String description;
    private String courseId;
    private String instructorId;
    private AssignmentType assignmentType;
    private DifficultyLevel difficultyLevel;
    private AssignmentStatus status;
    private Double totalMarks;
    private Double passingMarks;
    private LocalDateTime dueDate;
    private Boolean allowLateSubmission;
    private String attachmentUrl;
    private String createdAt;
    private String updatedAt;
}
