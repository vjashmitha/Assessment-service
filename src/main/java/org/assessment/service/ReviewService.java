package org.assessment.service;

import org.assessment.dto.request.ReviewAssignmentRequest;
import org.assessment.dto.response.ReviewResponse;

import java.util.List;

public interface ReviewService {
    ReviewResponse reviewSubmission(ReviewAssignmentRequest request);
    ReviewResponse getReviewBySubmission(String submissionId);
    List<ReviewResponse> getReviewsByReviewer(String reviewerId);
    ReviewResponse updateReview(String reviewId, ReviewAssignmentRequest request);
}
