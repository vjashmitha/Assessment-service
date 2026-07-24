package org.assessment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assessment.dto.request.ReviewAssignmentRequest;
import org.assessment.dto.response.ReviewResponse;
import org.assessment.enums.ResultStatus;
import org.assessment.exception.GlobalExceptionHandler;
import org.assessment.exception.ResourceNotFoundException;
import org.assessment.exception.ValidationException;
import org.assessment.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewController Tests")
class ReviewControllerTest {

    @Mock
    private ReviewService reviewService;

    @InjectMocks
    private ReviewController reviewController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private ReviewResponse sampleResponse;

    @BeforeEach
    void setUp() {
        // Standalone setup bypasses Spring Security — @PreAuthorize is not enforced here.
        // Security layer is tested separately in HeaderAuthFilterTest.
        mockMvc = MockMvcBuilders.standaloneSetup(reviewController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();

        sampleResponse = ReviewResponse.builder()
                .id("rev-001")
                .submissionId("sub-001")
                .reviewerId("instructor-1")
                .marksAwarded(80f)
                .feedback("Good work")
                .resultStatus(ResultStatus.PASS)
                .build();
    }

    // -------------------------------------------------------------------------
    // POST /api/v1/reviews
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("POST /api/v1/reviews")
    class ReviewSubmission {

        @Test
        @DisplayName("should return 201 with review response")
        void review_success_returns201() throws Exception {
            ReviewAssignmentRequest request = new ReviewAssignmentRequest();
            request.setSubmissionId("sub-001");
            request.setMarksAwarded(80f);
            request.setFeedback("Good work");

            when(reviewService.reviewSubmission(any())).thenReturn(sampleResponse);

            mockMvc.perform(post("/api/v1/reviews")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value("rev-001"))
                    .andExpect(jsonPath("$.marksAwarded").value(80.0))
                    .andExpect(jsonPath("$.resultStatus").value("PASS"));
        }

        @Test
        @DisplayName("should return 400 when submission already reviewed")
        void review_alreadyReviewed_returns400() throws Exception {
            ReviewAssignmentRequest request = new ReviewAssignmentRequest();
            request.setSubmissionId("sub-001");
            request.setMarksAwarded(80f);

            when(reviewService.reviewSubmission(any()))
                    .thenThrow(new ValidationException("Submission already reviewed"));

            mockMvc.perform(post("/api/v1/reviews")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Submission already reviewed"));
        }

        @Test
        @DisplayName("should return 404 when submission not found")
        void review_submissionNotFound_returns404() throws Exception {
            ReviewAssignmentRequest request = new ReviewAssignmentRequest();
            request.setSubmissionId("missing");
            request.setMarksAwarded(80f);

            when(reviewService.reviewSubmission(any()))
                    .thenThrow(new ResourceNotFoundException("Submission not found with id: missing"));

            mockMvc.perform(post("/api/v1/reviews")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/reviews/submissions/{submissionId}
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("GET /api/v1/reviews/submissions/{submissionId}")
    class GetReviewBySubmission {

        @Test
        @DisplayName("should return 200 with review")
        void getBySubmission_found() throws Exception {
            when(reviewService.getReviewBySubmission("sub-001")).thenReturn(sampleResponse);

            mockMvc.perform(get("/api/v1/reviews/submissions/sub-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value("rev-001"))
                    .andExpect(jsonPath("$.submissionId").value("sub-001"));
        }

        @Test
        @DisplayName("should return 404 when review not found")
        void getBySubmission_notFound() throws Exception {
            when(reviewService.getReviewBySubmission("missing"))
                    .thenThrow(new ResourceNotFoundException("Review not found for submission: missing"));

            mockMvc.perform(get("/api/v1/reviews/submissions/missing"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(containsString("Review not found")));
        }
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/reviews/reviewers/{reviewerId}
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("GET /api/v1/reviews/reviewers/{reviewerId}")
    class GetByReviewer {

        @Test
        @DisplayName("should return 200 with list of reviews")
        void getByReviewer_returnsList() throws Exception {
            when(reviewService.getReviewsByReviewer("instructor-1"))
                    .thenReturn(List.of(sampleResponse));

            mockMvc.perform(get("/api/v1/reviews/reviewers/instructor-1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].reviewerId").value("instructor-1"));
        }

        @Test
        @DisplayName("should return 200 with empty list when reviewer has no reviews")
        void getByReviewer_empty() throws Exception {
            when(reviewService.getReviewsByReviewer("instructor-1")).thenReturn(List.of());

            mockMvc.perform(get("/api/v1/reviews/reviewers/instructor-1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }

    // -------------------------------------------------------------------------
    // PUT /api/v1/reviews/{reviewId}
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("PUT /api/v1/reviews/{reviewId}")
    class UpdateReview {

        @Test
        @DisplayName("should return 200 with updated review")
        void update_success() throws Exception {
            ReviewAssignmentRequest request = new ReviewAssignmentRequest();
            request.setSubmissionId("sub-001");
            request.setMarksAwarded(90f);
            request.setFeedback("Excellent work");

            ReviewResponse updated = ReviewResponse.builder()
                    .id("rev-001").submissionId("sub-001").marksAwarded(90f)
                    .feedback("Excellent work").resultStatus(ResultStatus.PASS).build();

            when(reviewService.updateReview(eq("rev-001"), any())).thenReturn(updated);

            mockMvc.perform(put("/api/v1/reviews/rev-001")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.marksAwarded").value(90.0))
                    .andExpect(jsonPath("$.feedback").value("Excellent work"));
        }

        @Test
        @DisplayName("should return 404 when review not found")
        void update_notFound() throws Exception {
            ReviewAssignmentRequest request = new ReviewAssignmentRequest();
            request.setSubmissionId("sub-001");
            request.setMarksAwarded(80f);

            when(reviewService.updateReview(eq("missing"), any()))
                    .thenThrow(new ResourceNotFoundException("Review not found with id: missing"));

            mockMvc.perform(put("/api/v1/reviews/missing")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }
}
