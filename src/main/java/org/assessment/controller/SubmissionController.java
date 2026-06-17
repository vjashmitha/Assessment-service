package org.assessment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.assessment.dto.request.SubmitAssignmentRequest;
import org.assessment.dto.response.SubmissionResponse;
import org.assessment.service.SubmissionService;
import org.assessment.util.Constants;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping(Constants.SUBMISSION_BASE_URL)
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SubmissionResponse> submitAssignment(
            @Valid @RequestPart("request") SubmitAssignmentRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED).body(submissionService.submitAssignment(request, file));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubmissionResponse> getSubmissionById(@PathVariable String id) {
        return ResponseEntity.ok(submissionService.getSubmissionById(id));
    }

    @GetMapping("/assignments/{assignmentId}")
    public ResponseEntity<List<SubmissionResponse>> getSubmissionsByAssignment(@PathVariable String assignmentId) {
        return ResponseEntity.ok(submissionService.getSubmissionsByAssignment(assignmentId));
    }

    @GetMapping("/students/{studentId}")
    public ResponseEntity<List<SubmissionResponse>> getSubmissionsByStudent(@PathVariable String studentId) {
        return ResponseEntity.ok(submissionService.getSubmissionsByStudent(studentId));
    }

    @GetMapping("/assignments/{assignmentId}/students/{studentId}")
    public ResponseEntity<SubmissionResponse> getSubmissionByAssignmentAndStudent(
            @PathVariable String assignmentId,
            @PathVariable String studentId) {
        return ResponseEntity.ok(submissionService.getSubmissionByAssignmentAndStudent(assignmentId, studentId));
    }
}
