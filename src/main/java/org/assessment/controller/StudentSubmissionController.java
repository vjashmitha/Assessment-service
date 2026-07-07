package org.assessment.controller;

import org.assessment.dto.response.SubmissionResponse;
import org.assessment.service.StudentSubmissionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/students/{studentId}/assignments/{assignmentId}")
@RequiredArgsConstructor
public class StudentSubmissionController {

	private final StudentSubmissionService studentSubmissionService;

	@PostMapping(value = "/submit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<SubmissionResponse> submitAssignment(@PathVariable String studentId,
			@PathVariable String assignmentId, @RequestPart(value = "file", required = false) MultipartFile file) {

		return ResponseEntity.status(HttpStatus.CREATED)
				.body(studentSubmissionService.submitAssignment(studentId, assignmentId, file));
	}

	@PutMapping(value = "/submit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<SubmissionResponse> resubmitAssignment(@PathVariable String studentId,
			@PathVariable String assignmentId, @RequestPart(value = "file", required = false) MultipartFile file) {

		return ResponseEntity.ok(studentSubmissionService.resubmitAssignment(studentId, assignmentId, file));
	}

	// @GetMapping("/download")
	public ResponseEntity<String> downloadAssignmentFile(@PathVariable String studentId,
			@PathVariable String assignmentId) {

		return ResponseEntity.ok(studentSubmissionService.getAssignmentDownloadUrl(studentId, assignmentId));
	}

	@GetMapping("/submission")
	public ResponseEntity<SubmissionResponse> getMySubmission(@PathVariable String studentId,
			@PathVariable String assignmentId) {

		return ResponseEntity.ok(studentSubmissionService.getMySubmission(studentId, assignmentId));
	}

	@DeleteMapping("/submission")
	public ResponseEntity<Void> deleteSubmission(@PathVariable String studentId, @PathVariable String assignmentId) {

		studentSubmissionService.deleteSubmission(studentId, assignmentId);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/submission/download")
	public ResponseEntity<String> downloadMySubmission(@PathVariable String studentId,
			@PathVariable String assignmentId) {

		return ResponseEntity.ok(studentSubmissionService.downloadSubmission(studentId, assignmentId));
	}

}