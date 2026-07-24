package org.assessment.controller;

import org.assessment.dto.response.StudentAssignmentResponse;
import org.assessment.enums.AssignmentStatus;
import org.assessment.enums.DifficultyLevel;
import org.assessment.enums.ResultStatus;
import org.assessment.enums.SubmissionStatus;
import org.assessment.exception.GlobalExceptionHandler;
import org.assessment.exception.ResourceNotFoundException;
import org.assessment.service.StudentAssignmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StudentAssignmentController Tests")
class StudentAssignmentControllerTest {

    @Mock
    private StudentAssignmentService studentAssignmentService;

    @InjectMocks
    private StudentAssignmentController studentAssignmentController;

    private MockMvc mockMvc;
    private StudentAssignmentResponse sampleResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(studentAssignmentController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        sampleResponse = StudentAssignmentResponse.builder()
                .assignmentId("assign-001")
                .title("Java REST API")
                .courseId("course-101")
                .courseName("Java Fundamentals")
                .totalMarks(50f)
                .passMarks(25f)
                .difficultyLevel(DifficultyLevel.INTERMEDIATE)
                .assignmentStatus(AssignmentStatus.PUBLISHED)
                .submissionStatus(SubmissionStatus.NOT_SUBMITTED)
                .resultStatus(ResultStatus.PENDING)
                .overdue(false)
                .build();
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/students/{studentId}/assignments
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("GET /api/v1/students/{studentId}/assignments")
    class GetMyAssignments {

        @Test
        @DisplayName("should return 200 with all assignments for student")
        void getMyAssignments_returnsList() throws Exception {
            when(studentAssignmentService.getMyAssignments("student-1"))
                    .thenReturn(List.of(sampleResponse));

            mockMvc.perform(get("/api/v1/students/student-1/assignments"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].assignmentId").value("assign-001"))
                    .andExpect(jsonPath("$[0].title").value("Java REST API"))
                    .andExpect(jsonPath("$[0].submissionStatus").value("NOT_SUBMITTED"));
        }

        @Test
        @DisplayName("should return 200 with empty list when student has no assignments")
        void getMyAssignments_empty() throws Exception {
            when(studentAssignmentService.getMyAssignments("student-1")).thenReturn(List.of());

            mockMvc.perform(get("/api/v1/students/student-1/assignments"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/students/{studentId}/assignments?status=SUBMITTED
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("GET /api/v1/students/{studentId}/assignments?status=...")
    class GetByStatus {

        @Test
        @DisplayName("should return 200 filtered by SUBMITTED status")
        void getByStatus_submitted() throws Exception {
            StudentAssignmentResponse submitted = StudentAssignmentResponse.builder()
                    .assignmentId("assign-001").title("Java REST API")
                    .submissionStatus(SubmissionStatus.SUBMITTED)
                    .resultStatus(ResultStatus.PENDING).build();

            when(studentAssignmentService.getMyAssignmentsByStatus("student-1", SubmissionStatus.SUBMITTED))
                    .thenReturn(List.of(submitted));

            mockMvc.perform(get("/api/v1/students/student-1/assignments")
                            .param("status", "SUBMITTED"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].submissionStatus").value("SUBMITTED"));
        }

        @Test
        @DisplayName("should return 200 filtered by REVIEWED status")
        void getByStatus_reviewed() throws Exception {
            StudentAssignmentResponse reviewed = StudentAssignmentResponse.builder()
                    .assignmentId("assign-001").title("Java REST API")
                    .submissionStatus(SubmissionStatus.REVIEWED)
                    .marksAwarded(44f).scorePercentage(88.0f)
                    .resultStatus(ResultStatus.PASS).build();

            when(studentAssignmentService.getMyAssignmentsByStatus("student-1", SubmissionStatus.REVIEWED))
                    .thenReturn(List.of(reviewed));

            mockMvc.perform(get("/api/v1/students/student-1/assignments")
                            .param("status", "REVIEWED"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].submissionStatus").value("REVIEWED"))
                    .andExpect(jsonPath("$[0].resultStatus").value("PASS"))
                    .andExpect(jsonPath("$[0].scorePercentage").value(88.0));
        }

        @Test
        @DisplayName("should return 200 with empty list when no assignments match status")
        void getByStatus_noMatch() throws Exception {
            when(studentAssignmentService.getMyAssignmentsByStatus("student-1", SubmissionStatus.REVIEWED))
                    .thenReturn(List.of());

            mockMvc.perform(get("/api/v1/students/student-1/assignments")
                            .param("status", "REVIEWED"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/students/{studentId}/assignments/{assignmentId}
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("GET /api/v1/students/{studentId}/assignments/{assignmentId}")
    class GetAssignmentDetail {

        @Test
        @DisplayName("should return 200 with full assignment detail")
        void getDetail_found() throws Exception {
            when(studentAssignmentService.getAssignmentDetail("assign-001", "student-1"))
                    .thenReturn(sampleResponse);

            mockMvc.perform(get("/api/v1/students/student-1/assignments/assign-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.assignmentId").value("assign-001"))
                    .andExpect(jsonPath("$.title").value("Java REST API"))
                    .andExpect(jsonPath("$.totalMarks").value(50.0))
                    .andExpect(jsonPath("$.passMarks").value(25.0))
                    .andExpect(jsonPath("$.difficultyLevel").value("INTERMEDIATE"))
                    .andExpect(jsonPath("$.overdue").value(false));
        }

        @Test
        @DisplayName("should return 404 when assignment not found")
        void getDetail_notFound() throws Exception {
            when(studentAssignmentService.getAssignmentDetail("missing", "student-1"))
                    .thenThrow(new ResourceNotFoundException("Assignment not found: missing"));

            mockMvc.perform(get("/api/v1/students/student-1/assignments/missing"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404));
        }

        @Test
        @DisplayName("should return detail with review fields when assignment is reviewed")
        void getDetail_withReview() throws Exception {
            StudentAssignmentResponse reviewed = StudentAssignmentResponse.builder()
                    .assignmentId("assign-001").title("Java REST API")
                    .totalMarks(50f).passMarks(25f)
                    .submissionStatus(SubmissionStatus.REVIEWED)
                    .marksAwarded(44f).scorePercentage(88.0f)
                    .resultStatus(ResultStatus.PASS).feedback("Well done")
                    .overdue(false).build();

            when(studentAssignmentService.getAssignmentDetail("assign-001", "student-1"))
                    .thenReturn(reviewed);

            mockMvc.perform(get("/api/v1/students/student-1/assignments/assign-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.marksAwarded").value(44.0))
                    .andExpect(jsonPath("$.scorePercentage").value(88.0))
                    .andExpect(jsonPath("$.resultStatus").value("PASS"))
                    .andExpect(jsonPath("$.feedback").value("Well done"));
        }
    }
}
