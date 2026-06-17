package org.assessment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.assessment.dto.request.ReviewAssignmentRequest;
import org.assessment.dto.response.ReviewResponse;
import org.assessment.service.ReviewService;
import org.assessment.util.Constants;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(Constants.REVIEW_BASE_URL)
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    public ResponseEntity<ReviewResponse> reviewSubmission(@Valid @RequestBody ReviewAssignmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewService.reviewSubmission(request));
    }

    @GetMapping("/submissions/{submissionId}")
    public ResponseEntity<ReviewResponse> getReviewBySubmission(@PathVariable String submissionId) {
        return ResponseEntity.ok(reviewService.getReviewBySubmission(submissionId));
    }

    @GetMapping("/reviewers/{reviewerId}")
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    public ResponseEntity<List<ReviewResponse>> getReviewsByReviewer(@PathVariable String reviewerId) {
        return ResponseEntity.ok(reviewService.getReviewsByReviewer(reviewerId));
    }

    @PutMapping("/{reviewId}")
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    public ResponseEntity<ReviewResponse> updateReview(
            @PathVariable String reviewId,
            @Valid @RequestBody ReviewAssignmentRequest request) {
        return ResponseEntity.ok(reviewService.updateReview(reviewId, request));
    }
}
