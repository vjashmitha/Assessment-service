package org.assessment.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.assessment.dto.response.SubmissionResponse;
import org.assessment.entity.Assignment;
import org.assessment.entity.Submission;
import org.assessment.enums.AssignmentStatus;
import org.assessment.enums.ResultStatus;
import org.assessment.enums.SubmissionStatus;
import org.assessment.exception.ResourceNotFoundException;
import org.assessment.exception.ValidationException;
import org.assessment.repository.AssignmentRepository;
import org.assessment.repository.SubmissionRepository;
import org.assessment.service.StudentSubmissionService;
import org.assessment.storage.S3Service;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentSubmissionServiceImpl implements StudentSubmissionService {

	private final AssignmentRepository assignmentRepository;
	private final SubmissionRepository submissionRepository;
	private final S3Service s3Service;

	@Override
	public SubmissionResponse submitAssignment(String studentId, String assignmentId, MultipartFile file) {

		Assignment assignment = getAssignmentOrThrow(assignmentId);
		validateAssignmentOpen(assignment);

		Optional<Submission> existingSubmission = submissionRepository.findByAssignmentIdAndLearnerId(assignmentId,
				studentId);

		if (existingSubmission.isPresent()) {
			throw new ValidationException("Assignment already submitted. Use resubmit API if resubmission is allowed.");
		}

		Submission submission = new Submission();
		submission.setSubmissionId(UUID.randomUUID().toString());
		submission.setAssignmentId(assignmentId);
		submission.setLearnerId(studentId);
		submission.setCourseId(assignment.getCourseId());
		submission.setTrainerId(resolveTrainerId(assignment));
		submission.setStatus(SubmissionStatus.SUBMITTED);
		submission.setResultStatus(ResultStatus.PENDING);
		submission.setSubmittedAt(LocalDateTime.now());
		submission.setAttemptNumber(1);

		if (file != null && !file.isEmpty()) {
			String fileUrl = s3Service.uploadFile(file, "submissions");
			submission.setSubmissionFileUrl(fileUrl);
		}

		Submission saved = submissionRepository.save(submission);

		log.info("Assignment submitted successfully. assignmentId={}, studentId={}, submissionId={}", assignmentId,
				studentId, saved.getSubmissionId());

		return toResponse(saved);
	}

	@Override
	public SubmissionResponse resubmitAssignment(String studentId, String assignmentId, MultipartFile file) {

		Assignment assignment = getAssignmentOrThrow(assignmentId);
		validateAssignmentOpen(assignment);

		Submission existingSubmission = submissionRepository.findByAssignmentIdAndLearnerId(assignmentId, studentId)
				.orElseThrow(() -> new ResourceNotFoundException("No existing submission found for resubmission"));

		if (existingSubmission.getStatus() == SubmissionStatus.REVIEWED) {
			throw new ValidationException("Reviewed submission cannot be resubmitted");
		}

		if (!Boolean.TRUE.equals(assignment.getAllowResubmission())) {
			throw new ValidationException("Resubmission is not allowed for this assignment");
		}

		int currentAttempt = existingSubmission.getAttemptNumber() != null ? existingSubmission.getAttemptNumber() : 1;

		int maxAttempts = assignment.getMaxAttempts() != null ? assignment.getMaxAttempts() : 1;

		if (currentAttempt >= maxAttempts) {
			throw new ValidationException("Maximum submission attempts exceeded");
		}

		if (file != null && !file.isEmpty()) {
			if (existingSubmission.getSubmissionFileUrl() != null) {
				s3Service.deleteFile(existingSubmission.getSubmissionFileUrl());
			}

			String fileUrl = s3Service.uploadFile(file, "submissions");
			existingSubmission.setSubmissionFileUrl(fileUrl);
		}

		existingSubmission.setAttemptNumber(currentAttempt + 1);
		existingSubmission.setSubmittedAt(LocalDateTime.now());
		existingSubmission.setStatus(SubmissionStatus.SUBMITTED);
		existingSubmission.setResultStatus(ResultStatus.PENDING);
		existingSubmission.setMarksAwarded(null);
		existingSubmission.setFeedback(null);
		existingSubmission.setReviewedBy(null);
		existingSubmission.setReviewedAt(null);

		Submission saved = submissionRepository.save(existingSubmission);

		log.info("Assignment resubmitted successfully. assignmentId={}, studentId={}, submissionId={}, attempt={}",
				assignmentId, studentId, saved.getSubmissionId(), saved.getAttemptNumber());

		return toResponse(saved);
	}

	@Override
	public String getAssignmentDownloadUrl(String studentId, String assignmentId) {

		Assignment assignment = getAssignmentOrThrow(assignmentId);

		if (assignment.getAssignmentFileUrl() == null || assignment.getAssignmentFileUrl().isBlank()) {
			throw new ResourceNotFoundException("Assignment file not available");
		}

		return assignment.getAssignmentFileUrl();
	}

	@Override
	public SubmissionResponse getMySubmission(String studentId, String assignmentId) {

		Submission submission = submissionRepository.findByAssignmentIdAndLearnerId(assignmentId, studentId)
				.orElseThrow(() -> new ResourceNotFoundException("Submission not found"));

		return toResponse(submission);
	}

	@Override
	public void deleteSubmission(String studentId, String assignmentId) {

		Assignment assignment = getAssignmentOrThrow(assignmentId);

		Submission submission = submissionRepository.findByAssignmentIdAndLearnerId(assignmentId, studentId)
				.orElseThrow(() -> new ResourceNotFoundException("Submission not found"));

		if (submission.getStatus() == SubmissionStatus.REVIEWED) {
			throw new ValidationException("Reviewed submission cannot be deleted");
		}

		if (isDueDatePassed(assignment)) {
			throw new ValidationException("Submission cannot be deleted after due date");
		}

		if (submission.getSubmissionFileUrl() != null) {
			s3Service.deleteFile(submission.getSubmissionFileUrl());
		}

		submissionRepository.deleteById(submission.getSubmissionId());

		log.info("Submission deleted successfully. assignmentId={}, studentId={}, submissionId={}", assignmentId,
				studentId, submission.getSubmissionId());
	}

	private Assignment getAssignmentOrThrow(String assignmentId) {
		return assignmentRepository.findById(assignmentId)
				.orElseThrow(() -> new ResourceNotFoundException("Assignment not found: " + assignmentId));
	}

	private void validateAssignmentOpen(Assignment assignment) {

		if (assignment.getStatus() != AssignmentStatus.PUBLISHED) {
			throw new ValidationException("Assignment is not open for submissions");
		}

		if (isDueDatePassed(assignment)) {
			throw new ValidationException("Assignment due date has passed");
		}
	}

	private boolean isDueDatePassed(Assignment assignment) {
		if (assignment.getDueDate() == null || assignment.getDueDate().isBlank()) {
			return false;
		}

		return LocalDate.parse(assignment.getDueDate()).isBefore(LocalDate.now());
	}

	private String resolveTrainerId(Assignment assignment) {
		if (assignment.getTrainerId() != null && !assignment.getTrainerId().isBlank()) {
			return assignment.getTrainerId();
		}
		return assignment.getCreatedBy();
	}

	private SubmissionResponse toResponse(Submission submission) {
		return SubmissionResponse.builder().submissionId(submission.getSubmissionId())
				.assignmentId(submission.getAssignmentId()).courseId(submission.getCourseId())
				.learnerId(submission.getLearnerId()).learnerName(submission.getLearnerName())
				.trainerId(submission.getTrainerId()).submissionFileUrl(submission.getSubmissionFileUrl())
				.status(submission.getStatus()).resultStatus(submission.getResultStatus())
				.marksAwarded(submission.getMarksAwarded()).feedback(submission.getFeedback())
				.submittedAt(submission.getSubmittedAt() != null ? submission.getSubmittedAt().toString() : null)
				.reviewedBy(submission.getReviewedBy())
				.reviewedAt(submission.getReviewedAt() != null ? submission.getReviewedAt().toString() : null)
				.attemptNumber(submission.getAttemptNumber()).build();
	}
	
	
	@Override
	public String downloadSubmission(String studentId, String assignmentId) {

	    Submission submission = submissionRepository
	            .findByAssignmentIdAndLearnerId(assignmentId, studentId)
	            .orElseThrow(() ->
	                    new ResourceNotFoundException("Submission not found"));

	    return s3Service.generatePresignedUrl(
	            submission.getSubmissionFileUrl());
	}
	
}