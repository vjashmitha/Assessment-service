package org.assessment.service.impl;

import org.assessment.client.EnrollmentServiceClient;
import org.assessment.dto.request.SubmitAssignmentRequest;
import org.assessment.dto.response.SubmissionResponse;
import org.assessment.entity.Assignment;
import org.assessment.entity.Review;
import org.assessment.entity.Submission;
import org.assessment.enums.AssignmentStatus;
import org.assessment.enums.AssignmentType;
import org.assessment.enums.ResultStatus;
import org.assessment.enums.SubmissionStatus;
import org.assessment.exception.ResourceNotFoundException;
import org.assessment.exception.ValidationException;
import org.assessment.mapper.SubmissionMapper;
import org.assessment.repository.AssignmentRepository;
import org.assessment.repository.ReviewRepository;
import org.assessment.repository.SubmissionRepository;
import org.assessment.storage.S3Service;
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
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubmissionServiceImpl Tests")
class SubmissionServiceImplTest {

    @Mock private SubmissionRepository submissionRepository;
    @Mock private AssignmentRepository assignmentRepository;
    @Mock private ReviewRepository reviewRepository;
    @Mock private SubmissionMapper submissionMapper;
    @Mock private S3Service s3Service;
    @Mock private EnrollmentServiceClient enrollmentServiceClient;

    @InjectMocks
    private SubmissionServiceImpl submissionService;

    private static final String STUDENT_ID   = "42";      // must be numeric — service calls Long.valueOf(studentId)
    private static final String ASSIGNMENT_ID = "assign-001";
    private static final String COURSE_ID    = "101";
    private static final String SUBMISSION_ID = "sub-001";

    private Assignment publishedAssignment;
    private Submission sampleSubmission;
    private Review     sampleReview;
    private SubmitAssignmentRequest submitRequest;

    @BeforeEach
    void setUp() {
        publishedAssignment = Assignment.builder()
                .assignmentId(ASSIGNMENT_ID)
                .courseId(COURSE_ID)
                .status(AssignmentStatus.PUBLISHED)
                .assignmentType(AssignmentType.FILE_UPLOAD)
                .totalMarks(100f)
                .passMarks(50f)
                .dueDate(LocalDate.now().plusDays(7))
                .build();

        sampleSubmission = Submission.builder()
                .submissionId(SUBMISSION_ID)
                .assignmentId(ASSIGNMENT_ID)
                .learnerId(STUDENT_ID)
                .status(SubmissionStatus.SUBMITTED)
                .submittedAt(LocalDateTime.now())
                .build();

        sampleReview = Review.builder()
                .reviewId("rev-001")
                .submissionId(SUBMISSION_ID)
                .marksAwarded(75f)
                .resultStatus(ResultStatus.PASS)
                .build();

        submitRequest = new SubmitAssignmentRequest();
        submitRequest.setAssignmentId(ASSIGNMENT_ID);
    }

    // -------------------------------------------------------------------------
    // submitAssignment
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("submitAssignment")
    class SubmitAssignment {

        @Test
        @DisplayName("should submit successfully without file (non-FILE_UPLOAD type)")
        void submit_success_noFile() {
            publishedAssignment.setAssignmentType(AssignmentType.PROJECT);

            SubmissionResponse expectedResponse = SubmissionResponse.builder()
                    .id(SUBMISSION_ID).studentId(STUDENT_ID).status(SubmissionStatus.SUBMITTED).build();

            try (MockedStatic<CommonUtil> util = mockStatic(CommonUtil.class)) {
                util.when(CommonUtil::extractUserIdFromRequest).thenReturn(STUDENT_ID);
                when(assignmentRepository.findById(ASSIGNMENT_ID)).thenReturn(Optional.of(publishedAssignment));
                when(enrollmentServiceClient.isEnrolled(Long.valueOf(STUDENT_ID), Long.valueOf(COURSE_ID)))
                        .thenReturn(true);
                when(submissionRepository.findByAssignmentIdAndLearnerId(ASSIGNMENT_ID, STUDENT_ID))
                        .thenReturn(Optional.empty());
                when(submissionMapper.toEntity(submitRequest, STUDENT_ID)).thenReturn(sampleSubmission);
                when(submissionRepository.save(sampleSubmission)).thenReturn(sampleSubmission);
                when(submissionMapper.toResponse(sampleSubmission)).thenReturn(expectedResponse);

                SubmissionResponse result = submissionService.submitAssignment(submitRequest, null);

                assertThat(result).isNotNull();
                assertThat(result.getId()).isEqualTo(SUBMISSION_ID);
                verify(s3Service, never()).uploadFile(any(), anyString());
            }
        }

