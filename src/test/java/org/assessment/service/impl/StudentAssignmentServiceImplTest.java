package org.assessment.service.impl;

import org.assessment.dto.response.StudentAssignmentResponse;
import org.assessment.entity.Assignment;
import org.assessment.entity.Review;
import org.assessment.entity.Submission;
import org.assessment.enums.AssignmentStatus;
import org.assessment.enums.DifficultyLevel;
import org.assessment.enums.ResultStatus;
import org.assessment.enums.SubmissionStatus;
import org.assessment.exception.ResourceNotFoundException;
import org.assessment.repository.AssignmentRepository;
import org.assessment.repository.ReviewRepository;
import org.assessment.repository.SubmissionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StudentAssignmentServiceImpl Tests")
class StudentAssignmentServiceImplTest {

    @Mock private AssignmentRepository assignmentRepository;
    @Mock private SubmissionRepository submissionRepository;
    @Mock private ReviewRepository reviewRepository;

    @InjectMocks
    private StudentAssignmentServiceImpl studentAssignmentService;

    private static final String STUDENT_ID   = "student-1";
    private static final String ASSIGNMENT_ID = "assign-001";
    private static final String SUBMISSION_ID = "sub-001";

    private Assignment baseAssignment;

    @BeforeEach
    void setUp() {
        baseAssignment = Assignment.builder()
                .assignmentId(ASSIGNMENT_ID)
                .title("Java REST API")
                .description("Build a REST API using Spring Boot")
                .courseId("course-101")
                .courseName("Java Fundamentals")
                .totalMarks(50f)
                .passMarks(25f)
                .difficultyLevel(DifficultyLevel.INTERMEDIATE)
                .status(AssignmentStatus.PUBLISHED)
                .dueDate(LocalDate.now().plusDays(7))
                .createdAt(LocalDateTime.now())
                .assignmentFileUrl("https://s3.amazonaws.com/bucket/assignments/Assignment_REST_API.pdf")
                .build();
    }

    // -------------------------------------------------------------------------
    // getMyAssignments
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("getMyAssignments")
    class GetMyAssignments {

        @Test
        @DisplayName("should return list of StudentAssignmentResponse for all assignments")
        void getMyAssignments_returnsList() {
            when(assignmentRepository.findAll()).thenReturn(List.of(baseAssignment));
            when(submissionRepository.findByAssignmentIdAndLearnerId(ASSIGNMENT_ID, STUDENT_ID))
                    .thenReturn(Optional.empty());

            List<StudentAssignmentResponse> result = studentAssignmentService.getMyAssignments(STUDENT_ID);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getAssignmentId()).isEqualTo(ASSIGNMENT_ID);
            assertThat(result.get(0).getTitle()).isEqualTo("Java REST API");
        }

        @Test
        @DisplayName("should default submissionStatus to NOT_SUBMITTED when no submission exists")
        void getMyAssignments_noSubmission_defaultsToNotSubmitted() {
            when(assignmentRepository.findAll()).thenReturn(List.of(baseAssignment));
            when(submissionRepository.findByAssignmentIdAndLearnerId(ASSIGNMENT_ID, STUDENT_ID))
                    .thenReturn(Optional.empty());

            List<StudentAssignmentResponse> result = studentAssignmentService.getMyAssignments(STUDENT_ID);

            assertThat(result.get(0).getSubmissionStatus()).isEqualTo(SubmissionStatus.NOT_SUBMITTED);
            assertThat(result.get(0).getSubmissionId()).isNull();
        }

