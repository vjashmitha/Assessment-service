package org.assessment.service.impl;

import lombok.RequiredArgsConstructor;
import org.assessment.dto.request.ReviewAssignmentRequest;
import org.assessment.dto.response.ReviewResponse;
import org.assessment.entity.AssignmentReview;
import org.assessment.entity.Submission;
import org.assessment.enums.SubmissionStatus;
import org.assessment.exception.ResourceNotFoundException;
import org.assessment.exception.ValidationException;
import org.assessment.mapper.AssignmentReviewMapper;
import org.assessment.repository.AssignmentReviewRepository;
import org.assessment.repository.SubmissionRepository;
import org.assessment.service.ReviewService;
import org.assessment.util.CommonUtil;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final AssignmentReviewRepository reviewRepository;
    private final SubmissionRepository submissionRepository;
    private final AssignmentReviewMapper reviewMapper;

    @Override
    public ReviewResponse reviewSubmission(ReviewAssignmentRequest request) {
        String reviewerId = CommonUtil.extractUserIdFromRequest();

        Submission submission = submissionRepository.findById(request.getSubmissionId())
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found"));

        reviewRepository.findBySubmissionId(request.getSubmissionId())
                .ifPresent(r -> { throw new ValidationException("Submission already reviewed"); });

        AssignmentReview review = reviewMapper.toEntity(request, reviewerId);
        review = reviewRepository.save(review);

        submission.setStatusEnum(SubmissionStatus.GRADED);
        submission.setObtainedMarks(request.getMarksAwarded());
        submissionRepository.save(submission);

        return reviewMapper.toResponse(review);
    }

    @Override
    public ReviewResponse getReviewBySubmission(String submissionId) {
        return reviewMapper.toResponse(
                reviewRepository.findBySubmissionId(submissionId)
                        .orElseThrow(() -> new ResourceNotFoundException("Review not found for submission: " + submissionId))
        );
    }

    @Override
    public List<ReviewResponse> getReviewsByReviewer(String reviewerId) {
        return reviewRepository.findByReviewerId(reviewerId)
                .stream().map(reviewMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    public ReviewResponse updateReview(String reviewId, ReviewAssignmentRequest request) {
        AssignmentReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));
        review.setFeedback(request.getFeedback());
        review.setMarksAwarded(request.getMarksAwarded());
        return reviewMapper.toResponse(reviewRepository.save(review));
    }
}
