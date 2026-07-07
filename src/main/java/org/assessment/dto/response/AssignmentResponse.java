package org.assessment.dto.response;

import lombok.Builder;
import lombok.Data;
import org.assessment.enums.AssignmentStatus;
import org.assessment.enums.AssignmentType;
import org.assessment.enums.DifficultyLevel;

@Data
@Builder
public class AssignmentResponse {

    private String assignmentId;
    private String title;
    private String description;
    private String courseId;
    private String courseName;
    private Float totalMarks;
    private Float passMarks;
    private AssignmentType assignmentType;
    private DifficultyLevel difficultyLevel;
    private AssignmentStatus status;
    private String dueDate;
    private String assignmentFileUrl;
    private String createdBy;
    private String createdAt;
    private String updatedAt;
    private String submissionId;
    private String reviewedBy;
}
