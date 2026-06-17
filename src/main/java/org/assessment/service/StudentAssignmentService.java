package org.assessment.service;

import org.assessment.dto.response.StudentAssignmentResponse;
import org.assessment.enums.SubmissionStatus;

import java.util.List;

public interface StudentAssignmentService {

    /**
     * Returns all assignments for a student's course(s), each enriched with
     * that student's submission status and review result.
     * Powers the "My Assignments" list page.
     */
    List<StudentAssignmentResponse> getMyAssignments(String studentId);

    /**
     * Returns assignments filtered by submission status tab.
     * Tabs: All | Pending | Submitted | Not Submitted | Reviewed
     */
    List<StudentAssignmentResponse> getMyAssignmentsByStatus(String studentId, SubmissionStatus status);

    /**
     * Returns full detail of one assignment for a student —
     * assignment info + their submission + review result.
     * Powers the assignment detail/submit page.
     */
    StudentAssignmentResponse getAssignmentDetail(String assignmentId, String studentId);
}