        @Test
        @DisplayName("should skip file upload for FILE_UPLOAD type when file is null")
        void submit_nullFile_fileUploadType_skipsUpload() {
            SubmissionResponse expectedResponse = SubmissionResponse.builder()
                    .id(SUBMISSION_ID).status(SubmissionStatus.SUBMITTED).build();

            try (MockedStatic<CommonUtil> util = mockStatic(CommonUtil.class)) {
                util.when(CommonUtil::extractUserIdFromRequest).thenReturn(STUDENT_ID);
                when(assignmentRepository.findById(ASSIGNMENT_ID)).thenReturn(Optional.of(publishedAssignment));
                when(enrollmentServiceClient.isEnrolled(Long.valueOf(STUDENT_ID), Long.valueOf(COURSE_ID)))
                        .thenReturn(true);
                when(submissionRepository.findByAssignmentIdAndLearnerId(ASSIGNMENT_ID, STUDENT_ID))
                        .thenReturn(Optional.empty());
                when(submissionMapper.toEntity(submitRequest, STUDENT_ID)).thenReturn(sampleSubmission);
                when(submissionRepository.save(sampleSubmission)).thenReturn(sampleSubmission);
                when(submissionMapper.toResponse(sampleSubmission)).thenReturn(expectedResponse);

                SubmissionResponse result = submissionService.submitAssignment(submitRequest, null);

                assertThat(result).isNotNull();
                verify(s3Service, never()).uploadFile(any(), anyString());
                assertThat(sampleSubmission.getSubmissionFileUrl()).isNull();
            }
        }

        @Test
        @DisplayName("should upload file for FILE_UPLOAD type when file is provided")
        void submit_success_withFile_fileUploadType() {
            MultipartFile mockFile = mock(MultipartFile.class);
            when(mockFile.isEmpty()).thenReturn(false);

            SubmissionResponse expectedResponse = SubmissionResponse.builder()
                    .id(SUBMISSION_ID).fileUrl("https://s3/submissions/file.pdf").build();

            try (MockedStatic<CommonUtil> util = mockStatic(CommonUtil.class)) {
                util.when(CommonUtil::extractUserIdFromRequest).thenReturn(STUDENT_ID);
                when(assignmentRepository.findById(ASSIGNMENT_ID)).thenReturn(Optional.of(publishedAssignment));
                when(enrollmentServiceClient.isEnrolled(Long.valueOf(STUDENT_ID), Long.valueOf(COURSE_ID)))
                        .thenReturn(true);
                when(submissionRepository.findByAssignmentIdAndLearnerId(ASSIGNMENT_ID, STUDENT_ID))
                        .thenReturn(Optional.empty());
                when(submissionMapper.toEntity(submitRequest, STUDENT_ID)).thenReturn(sampleSubmission);
                when(s3Service.uploadFile(mockFile, "submissions")).thenReturn("https://s3/submissions/file.pdf");
                when(submissionRepository.save(sampleSubmission)).thenReturn(sampleSubmission);
                when(submissionMapper.toResponse(sampleSubmission)).thenReturn(expectedResponse);

                SubmissionResponse result = submissionService.submitAssignment(submitRequest, mockFile);

                assertThat(result).isNotNull();
                verify(s3Service).uploadFile(mockFile, "submissions");
                assertThat(sampleSubmission.getSubmissionFileUrl()).isEqualTo("https://s3/submissions/file.pdf");
            }
        }

