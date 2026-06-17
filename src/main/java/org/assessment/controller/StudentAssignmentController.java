package org.assessment.controller;

import lombok.RequiredArgsConstructor;
import org.assessment.dto.response.StudentAssignmentResponse;
import org.assessment.enums.SubmissionStatus;
import org.assessment.service.StudentAssignmentService;
import org.assessment.util.Constants;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Student-facing assignment endpoints.
 * Powers the "My Assignments" list page and assignment detail/submit page.
 */
@RestController
@RequestMapping(Constants.STUDENT_ASSIGNMENTS_URL)
@RequiredArgsConstructor
public class StudentAssignmentController {

    private final StudentAssignmentService studentAssignmentService;

    /**
     * GET /api/v1/students/{studentId}/assignments
     * My Assignments list — all assignments with this student's submission status.
     * Used for: All tab
     */
    @GetMapping
    public ResponseEntity<List<StudentAssignmentResponse>> getMyAssignments(
            @PathVariable String studentId) {
        return ResponseEntity.ok(studentAssignmentService.getMyAssignments(studentId));
    }

    /**
     * GET /api/v1/students/{studentId}/assignments?status=SUBMITTED
     * Filter by tab: Pending (NOT_SUBMITTED), Submitted, Reviewed
     * Query param maps to SubmissionStatus enum value.
     */
    @GetMapping(params = "status")
    public ResponseEntity<List<StudentAssignmentResponse>> getMyAssignmentsByStatus(
            @PathVariable String studentId,
            @RequestParam SubmissionStatus status) {
        return ResponseEntity.ok(studentAssignmentService.getMyAssignmentsByStatus(studentId, status));
    }

    /**
     * GET /api/v1/students/{studentId}/assignments/{assignmentId}
     * Assignment detail page — assignment info + submission state + review result.
     */
    @GetMapping("/{assignmentId}")
    public ResponseEntity<StudentAssignmentResponse> getAssignmentDetail(
            @PathVariable String studentId,
            @PathVariable String assignmentId) {
        return ResponseEntity.ok(studentAssignmentService.getAssignmentDetail(assignmentId, studentId));
    }
}
