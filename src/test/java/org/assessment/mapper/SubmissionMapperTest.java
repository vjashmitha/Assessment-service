package org.assessment.mapper;

import org.assessment.dto.request.SubmitAssignmentRequest;
import org.assessment.dto.response.SubmissionResponse;
import org.assessment.entity.Review;
import org.assessment.entity.Submission;
import org.assessment.enums.ResultStatus;
import org.assessment.enums.SubmissionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SubmissionMapper Tests")
class SubmissionMapperTest {

    private SubmissionMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new SubmissionMapper();
    }

    // -------------------------------------------------------------------------
    // toEntity
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("toEntity")
    class ToEntity {

        @Test
        @DisplayName("should map all fields from request to entity")
        void toEntity_mapsAllFields() {
            SubmitAssignmentRequest request = new SubmitAssignmentRequest();
            request.setAssignmentId("assign-001");
            request.setFileUrl("https://s3/file.pdf");
            request.setContent("My answer text");

            Submission entity = mapper.toEntity(request, "student-1");

            assertThat(entity).isNotNull();
            assertThat(entity.getAssignmentId()).isEqualTo("assign-001");
            assertThat(entity.getLearnerId()).isEqualTo("student-1");
            assertThat(entity.getSubmissionFileUrl()).isEqualTo("https://s3/file.pdf");
        }

        @Test
        @DisplayName("should automatically set status to SUBMITTED")
        void toEntity_setsSubmittedStatus() {
            SubmitAssignmentRequest request = new SubmitAssignmentRequest();
            request.setAssignmentId("assign-001");

            Submission entity = mapper.toEntity(request, "student-1");

            assertThat(entity.getStatus()).isEqualTo(SubmissionStatus.SUBMITTED);
        }

        @Test
        @DisplayName("should generate a non-null UUID submissionId")
        void toEntity_generatesSubmissionId() {
            SubmitAssignmentRequest request = new SubmitAssignmentRequest();
            request.setAssignmentId("assign-001");

            Submission entity = mapper.toEntity(request, "student-1");

            assertThat(entity.getSubmissionId()).isNotNull();
            assertThat(entity.getSubmissionId()).isNotBlank();
        }

        @Test
        @DisplayName("should generate unique submissionIds on each call")
        void toEntity_generatesUniqueIds() {
            SubmitAssignmentRequest request = new SubmitAssignmentRequest();
            request.setAssignmentId("assign-001");

            Submission e1 = mapper.toEntity(request, "student-1");
            Submission e2 = mapper.toEntity(request, "student-1");

            assertThat(e1.getSubmissionId()).isNotEqualTo(e2.getSubmissionId());
        }

        @Test
        @DisplayName("should set submittedAt to current time")
        void toEntity_setsSubmittedAt() {
            LocalDateTime before = LocalDateTime.now().minusSeconds(1);
            SubmitAssignmentRequest request = new SubmitAssignmentRequest();
            request.setAssignmentId("assign-001");

            Submission entity = mapper.toEntity(request, "student-1");

            assertThat(entity.getSubmittedAt()).isAfterOrEqualTo(before);
        }

        @Test
        @DisplayName("should return null when request is null")
        void toEntity_nullRequest_returnsNull() {
            Submission entity = mapper.toEntity(null, "student-1");

            assertThat(entity).isNull();
        }
    }

    // -------------------------------------------------------------------------
    // toResponse(Submission) — single-arg overload
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("toResponse(Submission)")
    class ToResponseNoReview {

        @Test
        @DisplayName("should map submission fields with null review fields")
        void toResponse_noReview_reviewFieldsNull() {
            LocalDateTime submittedAt = LocalDateTime.of(2025, 5, 10, 9, 0);
            Submission submission = Submission.builder()
                    .submissionId("sub-001")
                    .assignmentId("assign-001")
                    .learnerId("student-1")
                    .submissionFileUrl("https://s3/sub.pdf")
                    .status(SubmissionStatus.SUBMITTED)
                    .submittedAt(submittedAt)
                    .build();

            SubmissionResponse response = mapper.toResponse(submission);

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo("sub-001");
            assertThat(response.getAssignmentId()).isEqualTo("assign-001");
            assertThat(response.getStudentId()).isEqualTo("student-1");
            assertThat(response.getFileUrl()).isEqualTo("https://s3/sub.pdf");
            assertThat(response.getStatus()).isEqualTo(SubmissionStatus.SUBMITTED);
            assertThat(response.getResultStatus()).isNull();
            assertThat(response.getObtainedMarks()).isNull();
            assertThat(response.getFeedback()).isNull();
        }
    }

    // -------------------------------------------------------------------------
    // toResponse(Submission, Review) — two-arg overload
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("toResponse(Submission, Review)")
    class ToResponseWithReview {

        @Test
        @DisplayName("should populate review fields when review is provided")
        void toResponse_withReview_mapsReviewFields() {
            LocalDateTime submittedAt = LocalDateTime.of(2025, 5, 10, 9, 0);
            Submission submission = Submission.builder()
                    .submissionId("sub-001").assignmentId("assign-001").learnerId("student-1")
                    .status(SubmissionStatus.REVIEWED).submittedAt(submittedAt).build();
            Review review = Review.builder()
                    .reviewId("rev-001").submissionId("sub-001")
                    .marksAwarded(85f).resultStatus(ResultStatus.PASS)
                    .feedback("Great job").build();

            SubmissionResponse response = mapper.toResponse(submission, review);

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo("sub-001");
            assertThat(response.getStatus()).isEqualTo(SubmissionStatus.REVIEWED);
            assertThat(response.getObtainedMarks()).isEqualTo(85f);
            assertThat(response.getResultStatus()).isEqualTo(ResultStatus.PASS);
            assertThat(response.getFeedback()).isEqualTo("Great job");
        }

        @Test
        @DisplayName("should return null obtainedMarks when review has null marksAwarded")
        void toResponse_nullMarksAwarded_returnsNullObtainedMarks() {
            Submission submission = Submission.builder()
                    .submissionId("sub-001").assignmentId("assign-001").learnerId("student-1")
                    .status(SubmissionStatus.REVIEWED).submittedAt(LocalDateTime.now()).build();
            Review reviewNoMarks = Review.builder()
                    .reviewId("rev-001").submissionId("sub-001")
                    .marksAwarded(null).resultStatus(ResultStatus.PENDING).build();

            SubmissionResponse response = mapper.toResponse(submission, reviewNoMarks);

            assertThat(response.getObtainedMarks()).isNull();
        }

        @Test
        @DisplayName("should set all review fields to null when review is null")
        void toResponse_nullReview_reviewFieldsNull() {
            Submission submission = Submission.builder()
                    .submissionId("sub-001").assignmentId("assign-001").learnerId("student-1")
                    .status(SubmissionStatus.SUBMITTED).submittedAt(LocalDateTime.now()).build();

            SubmissionResponse response = mapper.toResponse(submission, null);

            assertThat(response.getResultStatus()).isNull();
            assertThat(response.getObtainedMarks()).isNull();
            assertThat(response.getFeedback()).isNull();
        }

        @Test
        @DisplayName("should return null when submission is null")
        void toResponse_nullSubmission_returnsNull() {
            SubmissionResponse response = mapper.toResponse(null, null);

            assertThat(response).isNull();
        }

        @Test
        @DisplayName("should use submittedAt for createdAt and updatedAt fields")
        void toResponse_submittedAtUsedForTimestamps() {
            LocalDateTime submittedAt = LocalDateTime.of(2025, 1, 15, 8, 30);
            Submission submission = Submission.builder()
                    .submissionId("sub-001").assignmentId("assign-001").learnerId("student-1")
                    .status(SubmissionStatus.SUBMITTED).submittedAt(submittedAt).build();

            SubmissionResponse response = mapper.toResponse(submission, null);

            String expectedTime = submittedAt.toString();
            assertThat(response.getSubmittedAt()).isEqualTo(expectedTime);
            assertThat(response.getCreatedAt()).isEqualTo(expectedTime);
            assertThat(response.getUpdatedAt()).isEqualTo(expectedTime);
        }

        @Test
        @DisplayName("should return null time fields when submittedAt is null")
        void toResponse_nullSubmittedAt_nullTimestamps() {
            Submission submission = Submission.builder()
                    .submissionId("sub-001").assignmentId("assign-001").learnerId("student-1")
                    .status(SubmissionStatus.SUBMITTED).submittedAt(null).build();

            SubmissionResponse response = mapper.toResponse(submission, null);

            assertThat(response.getSubmittedAt()).isNull();
            assertThat(response.getCreatedAt()).isNull();
            assertThat(response.getUpdatedAt()).isNull();
        }
    }
}