        @Test
        @DisplayName("should not upload file when file is empty")
        void submit_emptyFile_skipsUpload() {
            MultipartFile emptyFile = mock(MultipartFile.class);
            when(emptyFile.isEmpty()).thenReturn(true);

            SubmissionResponse expectedResponse = SubmissionResponse.builder().id(SUBMISSION_ID).build();

            try (MockedStatic<CommonUtil> util = mockStatic(CommonUtil.class)) {
                util.when(CommonUtil::extractUserIdFromRequest).thenReturn(STUDENT_ID);
                when(assignmentRepository.findById(ASSIGNMENT_ID)).thenReturn(Optional.of(publishedAssignment));
                when(enrollmentServiceClient.isEnrolled(Long.valueOf(STUDENT_ID), Long.valueOf(COURSE_ID)))
                        .thenReturn(true);
                when(submissionRepository.findByAssignmentIdAndLearnerId(ASSIGNMENT_ID, STUDENT_ID))
                        .thenReturn(Optional.empty());
                when(submissionMapper.toEntity(submitRequest, STUDENT_ID)).thenReturn(sampleSubmission);
                when(submissionRepository.save(sampleSubmission)).thenReturn(sampleSubmission);
                when(submissionMapper.toResponse(sampleSubmission)).thenReturn(expectedResponse);

                submissionService.submitAssignment(submitRequest, emptyFile);

                verify(s3Service, never()).uploadFile(any(), anyString());
            }
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when assignment not found")
        void submit_assignmentNotFound() {
            try (MockedStatic<CommonUtil> util = mockStatic(CommonUtil.class)) {
                util.when(CommonUtil::extractUserIdFromRequest).thenReturn(STUDENT_ID);
                when(assignmentRepository.findById(ASSIGNMENT_ID)).thenReturn(Optional.empty());

                assertThatThrownBy(() -> submissionService.submitAssignment(submitRequest, null))
                        .isInstanceOf(ResourceNotFoundException.class)
                        .hasMessageContaining("Assignment not found with id: " + ASSIGNMENT_ID);
            }
        }

        @Test
        @DisplayName("should throw ValidationException when assignment is not PUBLISHED")
        void submit_assignmentNotPublished() {
            publishedAssignment.setStatus(AssignmentStatus.DRAFT);

            try (MockedStatic<CommonUtil> util = mockStatic(CommonUtil.class)) {
                util.when(CommonUtil::extractUserIdFromRequest).thenReturn(STUDENT_ID);
                when(assignmentRepository.findById(ASSIGNMENT_ID)).thenReturn(Optional.of(publishedAssignment));

                assertThatThrownBy(() -> submissionService.submitAssignment(submitRequest, null))
                        .isInstanceOf(ValidationException.class)
                        .hasMessageContaining("Assignment is not open for submissions");
            }
        }

        @Test
        @DisplayName("should throw ValidationException when assignment is CLOSED")
        void submit_assignmentClosed() {
            publishedAssignment.setStatus(AssignmentStatus.CLOSED);

            try (MockedStatic<CommonUtil> util = mockStatic(CommonUtil.class)) {
                util.when(CommonUtil::extractUserIdFromRequest).thenReturn(STUDENT_ID);
                when(assignmentRepository.findById(ASSIGNMENT_ID)).thenReturn(Optional.of(publishedAssignment));

                assertThatThrownBy(() -> submissionService.submitAssignment(submitRequest, null))
                        .isInstanceOf(ValidationException.class)
                        .hasMessageContaining("Assignment is not open for submissions");
            }
        }

        @Test
        @DisplayName("should throw ValidationException when student is not enrolled")
        void submit_studentNotEnrolled() {
            try (MockedStatic<CommonUtil> util = mockStatic(CommonUtil.class)) {
                util.when(CommonUtil::extractUserIdFromRequest).thenReturn(STUDENT_ID);
                when(assignmentRepository.findById(ASSIGNMENT_ID)).thenReturn(Optional.of(publishedAssignment));
                when(enrollmentServiceClient.isEnrolled(Long.valueOf(STUDENT_ID), Long.valueOf(COURSE_ID)))
                        .thenReturn(false);

                assertThatThrownBy(() -> submissionService.submitAssignment(submitRequest, null))
                        .isInstanceOf(ValidationException.class)
                        .hasMessageContaining("Student is not enrolled in course");
            }
        }

        @Test
        @DisplayName("should throw ValidationException when enrollment service returns null")
        void submit_enrollmentReturnsNull() {
            try (MockedStatic<CommonUtil> util = mockStatic(CommonUtil.class)) {
                util.when(CommonUtil::extractUserIdFromRequest).thenReturn(STUDENT_ID);
                when(assignmentRepository.findById(ASSIGNMENT_ID)).thenReturn(Optional.of(publishedAssignment));
                when(enrollmentServiceClient.isEnrolled(Long.valueOf(STUDENT_ID), Long.valueOf(COURSE_ID)))
                        .thenReturn(null);

                assertThatThrownBy(() -> submissionService.submitAssignment(submitRequest, null))
                        .isInstanceOf(ValidationException.class)
                        .hasMessageContaining("Student is not enrolled");
            }
        }

        @Test
        @DisplayName("should throw ValidationException when student or course ID is not numeric")
        void submit_invalidIdFormat() {
            publishedAssignment.setCourseId("not-a-number");

            try (MockedStatic<CommonUtil> util = mockStatic(CommonUtil.class)) {
                util.when(CommonUtil::extractUserIdFromRequest).thenReturn(STUDENT_ID);
                when(assignmentRepository.findById(ASSIGNMENT_ID)).thenReturn(Optional.of(publishedAssignment));

                assertThatThrownBy(() -> submissionService.submitAssignment(submitRequest, null))
                        .isInstanceOf(ValidationException.class)
                        .hasMessageContaining("Invalid student or course ID format");
            }
        }

        @Test
        @DisplayName("should throw ValidationException when enrollment Feign call fails")
        void submit_enrollmentFeignFails() {
            try (MockedStatic<CommonUtil> util = mockStatic(CommonUtil.class)) {
                util.when(CommonUtil::extractUserIdFromRequest).thenReturn(STUDENT_ID);
                when(assignmentRepository.findById(ASSIGNMENT_ID)).thenReturn(Optional.of(publishedAssignment));
                when(enrollmentServiceClient.isEnrolled(any(), any()))
                        .thenThrow(new RuntimeException("Enrollment service down"));

                assertThatThrownBy(() -> submissionService.submitAssignment(submitRequest, null))
                        .isInstanceOf(ValidationException.class)
                        .hasMessageContaining("Failed to validate student enrollment");
            }
        }

        @Test
        @DisplayName("should throw ValidationException when student already submitted")
        void submit_duplicateSubmission() {
            try (MockedStatic<CommonUtil> util = mockStatic(CommonUtil.class)) {
                util.when(CommonUtil::extractUserIdFromRequest).thenReturn(STUDENT_ID);
                when(assignmentRepository.findById(ASSIGNMENT_ID)).thenReturn(Optional.of(publishedAssignment));
                when(enrollmentServiceClient.isEnrolled(Long.valueOf(STUDENT_ID), Long.valueOf(COURSE_ID)))
                        .thenReturn(true);
                when(submissionRepository.findByAssignmentIdAndLearnerId(ASSIGNMENT_ID, STUDENT_ID))
                        .thenReturn(Optional.of(sampleSubmission));

                assertThatThrownBy(() -> submissionService.submitAssignment(submitRequest, null))
                        .isInstanceOf(ValidationException.class)
                        .hasMessageContaining("Already submitted for this assignment");
            }
        }
    }

