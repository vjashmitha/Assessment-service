package org.assessment.mapper;

import org.assessment.dto.request.ReviewAssignmentRequest;
import org.assessment.dto.response.ReviewResponse;
import org.assessment.entity.AssignmentReview;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AssignmentReviewMapper {

    public AssignmentReview toEntity(ReviewAssignmentRequest request, String reviewerId) {
        AssignmentReview review = new AssignmentReview();
        review.setId(UUID.randomUUID().toString());
        review.setSubmissionId(request.getSubmissionId());
        review.setReviewerId(reviewerId);
        review.setFeedback(request.getFeedback());
        review.setMarksAwarded(request.getMarksAwarded());
        return review;
    }

    public ReviewResponse toResponse(AssignmentReview review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .submissionId(review.getSubmissionId())
                .reviewerId(review.getReviewerId())
                .feedback(review.getFeedback())
                .marksAwarded(review.getMarksAwarded())
                .resultStatus(review.getResultStatusEnum())
                .reviewedAt(review.getReviewedAt())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}
