package org.assessment.dto.response;

import lombok.Builder;
import lombok.Data;
import org.assessment.enums.AssignmentStatus;
import org.assessment.enums.DifficultyLevel;
import org.assessment.enums.ResultStatus;
import org.assessment.enums.SubmissionStatus;

/**
 * Used for the student "My Assignments" page.
 * Combines assignment details + this student's submission status in one response.
 * Maps to the list view: Assignment Name, Due Date, Total Marks, Pass Marks, Level, Status, Actions
 * And the detail view: title, description, file, submission state, review result.
 */
@Data
@Builder
public class StudentAssignmentResponse {

    // Assignment info
    private String assignmentId;
    private String title;
    private String description;
    private String courseId;
    private String courseName;
    private Float totalMarks;
    private Float passMarks;
    private DifficultyLevel difficultyLevel;
    private AssignmentStatus assignmentStatus;  // DRAFT / PUBLISHED / CLOSED
    private String dueDate;
    private String createdAt;
    private boolean overdue;                    // true if dueDate is past and not submitted

    // Assignment file (for download)
    private String assignmentFileUrl;
    private String assignmentFileName;          // just the filename for display e.g. "Assignment_REST_API.pdf"

    // This student's submission state
    private String submissionId;
    private SubmissionStatus submissionStatus;  // NOT_SUBMITTED / SUBMITTED / UNDER_REVIEW / REVIEWED
    private String submittedAt;
    private String submissionFileUrl;
    private String submissionFileName;          // filename of what student uploaded

    // Review / grading result (populated after instructor reviews)
    private Float marksAwarded;
    private Float scorePercentage;             // e.g. 88.0 (shown as "88%" in UI)
    private ResultStatus resultStatus;          // PASS / FAIL / PENDING
    private String feedback;                    // instructor's written feedback
    private String reviewedAt;
}