    // -------------------------------------------------------------------------
    // getSubmissionById
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("getSubmissionById")
    class GetSubmissionById {

        @Test
        @DisplayName("should return submission with review when review exists")
        void getSubmissionById_withReview() {
            SubmissionResponse expectedResponse = SubmissionResponse.builder()
                    .id(SUBMISSION_ID).obtainedMarks(75f).resultStatus(ResultStatus.PASS).build();

            when(submissionRepository.findById(SUBMISSION_ID)).thenReturn(Optional.of(sampleSubmission));
            when(reviewRepository.findBySubmissionId(SUBMISSION_ID)).thenReturn(Optional.of(sampleReview));
            when(submissionMapper.toResponse(sampleSubmission, sampleReview)).thenReturn(expectedResponse);

            SubmissionResponse result = submissionService.getSubmissionById(SUBMISSION_ID);

            assertThat(result).isNotNull();
            assertThat(result.getObtainedMarks()).isEqualTo(75f);
            assertThat(result.getResultStatus()).isEqualTo(ResultStatus.PASS);
        }

        @Test
        @DisplayName("should return submission with null review when no review exists")
        void getSubmissionById_withoutReview() {
            SubmissionResponse expectedResponse = SubmissionResponse.builder()
                    .id(SUBMISSION_ID).status(SubmissionStatus.SUBMITTED).build();

            when(submissionRepository.findById(SUBMISSION_ID)).thenReturn(Optional.of(sampleSubmission));
            when(reviewRepository.findBySubmissionId(SUBMISSION_ID)).thenReturn(Optional.empty());
            when(submissionMapper.toResponse(sampleSubmission, null)).thenReturn(expectedResponse);

            SubmissionResponse result = submissionService.getSubmissionById(SUBMISSION_ID);

            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo(SubmissionStatus.SUBMITTED);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when submission not found")
        void getSubmissionById_notFound() {
            when(submissionRepository.findById("missing")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> submissionService.getSubmissionById("missing"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Submission not found with id: missing");
        }
    }

    // -------------------------------------------------------------------------
    // getSubmissionsByAssignment
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("getSubmissionsByAssignment")
    class GetSubmissionsByAssignment {

        @Test
        @DisplayName("should return list of submissions for an assignment, each enriched with review")
        void getSubmissionsByAssignment_withReviews() {
            SubmissionResponse resp = SubmissionResponse.builder()
                    .id(SUBMISSION_ID).obtainedMarks(75f).build();

            when(submissionRepository.findByAssignmentId(ASSIGNMENT_ID))
                    .thenReturn(List.of(sampleSubmission));
            when(reviewRepository.findBySubmissionId(SUBMISSION_ID))
                    .thenReturn(Optional.of(sampleReview));
            when(submissionMapper.toResponse(sampleSubmission, sampleReview)).thenReturn(resp);

            List<SubmissionResponse> result = submissionService.getSubmissionsByAssignment(ASSIGNMENT_ID);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getObtainedMarks()).isEqualTo(75f);
        }

        @Test
        @DisplayName("should return empty list when no submissions exist for assignment")
        void getSubmissionsByAssignment_empty() {
            when(submissionRepository.findByAssignmentId(ASSIGNMENT_ID)).thenReturn(List.of());

            List<SubmissionResponse> result = submissionService.getSubmissionsByAssignment(ASSIGNMENT_ID);

            assertThat(result).isEmpty();
        }
    }

    // -------------------------------------------------------------------------
    // getSubmissionsByStudent
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("getSubmissionsByStudent")
    class GetSubmissionsByStudent {

        @Test
        @DisplayName("should return all submissions for a student")
        void getSubmissionsByStudent_returnsList() {
            SubmissionResponse resp = SubmissionResponse.builder().id(SUBMISSION_ID).studentId(STUDENT_ID).build();

            when(submissionRepository.findByLearnerId(STUDENT_ID)).thenReturn(List.of(sampleSubmission));
            when(reviewRepository.findBySubmissionId(SUBMISSION_ID)).thenReturn(Optional.empty());
            when(submissionMapper.toResponse(sampleSubmission, null)).thenReturn(resp);

            List<SubmissionResponse> result = submissionService.getSubmissionsByStudent(STUDENT_ID);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStudentId()).isEqualTo(STUDENT_ID);
        }

        @Test
        @DisplayName("should return empty list when student has no submissions")
        void getSubmissionsByStudent_empty() {
            when(submissionRepository.findByLearnerId(STUDENT_ID)).thenReturn(List.of());

            List<SubmissionResponse> result = submissionService.getSubmissionsByStudent(STUDENT_ID);

            assertThat(result).isEmpty();
        }
    }

