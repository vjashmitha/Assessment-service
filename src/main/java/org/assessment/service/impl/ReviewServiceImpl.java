package org.assessment.service.impl;

import lombok.RequiredArgsConstructor;
import org.assessment.dto.request.ReviewAssignmentRequest;
import org.assessment.dto.response.ReviewResponse;
import org.assessment.entity.Assignment;
import org.assessment.entity.Review;
import org.assessment.entity.Submission;
import org.assessment.enums.ResultStatus;
import org.assessment.enums.SubmissionStatus;
import org.assessment.exception.ResourceNotFoundException;
import org.assessment.exception.ValidationException;
import org.assessment.mapper.AssignmentReviewMapper;
import org.assessment.repository.AssignmentRepository;
import org.assessment.repository.ReviewRepository;
import org.assessment.repository.SubmissionRepository;
import org.assessment.service.ReviewService;
import org.assessment.util.CommonUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final SubmissionRepository submissionRepository;
    private final AssignmentRepository assignmentRepository;
    private final AssignmentReviewMapper reviewMapper;

     
    @Override
    public ReviewResponse reviewSubmission(ReviewAssignmentRequest request) {

        String reviewerId = CommonUtil.extractUserIdFromRequest();

        Submission submission = submissionRepository.findById(request.getSubmissionId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Submission not found with id: " + request.getSubmissionId()
                ));

        if (submission.getStatus() != SubmissionStatus.SUBMITTED
                && submission.getStatus() != SubmissionStatus.REVIEWED) {
            throw new ValidationException(
                    "Only submitted or reviewed submissions can be reviewed"
            );
        }

        Assignment assignment = assignmentRepository.findById(submission.getAssignmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Assignment not found with id: " + submission.getAssignmentId()
                ));

        if (request.getMarksAwarded() == null) {
            throw new ValidationException("Marks awarded is required");
        }

        if (assignment.getTotalMarks() == null) {
            throw new ValidationException("Assignment total marks is not set");
        }

        if (request.getMarksAwarded() > assignment.getTotalMarks()) {
            throw new ValidationException("Marks awarded cannot be greater than total marks");
        }

        Float passMarks = assignment.getPassMarks() != null ? assignment.getPassMarks() : 0F;

        ResultStatus resultStatus = request.getMarksAwarded() >= passMarks
                ? ResultStatus.PASS
                : ResultStatus.FAIL;

        LocalDateTime now = LocalDateTime.now();

        submission.setMarksAwarded(request.getMarksAwarded());
        submission.setFeedback(request.getFeedback());
        submission.setReviewedBy(reviewerId);
        submission.setReviewedAt(now);
        submission.setResultStatus(resultStatus);
        submission.setStatus(SubmissionStatus.REVIEWED);

        Submission saved = submissionRepository.save(submission);

        return ReviewResponse.builder()
                .submissionId(saved.getSubmissionId())
                .reviewedBy(saved.getReviewedBy())
                .feedback(saved.getFeedback())
                .marksAwarded(saved.getMarksAwarded())
                .resultStatus(saved.getResultStatus())
                .reviewedAt(saved.getReviewedAt() != null ? saved.getReviewedAt().toString() : null)
                .build();
    }

    @Override
    public ReviewResponse getReviewBySubmission(String submissionId) {
        Review review = reviewRepository.findBySubmissionId(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found for submission: " + submissionId));
        return reviewMapper.toResponse(review);
    }

    @Override
    public List<ReviewResponse> getReviewsByReviewer(String reviewerId) {
        return reviewRepository.findByReviewerId(reviewerId).stream()
                .map(reviewMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ReviewResponse updateReview(String reviewId, ReviewAssignmentRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));
        
        Submission submission = submissionRepository.findById(review.getSubmissionId())
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found for this review"));
        
        Assignment assignment = assignmentRepository.findById(submission.getAssignmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found for this review"));

        if (request.getFeedback() != null) {
            review.setFeedback(request.getFeedback());
        }
        if (request.getMarksAwarded() != null) {
            review.setMarksAwarded(request.getMarksAwarded());
            float passMarks = assignment.getPassMarks() != null ? assignment.getPassMarks() : 0f;
            review.setResultStatus(review.getMarksAwarded() >= passMarks ? ResultStatus.PASS : ResultStatus.FAIL);
        }
        review.setReviewedAt(LocalDateTime.now());

        Review savedReview = reviewRepository.save(review);
        return reviewMapper.toResponse(savedReview);
    }
}
