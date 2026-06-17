package org.assessment.mapper;

import org.assessment.dto.request.ReviewAssignmentRequest;
import org.assessment.dto.response.ReviewResponse;
import org.assessment.entity.Review;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class AssignmentReviewMapper {

    public Review toEntity(ReviewAssignmentRequest request, String reviewerId) {
        if (request == null) {
            return null;
        }
        Review review = new Review();
        review.setReviewId(UUID.randomUUID().toString());
        review.setSubmissionId(request.getSubmissionId());
        review.setReviewerId(reviewerId);
        review.setFeedback(request.getFeedback());
        review.setMarksAwarded(request.getMarksAwarded());
        review.setReviewedAt(LocalDateTime.now());
        return review;
    }

    public ReviewResponse toResponse(Review review) {
        if (review == null) {
            return null;
        }
        return ReviewResponse.builder()
                .id(review.getReviewId())
                .submissionId(review.getSubmissionId())
                .reviewerId(review.getReviewerId())
                .feedback(review.getFeedback())
                .marksAwarded(review.getMarksAwarded())
                .resultStatus(review.getResultStatus())
                .reviewedAt(review.getReviewedAt() != null ? review.getReviewedAt().toString() : null)
                .createdAt(review.getReviewedAt() != null ? review.getReviewedAt().toString() : null)
                .updatedAt(review.getReviewedAt() != null ? review.getReviewedAt().toString() : null)
                .build();
    }
}
