package org.assessment.mapper;

import org.assessment.dto.request.ReviewAssignmentRequest;
import org.assessment.dto.response.ReviewResponse;
import org.assessment.entity.Review;
import org.assessment.enums.ResultStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AssignmentReviewMapper Tests")
class AssignmentReviewMapperTest {

    private AssignmentReviewMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new AssignmentReviewMapper();
    }

    // -------------------------------------------------------------------------
    // toEntity
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("toEntity")
    class ToEntity {

        @Test
        @DisplayName("should map all request fields to review entity")
        void toEntity_mapsAllFields() {
            ReviewAssignmentRequest request = new ReviewAssignmentRequest();
            request.setSubmissionId("sub-001");
            request.setMarksAwarded(75f);
            request.setFeedback("Well done");

            Review entity = mapper.toEntity(request, "instructor-1");

            assertThat(entity).isNotNull();
            assertThat(entity.getSubmissionId()).isEqualTo("sub-001");
            assertThat(entity.getReviewerId()).isEqualTo("instructor-1");
            assertThat(entity.getMarksAwarded()).isEqualTo(75f);
            assertThat(entity.getFeedback()).isEqualTo("Well done");
        }

        @Test
        @DisplayName("should generate a non-null UUID as reviewId")
        void toEntity_generatesReviewId() {
            ReviewAssignmentRequest request = new ReviewAssignmentRequest();
            request.setSubmissionId("sub-001");
            request.setMarksAwarded(60f);

            Review entity = mapper.toEntity(request, "instructor-1");

            assertThat(entity.getReviewId()).isNotNull();
            assertThat(entity.getReviewId()).isNotBlank();
        }

        @Test
        @DisplayName("should generate unique reviewIds for different calls")
        void toEntity_generatesUniqueIds() {
            ReviewAssignmentRequest request = new ReviewAssignmentRequest();
            request.setSubmissionId("sub-001");
            request.setMarksAwarded(60f);

            Review entity1 = mapper.toEntity(request, "instructor-1");
            Review entity2 = mapper.toEntity(request, "instructor-1");

            assertThat(entity1.getReviewId()).isNotEqualTo(entity2.getReviewId());
        }

        @Test
        @DisplayName("should set reviewedAt to current time")
        void toEntity_setsReviewedAt() {
            LocalDateTime before = LocalDateTime.now().minusSeconds(1);
            ReviewAssignmentRequest request = new ReviewAssignmentRequest();
            request.setSubmissionId("sub-001");
            request.setMarksAwarded(60f);

            Review entity = mapper.toEntity(request, "instructor-1");

            assertThat(entity.getReviewedAt()).isAfterOrEqualTo(before);
        }

        @Test
        @DisplayName("should return null when request is null")
        void toEntity_nullRequest_returnsNull() {
            Review entity = mapper.toEntity(null, "instructor-1");

            assertThat(entity).isNull();
        }

        @Test
        @DisplayName("should map null feedback when feedback is not provided")
        void toEntity_nullFeedback() {
            ReviewAssignmentRequest request = new ReviewAssignmentRequest();
            request.setSubmissionId("sub-001");
            request.setMarksAwarded(50f);
            request.setFeedback(null);

            Review entity = mapper.toEntity(request, "instructor-1");

            assertThat(entity.getFeedback()).isNull();
        }
    }

    // -------------------------------------------------------------------------
    // toResponse
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("toResponse")
    class ToResponse {

        @Test
        @DisplayName("should map all review fields to response")
        void toResponse_mapsAllFields() {
            LocalDateTime reviewedAt = LocalDateTime.of(2025, 6, 15, 14, 30);
            Review review = Review.builder()
                    .reviewId("rev-001")
                    .submissionId("sub-001")
                    .reviewerId("instructor-1")
                    .marksAwarded(85f)
                    .feedback("Excellent work")
                    .resultStatus(ResultStatus.PASS)
                    .reviewedAt(reviewedAt)
                    .build();

            ReviewResponse response = mapper.toResponse(review);

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo("rev-001");
            assertThat(response.getSubmissionId()).isEqualTo("sub-001");
            assertThat(response.getReviewerId()).isEqualTo("instructor-1");
            assertThat(response.getMarksAwarded()).isEqualTo(85f);
            assertThat(response.getFeedback()).isEqualTo("Excellent work");
            assertThat(response.getResultStatus()).isEqualTo(ResultStatus.PASS);
            assertThat(response.getReviewedAt()).isEqualTo(reviewedAt.toString());
        }

        @Test
        @DisplayName("should use reviewedAt for createdAt and updatedAt fields")
        void toResponse_reviewedAtUsedForTimestamps() {
            LocalDateTime reviewedAt = LocalDateTime.of(2025, 3, 20, 12, 0);
            Review review = Review.builder()
                    .reviewId("rev-001").submissionId("sub-001")
                    .reviewedAt(reviewedAt).marksAwarded(70f).build();

            ReviewResponse response = mapper.toResponse(review);

            assertThat(response.getCreatedAt()).isEqualTo(reviewedAt.toString());
            assertThat(response.getUpdatedAt()).isEqualTo(reviewedAt.toString());
        }

        @Test
        @DisplayName("should return null time fields when reviewedAt is null")
        void toResponse_nullReviewedAt_nullTimestamps() {
            Review review = Review.builder()
                    .reviewId("rev-001").submissionId("sub-001")
                    .marksAwarded(70f).reviewedAt(null).build();

            ReviewResponse response = mapper.toResponse(review);

            assertThat(response.getReviewedAt()).isNull();
            assertThat(response.getCreatedAt()).isNull();
            assertThat(response.getUpdatedAt()).isNull();
        }

        @Test
        @DisplayName("should map FAIL result status correctly")
        void toResponse_failResultStatus() {
            Review review = Review.builder()
                    .reviewId("rev-001").submissionId("sub-001")
                    .marksAwarded(20f).resultStatus(ResultStatus.FAIL)
                    .reviewedAt(LocalDateTime.now()).build();

            ReviewResponse response = mapper.toResponse(review);

            assertThat(response.getResultStatus()).isEqualTo(ResultStatus.FAIL);
        }

        @Test
        @DisplayName("should return null when review is null")
        void toResponse_nullReview_returnsNull() {
            ReviewResponse response = mapper.toResponse(null);

            assertThat(response).isNull();
        }
    }
}
