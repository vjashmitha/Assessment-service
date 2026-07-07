package org.assessment.controller;

import org.assessment.dto.request.ReviewAssignmentRequest;
import org.assessment.dto.response.ReviewResponse;
import org.assessment.service.ReviewService;
import org.assessment.util.Constants;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(Constants.REVIEW_BASE_URL)
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @PreAuthorize("hasRole('TRAINER') or hasRole('ADMIN')")
    public ResponseEntity<ReviewResponse> reviewSubmission(
            @Valid @RequestBody ReviewAssignmentRequest request) {

        return ResponseEntity.ok(reviewService.reviewSubmission(request));
    }

    @GetMapping("/submissions/{submissionId}")
    public ResponseEntity<ReviewResponse> getReviewBySubmission(
            @PathVariable String submissionId) {

        return ResponseEntity.ok(reviewService.getReviewBySubmission(submissionId));
    }
}