        @Test
        @DisplayName("should return empty list when no assignments exist")
        void getMyAssignments_empty() {
            when(assignmentRepository.findAll()).thenReturn(List.of());

            List<StudentAssignmentResponse> result = studentAssignmentService.getMyAssignments(STUDENT_ID);

            assertThat(result).isEmpty();
        }
    }

    // -------------------------------------------------------------------------
    // getMyAssignmentsByStatus
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("getMyAssignmentsByStatus")
    class GetMyAssignmentsByStatus {

        @Test
        @DisplayName("should filter assignments by submission status")
        void filterByStatus_submitted() {
            Submission sub = Submission.builder()
                    .submissionId(SUBMISSION_ID).assignmentId(ASSIGNMENT_ID).learnerId(STUDENT_ID)
                    .status(SubmissionStatus.SUBMITTED).submittedAt(LocalDateTime.now()).build();

            when(assignmentRepository.findAll()).thenReturn(List.of(baseAssignment));
            when(submissionRepository.findByAssignmentIdAndLearnerId(ASSIGNMENT_ID, STUDENT_ID))
                    .thenReturn(Optional.of(sub));
            when(reviewRepository.findBySubmissionId(SUBMISSION_ID)).thenReturn(Optional.empty());

            List<StudentAssignmentResponse> result =
                    studentAssignmentService.getMyAssignmentsByStatus(STUDENT_ID, SubmissionStatus.SUBMITTED);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getSubmissionStatus()).isEqualTo(SubmissionStatus.SUBMITTED);
        }

        @Test
        @DisplayName("should return empty when no assignments match status filter")
        void filterByStatus_noMatch() {
            when(assignmentRepository.findAll()).thenReturn(List.of(baseAssignment));
            when(submissionRepository.findByAssignmentIdAndLearnerId(ASSIGNMENT_ID, STUDENT_ID))
                    .thenReturn(Optional.empty());

            List<StudentAssignmentResponse> result =
                    studentAssignmentService.getMyAssignmentsByStatus(STUDENT_ID, SubmissionStatus.REVIEWED);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should return all assignments when status filter is null")
        void filterByStatus_nullStatus_returnsAll() {
            when(assignmentRepository.findAll()).thenReturn(List.of(baseAssignment));
            when(submissionRepository.findByAssignmentIdAndLearnerId(ASSIGNMENT_ID, STUDENT_ID))
                    .thenReturn(Optional.empty());

            List<StudentAssignmentResponse> result =
                    studentAssignmentService.getMyAssignmentsByStatus(STUDENT_ID, null);

            assertThat(result).hasSize(1);
        }
    }

    // -------------------------------------------------------------------------
    // getAssignmentDetail
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("getAssignmentDetail")
    class GetAssignmentDetail {

        @Test
        @DisplayName("should return full detail for assignment and student")
        void getAssignmentDetail_found() {
            when(assignmentRepository.findById(ASSIGNMENT_ID)).thenReturn(Optional.of(baseAssignment));
            when(submissionRepository.findByAssignmentIdAndLearnerId(ASSIGNMENT_ID, STUDENT_ID))
                    .thenReturn(Optional.empty());

            StudentAssignmentResponse result =
                    studentAssignmentService.getAssignmentDetail(ASSIGNMENT_ID, STUDENT_ID);

            assertThat(result).isNotNull();
            assertThat(result.getAssignmentId()).isEqualTo(ASSIGNMENT_ID);
            assertThat(result.getTitle()).isEqualTo("Java REST API");
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when assignment not found")
        void getAssignmentDetail_notFound() {
            when(assignmentRepository.findById("missing")).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    studentAssignmentService.getAssignmentDetail("missing", STUDENT_ID))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Assignment not found: missing");
        }
    }

    // -------------------------------------------------------------------------
    // buildStudentAssignmentResponse — overdue logic
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("Overdue logic")
    class OverdueLogic {

        @Test
        @DisplayName("should mark overdue when due date is past and no submission")
        void overdue_pastDueNoSubmission() {
            baseAssignment.setDueDate(LocalDate.now().minusDays(1));

            when(assignmentRepository.findAll()).thenReturn(List.of(baseAssignment));
            when(submissionRepository.findByAssignmentIdAndLearnerId(ASSIGNMENT_ID, STUDENT_ID))
                    .thenReturn(Optional.empty());

            List<StudentAssignmentResponse> result = studentAssignmentService.getMyAssignments(STUDENT_ID);

            assertThat(result.get(0).isOverdue()).isTrue();
        }

        @Test
        @DisplayName("should not mark overdue when due date is in the future")
        void overdue_futureDueDate_notOverdue() {
            baseAssignment.setDueDate(LocalDate.now().plusDays(3));

            when(assignmentRepository.findAll()).thenReturn(List.of(baseAssignment));
            when(submissionRepository.findByAssignmentIdAndLearnerId(ASSIGNMENT_ID, STUDENT_ID))
                    .thenReturn(Optional.empty());

            List<StudentAssignmentResponse> result = studentAssignmentService.getMyAssignments(STUDENT_ID);

            assertThat(result.get(0).isOverdue()).isFalse();
        }

        @Test
        @DisplayName("should not mark overdue when student has submitted even if past due")
        void overdue_submitted_notOverdue() {
            baseAssignment.setDueDate(LocalDate.now().minusDays(2));
            Submission sub = Submission.builder()
                    .submissionId(SUBMISSION_ID).assignmentId(ASSIGNMENT_ID).learnerId(STUDENT_ID)
                    .status(SubmissionStatus.SUBMITTED).submittedAt(LocalDateTime.now()).build();

            when(assignmentRepository.findAll()).thenReturn(List.of(baseAssignment));
            when(submissionRepository.findByAssignmentIdAndLearnerId(ASSIGNMENT_ID, STUDENT_ID))
                    .thenReturn(Optional.of(sub));
            when(reviewRepository.findBySubmissionId(SUBMISSION_ID)).thenReturn(Optional.empty());

            List<StudentAssignmentResponse> result = studentAssignmentService.getMyAssignments(STUDENT_ID);

            assertThat(result.get(0).isOverdue()).isFalse();
        }

        @Test
        @DisplayName("should not mark overdue when assignment has no due date")
        void overdue_noDueDate_notOverdue() {
            baseAssignment.setDueDate(null);

            when(assignmentRepository.findAll()).thenReturn(List.of(baseAssignment));
            when(submissionRepository.findByAssignmentIdAndLearnerId(ASSIGNMENT_ID, STUDENT_ID))
                    .thenReturn(Optional.empty());

            List<StudentAssignmentResponse> result = studentAssignmentService.getMyAssignments(STUDENT_ID);

            assertThat(result.get(0).isOverdue()).isFalse();
        }

        @Test
        @DisplayName("should mark overdue when submission status is NOT_SUBMITTED and due date is past")
        void overdue_submissionNotSubmitted_isOverdue() {
            baseAssignment.setDueDate(LocalDate.now().minusDays(1));
            Submission sub = Submission.builder()
                    .submissionId(SUBMISSION_ID).assignmentId(ASSIGNMENT_ID).learnerId(STUDENT_ID)
                    .status(SubmissionStatus.NOT_SUBMITTED).build();

            when(assignmentRepository.findAll()).thenReturn(List.of(baseAssignment));
            when(submissionRepository.findByAssignmentIdAndLearnerId(ASSIGNMENT_ID, STUDENT_ID))
                    .thenReturn(Optional.of(sub));

            List<StudentAssignmentResponse> result = studentAssignmentService.getMyAssignments(STUDENT_ID);

            assertThat(result.get(0).isOverdue()).isTrue();
        }
    }

    // -------------------------------------------------------------------------
    // buildStudentAssignmentResponse — scorePercentage calculation
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("Score percentage calculation")
    class ScorePercentage {

        @Test
        @DisplayName("should calculate scorePercentage correctly rounded to 1 decimal")
        void scorePercentage_calculated() {
            // 44 / 50 * 100 = 88.0
            Submission sub = Submission.builder()
                    .submissionId(SUBMISSION_ID).assignmentId(ASSIGNMENT_ID).learnerId(STUDENT_ID)
                    .status(SubmissionStatus.REVIEWED).submittedAt(LocalDateTime.now()).build();
            Review review = Review.builder()
                    .reviewId("rev-001").submissionId(SUBMISSION_ID)
                    .marksAwarded(44f).resultStatus(ResultStatus.PASS)
                    .feedback("Well done").reviewedAt(LocalDateTime.now()).build();

            when(assignmentRepository.findAll()).thenReturn(List.of(baseAssignment));
            when(submissionRepository.findByAssignmentIdAndLearnerId(ASSIGNMENT_ID, STUDENT_ID))
                    .thenReturn(Optional.of(sub));
            when(reviewRepository.findBySubmissionId(SUBMISSION_ID)).thenReturn(Optional.of(review));

            List<StudentAssignmentResponse> result = studentAssignmentService.getMyAssignments(STUDENT_ID);
            StudentAssignmentResponse response = result.get(0);

            assertThat(response.getScorePercentage()).isEqualTo(88.0f);
            assertThat(response.getMarksAwarded()).isEqualTo(44f);
            assertThat(response.getResultStatus()).isEqualTo(ResultStatus.PASS);
            assertThat(response.getFeedback()).isEqualTo("Well done");
        }

        @Test
        @DisplayName("should return null scorePercentage when no review exists")
        void scorePercentage_noReview_null() {
            when(assignmentRepository.findAll()).thenReturn(List.of(baseAssignment));
            when(submissionRepository.findByAssignmentIdAndLearnerId(ASSIGNMENT_ID, STUDENT_ID))
                    .thenReturn(Optional.empty());

            List<StudentAssignmentResponse> result = studentAssignmentService.getMyAssignments(STUDENT_ID);

            assertThat(result.get(0).getScorePercentage()).isNull();
            assertThat(result.get(0).getResultStatus()).isEqualTo(ResultStatus.PENDING);
        }

        @Test
        @DisplayName("should return null scorePercentage when marksAwarded is null")
        void scorePercentage_nullMarks_null() {
            Submission sub = Submission.builder()
                    .submissionId(SUBMISSION_ID).assignmentId(ASSIGNMENT_ID).learnerId(STUDENT_ID)
                    .status(SubmissionStatus.REVIEWED).build();
            Review reviewNoMarks = Review.builder()
                    .reviewId("rev-001").submissionId(SUBMISSION_ID)
                    .marksAwarded(null).resultStatus(ResultStatus.PENDING).build();

            when(assignmentRepository.findAll()).thenReturn(List.of(baseAssignment));
            when(submissionRepository.findByAssignmentIdAndLearnerId(ASSIGNMENT_ID, STUDENT_ID))
                    .thenReturn(Optional.of(sub));
            when(reviewRepository.findBySubmissionId(SUBMISSION_ID)).thenReturn(Optional.of(reviewNoMarks));

            List<StudentAssignmentResponse> result = studentAssignmentService.getMyAssignments(STUDENT_ID);

            assertThat(result.get(0).getScorePercentage()).isNull();
        }

        @Test
        @DisplayName("should return null scorePercentage when totalMarks is zero")
        void scorePercentage_zeroTotalMarks_null() {
            baseAssignment.setTotalMarks(0f);

            Submission sub = Submission.builder()
                    .submissionId(SUBMISSION_ID).assignmentId(ASSIGNMENT_ID).learnerId(STUDENT_ID)
                    .status(SubmissionStatus.REVIEWED).build();
            Review review = Review.builder()
                    .reviewId("rev-001").submissionId(SUBMISSION_ID)
                    .marksAwarded(30f).resultStatus(ResultStatus.FAIL).build();

            when(assignmentRepository.findAll()).thenReturn(List.of(baseAssignment));
            when(submissionRepository.findByAssignmentIdAndLearnerId(ASSIGNMENT_ID, STUDENT_ID))
                    .thenReturn(Optional.of(sub));
            when(reviewRepository.findBySubmissionId(SUBMISSION_ID)).thenReturn(Optional.of(review));

            List<StudentAssignmentResponse> result = studentAssignmentService.getMyAssignments(STUDENT_ID);

            assertThat(result.get(0).getScorePercentage()).isNull();
        }

        @Test
        @DisplayName("should return null scorePercentage when totalMarks is null")
        void scorePercentage_nullTotalMarks_null() {
            baseAssignment.setTotalMarks(null);

            Submission sub = Submission.builder()
                    .submissionId(SUBMISSION_ID).assignmentId(ASSIGNMENT_ID).learnerId(STUDENT_ID)
                    .status(SubmissionStatus.REVIEWED).build();
            Review review = Review.builder()
                    .reviewId("rev-001").submissionId(SUBMISSION_ID)
                    .marksAwarded(30f).resultStatus(ResultStatus.FAIL).build();

            when(assignmentRepository.findAll()).thenReturn(List.of(baseAssignment));
            when(submissionRepository.findByAssignmentIdAndLearnerId(ASSIGNMENT_ID, STUDENT_ID))
                    .thenReturn(Optional.of(sub));
            when(reviewRepository.findBySubmissionId(SUBMISSION_ID)).thenReturn(Optional.of(review));

            List<StudentAssignmentResponse> result = studentAssignmentService.getMyAssignments(STUDENT_ID);

            assertThat(result.get(0).getScorePercentage()).isNull();
        }
    }

    // -------------------------------------------------------------------------
    // buildStudentAssignmentResponse — extractFileName logic
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("extractFileName logic")
    class ExtractFileName {

        @Test
        @DisplayName("should extract filename from full S3 URL")
        void extractFileName_fromUrl() {
            // assignmentFileUrl already set in setUp to full S3 URL
            when(assignmentRepository.findAll()).thenReturn(List.of(baseAssignment));
            when(submissionRepository.findByAssignmentIdAndLearnerId(ASSIGNMENT_ID, STUDENT_ID))
                    .thenReturn(Optional.empty());

            List<StudentAssignmentResponse> result = studentAssignmentService.getMyAssignments(STUDENT_ID);

            assertThat(result.get(0).getAssignmentFileName()).isEqualTo("Assignment_REST_API.pdf");
        }

        @Test
        @DisplayName("should return null assignmentFileName when no file URL")
        void extractFileName_nullUrl_returnsNull() {
            baseAssignment.setAssignmentFileUrl(null);

            when(assignmentRepository.findAll()).thenReturn(List.of(baseAssignment));
            when(submissionRepository.findByAssignmentIdAndLearnerId(ASSIGNMENT_ID, STUDENT_ID))
                    .thenReturn(Optional.empty());

            List<StudentAssignmentResponse> result = studentAssignmentService.getMyAssignments(STUDENT_ID);

            assertThat(result.get(0).getAssignmentFileName()).isNull();
        }

        @Test
        @DisplayName("should return null assignmentFileName when file URL is blank")
        void extractFileName_blankUrl_returnsNull() {
            baseAssignment.setAssignmentFileUrl("   ");

            when(assignmentRepository.findAll()).thenReturn(List.of(baseAssignment));
            when(submissionRepository.findByAssignmentIdAndLearnerId(ASSIGNMENT_ID, STUDENT_ID))
                    .thenReturn(Optional.empty());

            List<StudentAssignmentResponse> result = studentAssignmentService.getMyAssignments(STUDENT_ID);

            assertThat(result.get(0).getAssignmentFileName()).isNull();
        }

        @Test
        @DisplayName("should return full URL as filename when there is no slash in URL")
        void extractFileName_noSlashUrl_returnsSame() {
            baseAssignment.setAssignmentFileUrl("my_file.pdf");

            when(assignmentRepository.findAll()).thenReturn(List.of(baseAssignment));
            when(submissionRepository.findByAssignmentIdAndLearnerId(ASSIGNMENT_ID, STUDENT_ID))
                    .thenReturn(Optional.empty());

            List<StudentAssignmentResponse> result = studentAssignmentService.getMyAssignments(STUDENT_ID);

            assertThat(result.get(0).getAssignmentFileName()).isEqualTo("my_file.pdf");
        }

        @Test
        @DisplayName("should extract submission filename from submission file URL")
        void extractFileName_submissionFile() {
            Submission sub = Submission.builder()
                    .submissionId(SUBMISSION_ID).assignmentId(ASSIGNMENT_ID).learnerId(STUDENT_ID)
                    .status(SubmissionStatus.SUBMITTED)
                    .submissionFileUrl("https://s3.amazonaws.com/bucket/submissions/my_solution.pdf")
                    .submittedAt(LocalDateTime.now()).build();

            when(assignmentRepository.findAll()).thenReturn(List.of(baseAssignment));
            when(submissionRepository.findByAssignmentIdAndLearnerId(ASSIGNMENT_ID, STUDENT_ID))
                    .thenReturn(Optional.of(sub));
            when(reviewRepository.findBySubmissionId(SUBMISSION_ID)).thenReturn(Optional.empty());

            List<StudentAssignmentResponse> result = studentAssignmentService.getMyAssignments(STUDENT_ID);

            assertThat(result.get(0).getSubmissionFileName()).isEqualTo("my_solution.pdf");
        }

        @Test
        @DisplayName("should return null submissionFileName when no submission exists")
        void extractFileName_noSubmission_nullFileName() {
            when(assignmentRepository.findAll()).thenReturn(List.of(baseAssignment));
            when(submissionRepository.findByAssignmentIdAndLearnerId(ASSIGNMENT_ID, STUDENT_ID))
                    .thenReturn(Optional.empty());

            List<StudentAssignmentResponse> result = studentAssignmentService.getMyAssignments(STUDENT_ID);

            assertThat(result.get(0).getSubmissionFileName()).isNull();
        }
    }

    // -------------------------------------------------------------------------
    // buildStudentAssignmentResponse — combined review fields
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("Review data mapping")
    class ReviewDataMapping {

        @Test
        @DisplayName("should populate review fields when review exists")
        void reviewFields_populated() {
            LocalDateTime reviewTime = LocalDateTime.of(2025, 6, 1, 10, 30);
            Submission sub = Submission.builder()
                    .submissionId(SUBMISSION_ID).assignmentId(ASSIGNMENT_ID).learnerId(STUDENT_ID)
                    .status(SubmissionStatus.REVIEWED).submittedAt(LocalDateTime.now()).build();
            Review review = Review.builder()
                    .reviewId("rev-001").submissionId(SUBMISSION_ID)
                    .marksAwarded(40f).resultStatus(ResultStatus.FAIL)
                    .feedback("Needs improvement").reviewedAt(reviewTime).build();

            when(assignmentRepository.findAll()).thenReturn(List.of(baseAssignment));
            when(submissionRepository.findByAssignmentIdAndLearnerId(ASSIGNMENT_ID, STUDENT_ID))
                    .thenReturn(Optional.of(sub));
            when(reviewRepository.findBySubmissionId(SUBMISSION_ID)).thenReturn(Optional.of(review));

            StudentAssignmentResponse response =
                    studentAssignmentService.getMyAssignments(STUDENT_ID).get(0);

            assertThat(response.getMarksAwarded()).isEqualTo(40f);
            assertThat(response.getResultStatus()).isEqualTo(ResultStatus.FAIL);
            assertThat(response.getFeedback()).isEqualTo("Needs improvement");
            assertThat(response.getReviewedAt()).isEqualTo(reviewTime.toString());
        }

        @Test
        @DisplayName("should default resultStatus to PENDING when no review")
        void reviewFields_noReview_defaultPending() {
            when(assignmentRepository.findAll()).thenReturn(List.of(baseAssignment));
            when(submissionRepository.findByAssignmentIdAndLearnerId(ASSIGNMENT_ID, STUDENT_ID))
                    .thenReturn(Optional.empty());

            StudentAssignmentResponse response =
                    studentAssignmentService.getMyAssignments(STUDENT_ID).get(0);

            assertThat(response.getResultStatus()).isEqualTo(ResultStatus.PENDING);
            assertThat(response.getFeedback()).isNull();
            assertThat(response.getMarksAwarded()).isNull();
            assertThat(response.getReviewedAt()).isNull();
        }
    }
}
