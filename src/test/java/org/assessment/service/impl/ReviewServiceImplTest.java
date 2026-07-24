package org.assessment.service.impl;

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
import org.assessment.util.CommonUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewServiceImpl Tests")
class ReviewServiceImplTest {

    @Mock private ReviewRepository reviewRepository;
    @Mock private SubmissionRepository submissionRepository;
    @Mock private AssignmentRepository assignmentRepository;
    @Mock private AssignmentReviewMapper reviewMapper;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private static final String REVIEWER_ID   = "instructor-1";
    private static final String SUBMISSION_ID  = "sub-001";
    private static final String ASSIGNMENT_ID  = "assign-001";
    private static final String REVIEW_ID      = "rev-001";

    private Submission sampleSubmission;
    private Assignment sampleAssignment;
    private Review     sampleReview;
    private ReviewAssignmentRequest reviewRequest;

    @BeforeEach
    void setUp() {
        sampleSubmission = Submission.builder()
                .submissionId(SUBMISSION_ID)
                .assignmentId(ASSIGNMENT_ID)
                .learnerId("student-1")
                .status(SubmissionStatus.SUBMITTED)
                .build();

        sampleAssignment = Assignment.builder()
                .assignmentId(ASSIGNMENT_ID)
                .passMarks(50f)
                .totalMarks(100f)
                .build();

        sampleReview = Review.builder()
                .reviewId(REVIEW_ID)
                .submissionId(SUBMISSION_ID)
                .reviewerId(REVIEWER_ID)
                .marksAwarded(75f)
                .feedback("Good work")
                .reviewedAt(LocalDateTime.now())
                .build();

        reviewRequest = new ReviewAssignmentRequest();
        reviewRequest.setSubmissionId(SUBMISSION_ID);
        reviewRequest.setMarksAwarded(75f);
        reviewRequest.setFeedback("Good work");
    }

    // -------------------------------------------------------------------------
    // reviewSubmission
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("reviewSubmission")
    class ReviewSubmission {

        @Test
        @DisplayName("should set PASS when marks awarded >= pass marks")
        void reviewSubmission_pass() {
            ReviewResponse expectedResponse = ReviewResponse.builder()
                    .id(REVIEW_ID).resultStatus(ResultStatus.PASS).marksAwarded(75f).build();

            try (MockedStatic<CommonUtil> util = mockStatic(CommonUtil.class)) {
                util.when(CommonUtil::extractUserIdFromRequest).thenReturn(REVIEWER_ID);
                when(submissionRepository.findById(SUBMISSION_ID)).thenReturn(Optional.of(sampleSubmission));
                when(reviewRepository.findBySubmissionId(SUBMISSION_ID)).thenReturn(Optional.empty());
                when(assignmentRepository.findById(ASSIGNMENT_ID)).thenReturn(Optional.of(sampleAssignment));
                when(reviewMapper.toEntity(reviewRequest, REVIEWER_ID)).thenReturn(sampleReview);
                when(reviewRepository.save(sampleReview)).thenReturn(sampleReview);
                when(submissionRepository.save(sampleSubmission)).thenReturn(sampleSubmission);
                when(reviewMapper.toResponse(sampleReview)).thenReturn(expectedResponse);

                ReviewResponse result = reviewService.reviewSubmission(reviewRequest);

                assertThat(result).isNotNull();
                assertThat(sampleReview.getResultStatus()).isEqualTo(ResultStatus.PASS);
                assertThat(sampleSubmission.getStatus()).isEqualTo(SubmissionStatus.REVIEWED);
                verify(reviewRepository).save(sampleReview);
                verify(submissionRepository).save(sampleSubmission);
            }
        }