    // -------------------------------------------------------------------------
    // getSubmissionByAssignmentAndStudent
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("getSubmissionByAssignmentAndStudent")
    class GetSubmissionByAssignmentAndStudent {

        @Test
        @DisplayName("should return submission for specific assignment and student")
        void getByAssignmentAndStudent_found() {
            SubmissionResponse resp = SubmissionResponse.builder()
                    .id(SUBMISSION_ID).studentId(STUDENT_ID).build();

            when(submissionRepository.findByAssignmentIdAndLearnerId(ASSIGNMENT_ID, STUDENT_ID))
                    .thenReturn(Optional.of(sampleSubmission));
            when(reviewRepository.findBySubmissionId(SUBMISSION_ID)).thenReturn(Optional.empty());
            when(submissionMapper.toResponse(sampleSubmission, null)).thenReturn(resp);

            SubmissionResponse result =
                    submissionService.getSubmissionByAssignmentAndStudent(ASSIGNMENT_ID, STUDENT_ID);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(SUBMISSION_ID);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when submission not found")
        void getByAssignmentAndStudent_notFound() {
            when(submissionRepository.findByAssignmentIdAndLearnerId(ASSIGNMENT_ID, STUDENT_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    submissionService.getSubmissionByAssignmentAndStudent(ASSIGNMENT_ID, STUDENT_ID))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Submission not found");
        }
    }
}
