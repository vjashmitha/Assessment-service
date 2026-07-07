package org.assessment.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.assessment.client.AuthServiceClient;
import org.assessment.client.EnrollmentServiceClient;
import org.assessment.dto.request.SubmitAssignmentRequest;
import org.assessment.dto.response.SubmissionResponse;
import org.assessment.dto.response.UserSummaryResponse;
import org.assessment.entity.Assignment;
import org.assessment.entity.Review;
import org.assessment.entity.Submission;
import org.assessment.enums.AssignmentStatus;
import org.assessment.enums.AssignmentType;
import org.assessment.enums.SubmissionStatus;
import org.assessment.exception.ResourceNotFoundException;
import org.assessment.exception.ValidationException;
import org.assessment.mapper.SubmissionMapper;
import org.assessment.repository.AssignmentRepository;
import org.assessment.repository.ReviewRepository;
import org.assessment.repository.SubmissionRepository;
import org.assessment.service.SubmissionService;
import org.assessment.storage.S3Service;
import org.assessment.util.CommonUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubmissionServiceImpl implements SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final AssignmentRepository assignmentRepository;
    private final ReviewRepository reviewRepository;
    private final SubmissionMapper submissionMapper;
    private final S3Service s3Service;
    private final EnrollmentServiceClient enrollmentServiceClient;
    private final AuthServiceClient authServiceClient;

    @Override
    public SubmissionResponse submitAssignment(SubmitAssignmentRequest request, MultipartFile file) {
        String studentId = CommonUtil.extractUserIdFromRequest();

        Assignment assignment = assignmentRepository.findById(request.getAssignmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Assignment not found with id: " + request.getAssignmentId()));

        if (assignment.getStatus() != AssignmentStatus.PUBLISHED) {
            throw new ValidationException("Assignment is not open for submissions");
        }

        try {
            Boolean enrolled = enrollmentServiceClient.isEnrolled(
                    Long.valueOf(studentId),
                    Long.valueOf(assignment.getCourseId()));

            if (enrolled == null || !enrolled) {
                throw new ValidationException("Student is not enrolled in course: " + assignment.getCourseId());
            }

        } catch (NumberFormatException e) {
            throw new ValidationException("Invalid student or course ID format");
        } catch (Exception e) {
            log.warn("Failed to check enrollment via Feign", e);
            throw new ValidationException("Failed to validate student enrollment: " + e.getMessage());
        }

        submissionRepository.findByAssignmentIdAndLearnerId(request.getAssignmentId(), studentId)
                .ifPresent(s -> {
                    throw new ValidationException("Already submitted for this assignment");
                });

        Submission submission = submissionMapper.toEntity(request, studentId);

        submission.setAssignmentId(assignment.getAssignmentId());
        submission.setCourseId(assignment.getCourseId());
        submission.setTrainerId(assignment.getTrainerId());

        if (assignment.getAssignmentType() == AssignmentType.FILE_UPLOAD && file != null && !file.isEmpty()) {
            String fileUrl = s3Service.uploadFile(file, "submissions");
            submission.setSubmissionFileUrl(fileUrl);
        }

        Submission saved = submissionRepository.save(submission);

        return enrichLearnerDetails(saved, null);
    }

    @Override
    public SubmissionResponse getSubmissionById(String id) {
        Submission submission = submissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found with id: " + id));

        Review review = reviewRepository.findBySubmissionId(id).orElse(null);

        return enrichLearnerDetails(submission, review);
    }

    @Override
    public List<SubmissionResponse> getSubmissionsByAssignment(String assignmentId) {
        return submissionRepository.findByAssignmentId(assignmentId)
                .stream()
                .map(this::toEnrichedResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<SubmissionResponse> getSubmissionsByStudent(String studentId) {
        return submissionRepository.findByLearnerId(studentId)
                .stream()
                .map(this::toEnrichedResponse)
                .collect(Collectors.toList());
    }

    @Override
    public SubmissionResponse getSubmissionByAssignmentAndStudent(String assignmentId, String studentId) {
        Submission submission = submissionRepository.findByAssignmentIdAndLearnerId(assignmentId, studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found"));

        Review review = reviewRepository.findBySubmissionId(submission.getSubmissionId()).orElse(null);

        return enrichLearnerDetails(submission, review);
    }

    @Override
    public String downloadSubmissionFile(String submissionId) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found with id: " + submissionId));

        if (submission.getSubmissionFileUrl() == null || submission.getSubmissionFileUrl().isBlank()) {
            throw new ResourceNotFoundException("Submission file not found");
        }

        return s3Service.generatePresignedUrl(submission.getSubmissionFileUrl());
    }

    @Override
    public List<SubmissionResponse> getPendingSubmissionsByTrainer(String trainerId) {
        return submissionRepository.findPendingByTrainerId(trainerId)
                .stream()
                .map(this::toEnrichedResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<SubmissionResponse> getSubmissionsByTrainerAndStatus(String trainerId, SubmissionStatus status) {
        return submissionRepository.findByTrainerIdAndStatus(trainerId, status)
                .stream()
                .map(this::toEnrichedResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<SubmissionResponse> getAllSubmissionsByTrainer(String trainerId) {
        return submissionRepository.findByTrainerId(trainerId)
                .stream()
                .map(this::toEnrichedResponse)
                .collect(Collectors.toList());
    }

    private SubmissionResponse toEnrichedResponse(Submission submission) {
        Review review = reviewRepository.findBySubmissionId(submission.getSubmissionId()).orElse(null);
        return enrichLearnerDetails(submission, review);
    }

    private SubmissionResponse enrichLearnerDetails(Submission submission, Review review) {
        SubmissionResponse response = submissionMapper.toResponse(submission, review);

        if (submission.getLearnerId() == null || submission.getLearnerId().isBlank()) {
            return response;
        }

        try {
            UserSummaryResponse learner = authServiceClient.getUserById(submission.getLearnerId());

            if (learner != null) {
                response.setLearnerName(learner.getName());
                response.setLearnerUsername(learner.getUsername());
                response.setLearnerEmail(learner.getEmail());
            }

        } catch (Exception e) {
            log.warn("Failed to fetch learner details. learnerId={}", submission.getLearnerId(), e);
        }

        return response;
    }
}