        @Test
        @DisplayName("should set FAIL when marks awarded < pass marks")
        void reviewSubmission_fail() {
            sampleReview.setMarksAwarded(30f);  // below pass marks of 50
            reviewRequest.setMarksAwarded(30f);

            ReviewResponse expectedResponse = ReviewResponse.builder()
                    .id(REVIEW_ID).resultStatus(ResultStatus.FAIL).marksAwarded(30f).build();

            try (MockedStatic<CommonUtil> util = mockStatic(CommonUtil.class)) {
                util.when(CommonUtil::extractUserIdFromRequest).thenReturn(REVIEWER_ID);
                when(submissionRepository.findById(SUBMISSION_ID)).thenReturn(Optional.of(sampleSubmission));
                when(reviewRepository.findBySubmissionId(SUBMISSION_ID)).thenReturn(Optional.empty());
                when(assignmentRepository.findById(ASSIGNMENT_ID)).thenReturn(Optional.of(sampleAssignment));
                when(reviewMapper.toEntity(reviewRequest, REVIEWER_ID)).thenReturn(sampleReview);
                when(reviewRepository.save(sampleReview)).thenReturn(sampleReview);
                when(submissionRepository.save(sampleSubmission)).thenReturn(sampleSubmission);
                when(reviewMapper.toResponse(sampleReview)).thenReturn(expectedResponse);

                ReviewResponse result = reviewService.reviewSubmission(reviewRequest);

                assertThat(result.getResultStatus()).isEqualTo(ResultStatus.FAIL);
                assertThat(sampleReview.getResultStatus()).isEqualTo(ResultStatus.FAIL);
            }
        }

        @Test
        @DisplayName("should set PASS when marks awarded equals pass marks exactly")
        void reviewSubmission_exactPassMark() {
            sampleReview.setMarksAwarded(50f);
            sampleAssignment.setPassMarks(50f);

            try (MockedStatic<CommonUtil> util = mockStatic(CommonUtil.class)) {
                util.when(CommonUtil::extractUserIdFromRequest).thenReturn(REVIEWER_ID);
                when(submissionRepository.findById(SUBMISSION_ID)).thenReturn(Optional.of(sampleSubmission));
                when(reviewRepository.findBySubmissionId(SUBMISSION_ID)).thenReturn(Optional.empty());
                when(assignmentRepository.findById(ASSIGNMENT_ID)).thenReturn(Optional.of(sampleAssignment));
                when(reviewMapper.toEntity(reviewRequest, REVIEWER_ID)).thenReturn(sampleReview);
                when(reviewRepository.save(sampleReview)).thenReturn(sampleReview);
                when(submissionRepository.save(sampleSubmission)).thenReturn(sampleSubmission);
                when(reviewMapper.toResponse(sampleReview)).thenReturn(
                        ReviewResponse.builder().resultStatus(ResultStatus.PASS).build());

                reviewService.reviewSubmission(reviewRequest);

                assertThat(sampleReview.getResultStatus()).isEqualTo(ResultStatus.PASS);
            }
        }

