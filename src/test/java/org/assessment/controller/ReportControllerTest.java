package org.assessment.controller;

import org.assessment.dto.response.ReportResponse;
import org.assessment.dto.response.SubmissionResponse;
import org.assessment.enums.AssignmentStatus;
import org.assessment.exception.GlobalExceptionHandler;
import org.assessment.exception.ResourceNotFoundException;
import org.assessment.service.ReportService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReportController Tests")
class ReportControllerTest {

    @Mock
    private ReportService reportService;

    @InjectMocks
    private ReportController reportController;

    private MockMvc mockMvc;
    private ReportResponse sampleReport;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(reportController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        sampleReport = ReportResponse.builder()
                .assignmentId("assign-001")
                .assignmentTitle("Java Basics")
                .status(AssignmentStatus.PUBLISHED)
                .totalStudents(10L)
                .submittedCount(8L)
                .gradedCount(6L)
                .pendingCount(2L)
                .passCount(5L)
                .failCount(1L)
                .averageScore(74.5f)
                .highestScore(95f)
                .lowestScore(45f)
                .submissions(List.of())
                .build();
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/reports/assignments/{assignmentId}
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("GET /api/v1/reports/assignments/{assignmentId}")
    class GetAssignmentReport {

        @Test
        @DisplayName("should return 200 with full report")
        void getReport_returnsReport() throws Exception {
            when(reportService.getAssignmentReport("assign-001")).thenReturn(sampleReport);

            mockMvc.perform(get("/api/v1/reports/assignments/assign-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.assignmentId").value("assign-001"))
                    .andExpect(jsonPath("$.assignmentTitle").value("Java Basics"))
                    .andExpect(jsonPath("$.totalStudents").value(10))
                    .andExpect(jsonPath("$.submittedCount").value(8))
                    .andExpect(jsonPath("$.gradedCount").value(6))
                    .andExpect(jsonPath("$.passCount").value(5))
                    .andExpect(jsonPath("$.failCount").value(1))
                    .andExpect(jsonPath("$.averageScore").value(74.5))
                    .andExpect(jsonPath("$.highestScore").value(95.0))
                    .andExpect(jsonPath("$.lowestScore").value(45.0));
        }

        @Test
        @DisplayName("should return 404 when assignment not found")
        void getReport_notFound() throws Exception {
            when(reportService.getAssignmentReport("missing"))
                    .thenThrow(new ResourceNotFoundException("Assignment not found with id: missing"));

            mockMvc.perform(get("/api/v1/reports/assignments/missing"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404));
        }
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/reports/courses/{courseId}
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("GET /api/v1/reports/courses/{courseId}")
    class GetCourseReport {

        @Test
        @DisplayName("should return 200 with list of reports")
        void getCourseReport_returnsList() throws Exception {
            when(reportService.getCourseReport("course-101")).thenReturn(List.of(sampleReport));

            mockMvc.perform(get("/api/v1/reports/courses/course-101"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].assignmentId").value("assign-001"));
        }

        @Test
        @DisplayName("should return 200 with empty list when course has no assignments")
        void getCourseReport_empty() throws Exception {
            when(reportService.getCourseReport("course-empty")).thenReturn(List.of());

            mockMvc.perform(get("/api/v1/reports/courses/course-empty"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/reports/assignments/{assignmentId}/export
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("GET /api/v1/reports/assignments/{assignmentId}/export")
    class ExportCsv {

        @Test
        @DisplayName("should return 200 with CSV content-type and attachment header")
        void exportCsv_returnsFile() throws Exception {
            String csvContent = "SubmissionId,StudentId,Status,ObtainedMarks,ResultStatus,SubmittedAt\n"
                    + "sub-001,42,REVIEWED,80.0,PASS,2025-01-15T10:00:00\n";
            when(reportService.exportReportAsCsv("assign-001"))
                    .thenReturn(csvContent.getBytes());

            mockMvc.perform(get("/api/v1/reports/assignments/assign-001/export"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Disposition",
                            containsString("attachment; filename=report-assign-001.csv")))
                    .andExpect(content().contentType("text/csv"))
                    .andExpect(content().string(containsString("SubmissionId")));
        }

        @Test
        @DisplayName("should return 200 with only header row when no submissions")
        void exportCsv_noSubmissions_onlyHeader() throws Exception {
            String csvContent = "SubmissionId,StudentId,Status,ObtainedMarks,ResultStatus,SubmittedAt\n";
            when(reportService.exportReportAsCsv("assign-001"))
                    .thenReturn(csvContent.getBytes());

            mockMvc.perform(get("/api/v1/reports/assignments/assign-001/export"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(not(containsString("sub-001"))));
        }

        @Test
        @DisplayName("should return 404 when assignment not found for export")
        void exportCsv_notFound() throws Exception {
            when(reportService.exportReportAsCsv("missing"))
                    .thenThrow(new ResourceNotFoundException("Assignment not found with id: missing"));

            mockMvc.perform(get("/api/v1/reports/assignments/missing/export"))
                    .andExpect(status().isNotFound());
        }
    }
}
