package org.assessment.controller;

import java.util.List;

import org.assessment.dto.request.SubmitAssignmentRequest;
import org.assessment.dto.response.SubmissionResponse;
import org.assessment.enums.SubmissionStatus;
import org.assessment.service.SubmissionService;
import org.assessment.util.CommonUtil;
import org.assessment.util.Constants;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

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

	@GetMapping("/learners/{learnerId}")
	public ResponseEntity<List<SubmissionResponse>> getSubmissionsByLearner(@PathVariable String learnerId) {

		return ResponseEntity.ok(submissionService.getSubmissionsByStudent(learnerId));
	}

	@GetMapping("/assignments/{assignmentId}/learners/{learnerId}")
	public ResponseEntity<SubmissionResponse> getSubmissionByAssignmentAndLearner(@PathVariable String assignmentId,
			@PathVariable String learnerId) {

		return ResponseEntity.ok(submissionService.getSubmissionByAssignmentAndStudent(assignmentId, learnerId));
	}
	@GetMapping("/{id}/download")
	public ResponseEntity<String> downloadSubmissionFile(@PathVariable String id) {
	    return ResponseEntity.ok(submissionService.downloadSubmissionFile(id));
	}
	@GetMapping("/trainers/{trainerId}/pending")
	public ResponseEntity<List<SubmissionResponse>> getPendingSubmissionsByTrainer(
	        @PathVariable String trainerId) {

	    return ResponseEntity.ok(submissionService.getPendingSubmissionsByTrainer(trainerId));
	}

	@GetMapping("/trainers/{trainerId}/reviewing")
	public ResponseEntity<List<SubmissionResponse>> getReviewingSubmissionsByTrainer(
	        @PathVariable String trainerId) {

	    return ResponseEntity.ok(submissionService.getSubmissionsByTrainerAndStatus(
	            trainerId, SubmissionStatus.UNDER_REVIEW));
	}

	@GetMapping("/trainers/{trainerId}/reviewed")
	public ResponseEntity<List<SubmissionResponse>> getReviewedSubmissionsByTrainer(
	        @PathVariable String trainerId) {

	    return ResponseEntity.ok(submissionService.getSubmissionsByTrainerAndStatus(
	            trainerId, SubmissionStatus.REVIEWED));
	}

	@GetMapping("/trainers/{trainerId}/rejected")
	public ResponseEntity<List<SubmissionResponse>> getRejectedSubmissionsByTrainer(
	        @PathVariable String trainerId) {

	    return ResponseEntity.ok(submissionService.getSubmissionsByTrainerAndStatus(
	            trainerId, SubmissionStatus.REJECTED));
	}
	@GetMapping("/trainers/{trainerId}")
	public ResponseEntity<List<SubmissionResponse>> getAllSubmissionsByTrainer(
	        @PathVariable String trainerId) {

	    return ResponseEntity.ok(
	            submissionService.getAllSubmissionsByTrainer(trainerId));
	}
	 
}