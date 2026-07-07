package org.assessment.dto.request;

import lombok.Data;
import org.assessment.enums.AssignmentStatus;
import org.assessment.enums.AssignmentType;
import org.assessment.enums.DifficultyLevel;

import java.time.LocalDate;

@Data
public class UpdateAssignmentRequest {

    private String title;

    private String description;

    private AssignmentType assignmentType;

    private DifficultyLevel difficultyLevel;

    private AssignmentStatus status;

    private Float totalMarks;

    private Float passMarks;

    private LocalDate dueDate;

    private Boolean allowLateSubmission;
    private Boolean allowResubmission;
    private Integer maxAttempts;
    
    private String courseId;
     
    
}