        @Test
        @DisplayName("should treat null passMarks as 0 — any positive score is PASS")
        void reviewSubmission_nullPassMarks_treatedAsZero() {
            sampleAssignment.setPassMarks(null);
            sampleReview.setMarksAwarded(1f);

            try (MockedStatic<CommonUtil> util = mockStatic(CommonUtil.class)) {
                util.when(CommonUtil::extractUserIdFromRequest).thenReturn(REVIEWER_ID);
                when(submissionRepository.findById(SUBMISSION_ID)).thenReturn(Optional.of(sampleSubmission));
                when(reviewRepository.findBySubmissionId(SUBMISSION_ID)).thenReturn(Optional.empty());
                when(assignmentRepository.findById(ASSIGNMENT_ID)).thenReturn(Optional.of(sampleAssignment));
                when(reviewMapper.toEntity(reviewRequest, REVIEWER_ID)).thenReturn(sampleReview);
                when(reviewRepository.save(sampleReview)).thenReturn(sampleReview);
                when(submissionRepository.save(sampleSubmission)).thenReturn(sampleSubmission);
                when(reviewMapper.toResponse(sampleReview)).thenReturn(
                        ReviewResponse.builder().resultStatus(ResultStatus.PASS).build());

                reviewService.reviewSubmission(reviewRequest);

                assertThat(sampleReview.getResultStatus()).isEqualTo(ResultStatus.PASS);
            }
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when submission not found")
        void reviewSubmission_submissionNotFound() {
            try (MockedStatic<CommonUtil> util = mockStatic(CommonUtil.class)) {
                util.when(CommonUtil::extractUserIdFromRequest).thenReturn(REVIEWER_ID);
                when(submissionRepository.findById(SUBMISSION_ID)).thenReturn(Optional.empty());

                assertThatThrownBy(() -> reviewService.reviewSubmission(reviewRequest))
                        .isInstanceOf(ResourceNotFoundException.class)
                        .hasMessageContaining("Submission not found with id: " + SUBMISSION_ID);
            }
        }

        @Test
        @DisplayName("should default marksAwarded to 0 when it is null in review submission")
        void reviewSubmission_nullMarksAwarded() {
            sampleReview.setMarksAwarded(null);
            reviewRequest.setMarksAwarded(null);

            try (MockedStatic<CommonUtil> util = mockStatic(CommonUtil.class)) {
                util.when(CommonUtil::extractUserIdFromRequest).thenReturn(REVIEWER_ID);
                when(submissionRepository.findById(SUBMISSION_ID)).thenReturn(Optional.of(sampleSubmission));
                when(reviewRepository.findBySubmissionId(SUBMISSION_ID)).thenReturn(Optional.empty());
                when(assignmentRepository.findById(ASSIGNMENT_ID)).thenReturn(Optional.of(sampleAssignment));
                when(reviewMapper.toEntity(reviewRequest, REVIEWER_ID)).thenReturn(sampleReview);
                when(reviewRepository.save(sampleReview)).thenReturn(sampleReview);
                when(submissionRepository.save(sampleSubmission)).thenReturn(sampleSubmission);
                when(reviewMapper.toResponse(sampleReview)).thenReturn(
                        ReviewResponse.builder().resultStatus(ResultStatus.FAIL).build());

                ReviewResponse result = reviewService.reviewSubmission(reviewRequest);

                assertThat(sampleReview.getResultStatus()).isEqualTo(ResultStatus.FAIL);
            }
        }

        @Test
        @DisplayName("should throw ValidationException when submission already reviewed")
        void reviewSubmission_alreadyReviewed() {
            try (MockedStatic<CommonUtil> util = mockStatic(CommonUtil.class)) {
                util.when(CommonUtil::extractUserIdFromRequest).thenReturn(REVIEWER_ID);
                when(submissionRepository.findById(SUBMISSION_ID)).thenReturn(Optional.of(sampleSubmission));
                when(reviewRepository.findBySubmissionId(SUBMISSION_ID)).thenReturn(Optional.of(sampleReview));

                assertThatThrownBy(() -> reviewService.reviewSubmission(reviewRequest))
                        .isInstanceOf(ValidationException.class)
                        .hasMessageContaining("Submission already reviewed");
            }
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when assignment not found for submission")
        void reviewSubmission_assignmentNotFound() {
            try (MockedStatic<CommonUtil> util = mockStatic(CommonUtil.class)) {
                util.when(CommonUtil::extractUserIdFromRequest).thenReturn(REVIEWER_ID);
                when(submissionRepository.findById(SUBMISSION_ID)).thenReturn(Optional.of(sampleSubmission));
                when(reviewRepository.findBySubmissionId(SUBMISSION_ID)).thenReturn(Optional.empty());
                when(assignmentRepository.findById(ASSIGNMENT_ID)).thenReturn(Optional.empty());

                assertThatThrownBy(() -> reviewService.reviewSubmission(reviewRequest))
                        .isInstanceOf(ResourceNotFoundException.class)
                        .hasMessageContaining("Assignment not found with id: " + ASSIGNMENT_ID);
            }
        }
    }

    // -------------------------------------------------------------------------
    // getReviewBySubmission
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("getReviewBySubmission")
    class GetReviewBySubmission {

        @Test
        @DisplayName("should return review for a submission")
        void getReviewBySubmission_found() {
            ReviewResponse expectedResponse = ReviewResponse.builder()
                    .id(REVIEW_ID).submissionId(SUBMISSION_ID).build();

            when(reviewRepository.findBySubmissionId(SUBMISSION_ID)).thenReturn(Optional.of(sampleReview));
            when(reviewMapper.toResponse(sampleReview)).thenReturn(expectedResponse);

            ReviewResponse result = reviewService.getReviewBySubmission(SUBMISSION_ID);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(REVIEW_ID);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when no review for submission")
        void getReviewBySubmission_notFound() {
            when(reviewRepository.findBySubmissionId(SUBMISSION_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> reviewService.getReviewBySubmission(SUBMISSION_ID))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Review not found for submission: " + SUBMISSION_ID);
        }
    }

    // -------------------------------------------------------------------------
    // getReviewsByReviewer
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("getReviewsByReviewer")
    class GetReviewsByReviewer {

        @Test
        @DisplayName("should return all reviews by a reviewer")
        void getReviewsByReviewer_returnsList() {
            ReviewResponse resp = ReviewResponse.builder().id(REVIEW_ID).reviewerId(REVIEWER_ID).build();

            when(reviewRepository.findByReviewerId(REVIEWER_ID)).thenReturn(List.of(sampleReview));
            when(reviewMapper.toResponse(sampleReview)).thenReturn(resp);

            List<ReviewResponse> result = reviewService.getReviewsByReviewer(REVIEWER_ID);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getReviewerId()).isEqualTo(REVIEWER_ID);
        }

        @Test
        @DisplayName("should return empty list when reviewer has no reviews")
        void getReviewsByReviewer_empty() {
            when(reviewRepository.findByReviewerId(REVIEWER_ID)).thenReturn(List.of());

            List<ReviewResponse> result = reviewService.getReviewsByReviewer(REVIEWER_ID);

            assertThat(result).isEmpty();
        }
    }

    // -------------------------------------------------------------------------
    // updateReview
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("updateReview")
    class UpdateReview {

        @Test
        @DisplayName("should update feedback and marks, recalculate PASS result")
        void updateReview_updateFeedbackAndMarks_pass() {
            ReviewAssignmentRequest updateReq = new ReviewAssignmentRequest();
            updateReq.setFeedback("Excellent work");
            updateReq.setMarksAwarded(80f);
            updateReq.setSubmissionId(SUBMISSION_ID);

            ReviewResponse expectedResponse = ReviewResponse.builder()
                    .id(REVIEW_ID).feedback("Excellent work").marksAwarded(80f)
                    .resultStatus(ResultStatus.PASS).build();

            when(reviewRepository.findById(REVIEW_ID)).thenReturn(Optional.of(sampleReview));
            when(submissionRepository.findById(SUBMISSION_ID)).thenReturn(Optional.of(sampleSubmission));
            when(assignmentRepository.findById(ASSIGNMENT_ID)).thenReturn(Optional.of(sampleAssignment));
            when(reviewRepository.save(sampleReview)).thenReturn(sampleReview);
            when(reviewMapper.toResponse(sampleReview)).thenReturn(expectedResponse);

            ReviewResponse result = reviewService.updateReview(REVIEW_ID, updateReq);

            assertThat(result).isNotNull();
            assertThat(sampleReview.getFeedback()).isEqualTo("Excellent work");
            assertThat(sampleReview.getMarksAwarded()).isEqualTo(80f);
            assertThat(sampleReview.getResultStatus()).isEqualTo(ResultStatus.PASS);
        }

        @Test
        @DisplayName("should recalculate FAIL when updated marks are below pass marks")
        void updateReview_updateMarks_fail() {
            ReviewAssignmentRequest updateReq = new ReviewAssignmentRequest();
            updateReq.setMarksAwarded(20f);
            updateReq.setSubmissionId(SUBMISSION_ID);

            when(reviewRepository.findById(REVIEW_ID)).thenReturn(Optional.of(sampleReview));
            when(submissionRepository.findById(SUBMISSION_ID)).thenReturn(Optional.of(sampleSubmission));
            when(assignmentRepository.findById(ASSIGNMENT_ID)).thenReturn(Optional.of(sampleAssignment));
            when(reviewRepository.save(sampleReview)).thenReturn(sampleReview);
            when(reviewMapper.toResponse(sampleReview)).thenReturn(
                    ReviewResponse.builder().resultStatus(ResultStatus.FAIL).build());

            reviewService.updateReview(REVIEW_ID, updateReq);

            assertThat(sampleReview.getResultStatus()).isEqualTo(ResultStatus.FAIL);
        }

        @Test
        @DisplayName("should update only feedback without recalculating result status")
        void updateReview_updateFeedbackOnly() {
            ReviewAssignmentRequest updateReq = new ReviewAssignmentRequest();
            updateReq.setFeedback("Updated feedback");
            updateReq.setSubmissionId(SUBMISSION_ID);
            // marksAwarded is null — no recalculation expected

            when(reviewRepository.findById(REVIEW_ID)).thenReturn(Optional.of(sampleReview));
            when(submissionRepository.findById(SUBMISSION_ID)).thenReturn(Optional.of(sampleSubmission));
            when(assignmentRepository.findById(ASSIGNMENT_ID)).thenReturn(Optional.of(sampleAssignment));
            when(reviewRepository.save(sampleReview)).thenReturn(sampleReview);
            when(reviewMapper.toResponse(sampleReview)).thenReturn(
                    ReviewResponse.builder().feedback("Updated feedback").build());

            reviewService.updateReview(REVIEW_ID, updateReq);

            assertThat(sampleReview.getFeedback()).isEqualTo("Updated feedback");
            // result status should remain unchanged — no recalculation when marksAwarded is null
            assertThat(sampleReview.getResultStatus()).isNull();
        }

        @Test
        @DisplayName("should treat null passMarks as 0 when updating review marks")
        void updateReview_nullPassMarks() {
            ReviewAssignmentRequest updateReq = new ReviewAssignmentRequest();
            updateReq.setMarksAwarded(10f);
            updateReq.setSubmissionId(SUBMISSION_ID);

            sampleAssignment.setPassMarks(null);

            when(reviewRepository.findById(REVIEW_ID)).thenReturn(Optional.of(sampleReview));
            when(submissionRepository.findById(SUBMISSION_ID)).thenReturn(Optional.of(sampleSubmission));
            when(assignmentRepository.findById(ASSIGNMENT_ID)).thenReturn(Optional.of(sampleAssignment));
            when(reviewRepository.save(sampleReview)).thenReturn(sampleReview);
            when(reviewMapper.toResponse(sampleReview)).thenReturn(
                    ReviewResponse.builder().resultStatus(ResultStatus.PASS).build());

            reviewService.updateReview(REVIEW_ID, updateReq);

            assertThat(sampleReview.getResultStatus()).isEqualTo(ResultStatus.PASS);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when review not found")
        void updateReview_reviewNotFound() {
            when(reviewRepository.findById(REVIEW_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> reviewService.updateReview(REVIEW_ID, reviewRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Review not found with id: " + REVIEW_ID);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when submission for review not found")
        void updateReview_submissionNotFound() {
            when(reviewRepository.findById(REVIEW_ID)).thenReturn(Optional.of(sampleReview));
            when(submissionRepository.findById(SUBMISSION_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> reviewService.updateReview(REVIEW_ID, reviewRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Submission not found for this review");
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when assignment for review not found")
        void updateReview_assignmentNotFound() {
            when(reviewRepository.findById(REVIEW_ID)).thenReturn(Optional.of(sampleReview));
            when(submissionRepository.findById(SUBMISSION_ID)).thenReturn(Optional.of(sampleSubmission));
            when(assignmentRepository.findById(ASSIGNMENT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> reviewService.updateReview(REVIEW_ID, reviewRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Assignment not found for this review");
        }
    }
}
