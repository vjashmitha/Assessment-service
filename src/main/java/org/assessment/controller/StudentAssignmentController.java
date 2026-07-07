package org.assessment.controller;

import java.util.List;

import org.assessment.dto.response.StudentAssignmentResponse;
import org.assessment.enums.SubmissionStatus;
import org.assessment.service.StudentAssignmentService;
import org.assessment.util.Constants;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

/**
 * Student-facing assignment endpoints.
 * Powers the "My Assignments" list page and assignment detail/submit page.
 */
@RestController
@RequestMapping(Constants.STUDENT_ASSIGNMENTS_URL)
@RequiredArgsConstructor
public class StudentAssignmentController {

    private final StudentAssignmentService studentAssignmentService;

    @GetMapping
    public ResponseEntity<List<StudentAssignmentResponse>> getMyAssignments(
            @PathVariable String studentId) {
        return ResponseEntity.ok(studentAssignmentService.getMyAssignments(studentId));
    }

    @GetMapping(params = "status")
    public ResponseEntity<List<StudentAssignmentResponse>> getMyAssignmentsByStatus(
            @PathVariable String studentId,
            @RequestParam SubmissionStatus status) {
        return ResponseEntity.ok(studentAssignmentService.getMyAssignmentsByStatus(studentId, status));
    }

    @GetMapping("/{assignmentId}")
    public ResponseEntity<StudentAssignmentResponse> getAssignmentDetail(
            @PathVariable String studentId,
            @PathVariable String assignmentId) {
        return ResponseEntity.ok(studentAssignmentService.getAssignmentDetail(assignmentId, studentId));
    }

    @GetMapping("/{assignmentId}/download")
    public ResponseEntity<String> downloadAssignment(
            @PathVariable String studentId,
            @PathVariable String assignmentId) {
        return ResponseEntity.ok(
                studentAssignmentService.downloadAssignment(assignmentId, studentId));
    }
}
