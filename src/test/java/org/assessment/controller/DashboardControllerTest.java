package org.assessment.controller;

import org.assessment.dto.response.DashboardResponse;
import org.assessment.exception.GlobalExceptionHandler;
import org.assessment.service.DashboardService;
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

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DashboardController Tests")
class DashboardControllerTest {

    @Mock
    private DashboardService dashboardService;

    @InjectMocks
    private DashboardController dashboardController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(dashboardController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/dashboard/instructors/{instructorId}
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("GET /api/v1/dashboard/instructors/{instructorId}")
    class InstructorDashboard {

        @Test
        @DisplayName("should return 200 with instructor dashboard stats")
        void getInstructorDashboard_returnsStats() throws Exception {
            DashboardResponse response = DashboardResponse.builder()
                    .totalAssignments(5L)
                    .totalSubmissions(20L)
                    .pendingReviews(8L)
                    .gradedSubmissions(12L)
                    .build();

            when(dashboardService.getInstructorDashboard("instructor-1")).thenReturn(response);

            mockMvc.perform(get("/api/v1/dashboard/instructors/instructor-1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalAssignments").value(5))
                    .andExpect(jsonPath("$.totalSubmissions").value(20))
                    .andExpect(jsonPath("$.pendingReviews").value(8))
                    .andExpect(jsonPath("$.gradedSubmissions").value(12));
        }

        @Test
        @DisplayName("should return 200 with all-zero dashboard for new instructor")
        void getInstructorDashboard_allZeros() throws Exception {
            DashboardResponse response = DashboardResponse.builder()
                    .totalAssignments(0L).totalSubmissions(0L)
                    .pendingReviews(0L).gradedSubmissions(0L)
                    .build();

            when(dashboardService.getInstructorDashboard("instructor-new")).thenReturn(response);

            mockMvc.perform(get("/api/v1/dashboard/instructors/instructor-new"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalAssignments").value(0));
        }
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/dashboard/students/{studentId}
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("GET /api/v1/dashboard/students/{studentId}")
    class StudentDashboard {

        @Test
        @DisplayName("should return 200 with student dashboard stats")
        void getStudentDashboard_returnsStats() throws Exception {
            DashboardResponse response = DashboardResponse.builder()
                    .totalAssignments(10L)
                    .totalSubmissions(6L)
                    .submittedCount(6L)
                    .reviewedCount(4L)
                    .pendingOverdue(1L)
                    .passCount(3L)
                    .failCount(1L)
                    .averageScore(72.5f)
                    .build();

            when(dashboardService.getStudentDashboard("student-1")).thenReturn(response);

            mockMvc.perform(get("/api/v1/dashboard/students/student-1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalAssignments").value(10))
                    .andExpect(jsonPath("$.submittedCount").value(6))
                    .andExpect(jsonPath("$.reviewedCount").value(4))
                    .andExpect(jsonPath("$.pendingOverdue").value(1))
                    .andExpect(jsonPath("$.passCount").value(3))
                    .andExpect(jsonPath("$.failCount").value(1))
                    .andExpect(jsonPath("$.averageScore").value(72.5));
        }

        @Test
        @DisplayName("should return 200 with zero averageScore when no submissions graded")
        void getStudentDashboard_noGrades() throws Exception {
            DashboardResponse response = DashboardResponse.builder()
                    .totalAssignments(3L).totalSubmissions(0L)
                    .averageScore(0.0f).build();

            when(dashboardService.getStudentDashboard("student-new")).thenReturn(response);

            mockMvc.perform(get("/api/v1/dashboard/students/student-new"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.averageScore").value(0.0));
        }
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/dashboard/courses/{courseId}
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("GET /api/v1/dashboard/courses/{courseId}")
    class CourseDashboard {

        @Test
        @DisplayName("should return 200 with course assignment stats")
        void getCourseStats_returnsStats() throws Exception {
            DashboardResponse response = DashboardResponse.builder()
                    .totalAssignments(3L)
                    .totalSubmissions(45L)
                    .build();

            when(dashboardService.getCourseAssignmentStats("course-101")).thenReturn(response);

            mockMvc.perform(get("/api/v1/dashboard/courses/course-101"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalAssignments").value(3))
                    .andExpect(jsonPath("$.totalSubmissions").value(45));
        }

        @Test
        @DisplayName("should return 200 with zeros when course has no assignments")
        void getCourseStats_empty() throws Exception {
            DashboardResponse response = DashboardResponse.builder()
                    .totalAssignments(0L).totalSubmissions(0L).build();

            when(dashboardService.getCourseAssignmentStats("course-empty")).thenReturn(response);

            mockMvc.perform(get("/api/v1/dashboard/courses/course-empty"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalAssignments").value(0));
        }
    }
}
