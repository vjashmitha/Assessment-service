package org.assessment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assessment.dto.request.SubmitAssignmentRequest;
import org.assessment.dto.response.SubmissionResponse;
import org.assessment.enums.ResultStatus;
import org.assessment.enums.SubmissionStatus;
import org.assessment.exception.GlobalExceptionHandler;
import org.assessment.exception.ResourceNotFoundException;
import org.assessment.exception.ValidationException;
import org.assessment.service.SubmissionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubmissionController Tests")
class SubmissionControllerTest {

    @Mock
    private SubmissionService submissionService;

    @InjectMocks
    private SubmissionController submissionController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private SubmissionResponse sampleResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(submissionController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();

        sampleResponse = SubmissionResponse.builder()
                .id("sub-001")
                .assignmentId("assign-001")
                .studentId("42")
                .status(SubmissionStatus.SUBMITTED)
                .build();
    }

    // -------------------------------------------------------------------------
    // POST /api/v1/submissions
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("POST /api/v1/submissions")
    class SubmitAssignment {

        @Test
        @DisplayName("should return 201 when submission created without file")
        void submit_noFile_returns201() throws Exception {
            SubmitAssignmentRequest request = new SubmitAssignmentRequest();
            request.setAssignmentId("assign-001");

            MockMultipartFile requestPart = new MockMultipartFile(
                    "request", "", MediaType.APPLICATION_JSON_VALUE,
                    objectMapper.writeValueAsBytes(request));

            when(submissionService.submitAssignment(any(), isNull())).thenReturn(sampleResponse);

            mockMvc.perform(multipart("/api/v1/submissions")
                            .file(requestPart))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value("sub-001"))
                    .andExpect(jsonPath("$.assignmentId").value("assign-001"));
        }

        @Test
        @DisplayName("should return 201 when submission created with file")
        void submit_withFile_returns201() throws Exception {
            SubmitAssignmentRequest request = new SubmitAssignmentRequest();
            request.setAssignmentId("assign-001");

            MockMultipartFile requestPart = new MockMultipartFile(
                    "request", "", MediaType.APPLICATION_JSON_VALUE,
                    objectMapper.writeValueAsBytes(request));
            MockMultipartFile filePart = new MockMultipartFile(
                    "file", "solution.pdf", MediaType.APPLICATION_PDF_VALUE,
                    "file content".getBytes());

            when(submissionService.submitAssignment(any(), any())).thenReturn(sampleResponse);

            mockMvc.perform(multipart("/api/v1/submissions")
                            .file(requestPart)
                            .file(filePart))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value("sub-001"));
        }

        @Test
        @DisplayName("should return 400 when assignment is not open for submission")
        void submit_assignmentClosed_returns400() throws Exception {
            SubmitAssignmentRequest request = new SubmitAssignmentRequest();
            request.setAssignmentId("assign-001");

            MockMultipartFile requestPart = new MockMultipartFile(
                    "request", "", MediaType.APPLICATION_JSON_VALUE,
                    objectMapper.writeValueAsBytes(request));

            when(submissionService.submitAssignment(any(), any()))
                    .thenThrow(new ValidationException("Assignment is not open for submissions"));

            mockMvc.perform(multipart("/api/v1/submissions")
                            .file(requestPart))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Assignment is not open for submissions"));
        }
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/submissions/{id}
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("GET /api/v1/submissions/{id}")
    class GetSubmissionById {

        @Test
        @DisplayName("should return 200 with submission when found")
        void getById_found() throws Exception {
            SubmissionResponse reviewed = SubmissionResponse.builder()
                    .id("sub-001").assignmentId("assign-001").studentId("42")
                    .status(SubmissionStatus.REVIEWED).obtainedMarks(80f)
                    .resultStatus(ResultStatus.PASS).build();

            when(submissionService.getSubmissionById("sub-001")).thenReturn(reviewed);

            mockMvc.perform(get("/api/v1/submissions/sub-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value("sub-001"))
                    .andExpect(jsonPath("$.obtainedMarks").value(80.0))
                    .andExpect(jsonPath("$.resultStatus").value("PASS"));
        }

        @Test
        @DisplayName("should return 404 when submission not found")
        void getById_notFound() throws Exception {
            when(submissionService.getSubmissionById("missing"))
                    .thenThrow(new ResourceNotFoundException("Submission not found with id: missing"));

            mockMvc.perform(get("/api/v1/submissions/missing"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404));
        }
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/submissions/assignments/{assignmentId}
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("GET /api/v1/submissions/assignments/{assignmentId}")
    class GetByAssignment {

        @Test
        @DisplayName("should return 200 with list of submissions")
        void getByAssignment_returnsList() throws Exception {
            when(submissionService.getSubmissionsByAssignment("assign-001"))
                    .thenReturn(List.of(sampleResponse));

            mockMvc.perform(get("/api/v1/submissions/assignments/assign-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id").value("sub-001"));
        }

        @Test
        @DisplayName("should return 200 with empty list when no submissions")
        void getByAssignment_empty() throws Exception {
            when(submissionService.getSubmissionsByAssignment("assign-001")).thenReturn(List.of());

            mockMvc.perform(get("/api/v1/submissions/assignments/assign-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/submissions/students/{studentId}
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("GET /api/v1/submissions/students/{studentId}")
    class GetByStudent {

        @Test
        @DisplayName("should return 200 with submissions for student")
        void getByStudent_returnsList() throws Exception {
            when(submissionService.getSubmissionsByStudent("42")).thenReturn(List.of(sampleResponse));

            mockMvc.perform(get("/api/v1/submissions/students/42"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].studentId").value("42"));
        }
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/submissions/assignments/{assignmentId}/students/{studentId}
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("GET /api/v1/submissions/assignments/{assignmentId}/students/{studentId}")
    class GetByAssignmentAndStudent {

        @Test
        @DisplayName("should return 200 with specific submission")
        void getByAssignmentAndStudent_found() throws Exception {
            when(submissionService.getSubmissionByAssignmentAndStudent("assign-001", "42"))
                    .thenReturn(sampleResponse);

            mockMvc.perform(get("/api/v1/submissions/assignments/assign-001/students/42"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value("sub-001"));
        }

        @Test
        @DisplayName("should return 404 when submission not found")
        void getByAssignmentAndStudent_notFound() throws Exception {
            when(submissionService.getSubmissionByAssignmentAndStudent("assign-001", "42"))
                    .thenThrow(new ResourceNotFoundException("Submission not found"));

            mockMvc.perform(get("/api/v1/submissions/assignments/assign-001/students/42"))
                    .andExpect(status().isNotFound());
        }
    }
}
