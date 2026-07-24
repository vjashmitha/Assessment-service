package org.assessment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assessment.dto.request.CreateAssignmentRequest;
import org.assessment.dto.request.UpdateAssignmentRequest;
import org.assessment.dto.response.AssignmentResponse;
import org.assessment.enums.AssignmentStatus;
import org.assessment.enums.AssignmentType;
import org.assessment.enums.DifficultyLevel;
import org.assessment.exception.GlobalExceptionHandler;
import org.assessment.exception.ResourceNotFoundException;
import org.assessment.service.AssignmentService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AssignmentController Tests")
class AssignmentControllerTest {

    @Mock
    private AssignmentService assignmentService;

    @InjectMocks
    private AssignmentController assignmentController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private AssignmentResponse sampleResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(assignmentController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();

        sampleResponse = AssignmentResponse.builder()
                .assignmentId("assign-001")
                .title("Java Basics")
                .courseId("101")
                .totalMarks(100f)
                .passMarks(50f)
                .status(AssignmentStatus.PUBLISHED)
                .assignmentType(AssignmentType.FILE_UPLOAD)
                .difficultyLevel(DifficultyLevel.BEGINNER)
                .build();
    }

    // -------------------------------------------------------------------------
    // POST /api/v1/assignments  — createAssignment
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("POST /api/v1/assignments")
    class CreateAssignment {

        @Test
        @DisplayName("should return 201 with response body when assignment created")
        void create_success() throws Exception {
            CreateAssignmentRequest request = new CreateAssignmentRequest();
            request.setTitle("Java Basics");
            request.setCourseId("101");
            request.setTotalMarks(100f);
            request.setPassMarks(50f);

            MockMultipartFile requestPart = new MockMultipartFile(
                    "request", "", MediaType.APPLICATION_JSON_VALUE,
                    objectMapper.writeValueAsBytes(request));

            when(assignmentService.createAssignment(any(), isNull())).thenReturn(sampleResponse);

            mockMvc.perform(multipart("/api/v1/assignments")
                            .file(requestPart))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.assignmentId").value("assign-001"))
                    .andExpect(jsonPath("$.title").value("Java Basics"));
        }

        @Test
        @DisplayName("should return 201 with file when file is provided")
        void create_withFile() throws Exception {
            CreateAssignmentRequest request = new CreateAssignmentRequest();
            request.setTitle("Java Basics");
            request.setCourseId("101");
            request.setTotalMarks(100f);
            request.setPassMarks(50f);

            MockMultipartFile requestPart = new MockMultipartFile(
                    "request", "", MediaType.APPLICATION_JSON_VALUE,
                    objectMapper.writeValueAsBytes(request));
            MockMultipartFile filePart = new MockMultipartFile(
                    "file", "assignment.pdf", MediaType.APPLICATION_PDF_VALUE,
                    "pdf content".getBytes());

            when(assignmentService.createAssignment(any(), any())).thenReturn(sampleResponse);

            mockMvc.perform(multipart("/api/v1/assignments")
                            .file(requestPart)
                            .file(filePart))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.assignmentId").value("assign-001"));
        }
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/assignments/{id}  — getAssignmentById
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("GET /api/v1/assignments/{id}")
    class GetAssignmentById {

        @Test
        @DisplayName("should return 200 with assignment when found")
        void getById_found() throws Exception {
            when(assignmentService.getAssignmentById("assign-001")).thenReturn(sampleResponse);

            mockMvc.perform(get("/api/v1/assignments/assign-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.assignmentId").value("assign-001"))
                    .andExpect(jsonPath("$.title").value("Java Basics"));
        }

        @Test
        @DisplayName("should return 404 when assignment not found")
        void getById_notFound() throws Exception {
            when(assignmentService.getAssignmentById("missing"))
                    .thenThrow(new ResourceNotFoundException("Assignment not found with id: missing"));

            mockMvc.perform(get("/api/v1/assignments/missing"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.message").value(containsString("Assignment not found")));
        }
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/assignments  — getAllAssignments
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("GET /api/v1/assignments")
    class GetAllAssignments {

        @Test
        @DisplayName("should return 200 with list of assignments")
        void getAll_returnsList() throws Exception {
            when(assignmentService.getAllAssignments()).thenReturn(List.of(sampleResponse));

            mockMvc.perform(get("/api/v1/assignments"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].assignmentId").value("assign-001"));
        }

        @Test
        @DisplayName("should return 200 with empty list when no assignments")
        void getAll_emptyList() throws Exception {
            when(assignmentService.getAllAssignments()).thenReturn(List.of());

            mockMvc.perform(get("/api/v1/assignments"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/assignments/courses/{courseId}
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("GET /api/v1/assignments/courses/{courseId}")
    class GetByCourse {

        @Test
        @DisplayName("should return 200 with assignments for a course")
        void getByCourse_returnsList() throws Exception {
            when(assignmentService.getAssignmentsByCourse("101")).thenReturn(List.of(sampleResponse));

            mockMvc.perform(get("/api/v1/assignments/courses/101"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].courseId").value("101"));
        }
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/assignments/instructors/{instructorId}
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("GET /api/v1/assignments/instructors/{instructorId}")
    class GetByInstructor {

        @Test
        @DisplayName("should return 200 with assignments by instructor")
        void getByInstructor_returnsList() throws Exception {
            when(assignmentService.getAssignmentsByInstructor("instructor-1"))
                    .thenReturn(List.of(sampleResponse));

            mockMvc.perform(get("/api/v1/assignments/instructors/instructor-1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)));
        }
    }

    // -------------------------------------------------------------------------
    // PUT /api/v1/assignments/{id}  — updateAssignment
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("PUT /api/v1/assignments/{id}")
    class UpdateAssignment {

        @Test
        @DisplayName("should return 200 with updated assignment")
        void update_success() throws Exception {
            UpdateAssignmentRequest request = new UpdateAssignmentRequest();
            request.setTitle("Advanced Java");

            MockMultipartFile requestPart = new MockMultipartFile(
                    "request", "", MediaType.APPLICATION_JSON_VALUE,
                    objectMapper.writeValueAsBytes(request));

            when(assignmentService.updateAssignment(eq("assign-001"), any(), isNull()))
                    .thenReturn(sampleResponse);

            mockMvc.perform(multipart("/api/v1/assignments/assign-001")
                            .file(requestPart)
                            .with(req -> { req.setMethod("PUT"); return req; }))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.assignmentId").value("assign-001"));
        }

        @Test
        @DisplayName("should return 404 when assignment not found during update")
        void update_notFound() throws Exception {
            UpdateAssignmentRequest request = new UpdateAssignmentRequest();
            MockMultipartFile requestPart = new MockMultipartFile(
                    "request", "", MediaType.APPLICATION_JSON_VALUE,
                    objectMapper.writeValueAsBytes(request));

            when(assignmentService.updateAssignment(eq("missing"), any(), any()))
                    .thenThrow(new ResourceNotFoundException("Assignment not found with id: missing"));

            mockMvc.perform(multipart("/api/v1/assignments/missing")
                            .file(requestPart)
                            .with(req -> { req.setMethod("PUT"); return req; }))
                    .andExpect(status().isNotFound());
        }
    }

    // -------------------------------------------------------------------------
    // DELETE /api/v1/assignments/{id}
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("DELETE /api/v1/assignments/{id}")
    class DeleteAssignment {

        @Test
        @DisplayName("should return 204 when assignment deleted")
        void delete_success() throws Exception {
            doNothing().when(assignmentService).deleteAssignment("assign-001");

            mockMvc.perform(delete("/api/v1/assignments/assign-001"))
                    .andExpect(status().isNoContent());

            verify(assignmentService).deleteAssignment("assign-001");
        }

        @Test
        @DisplayName("should return 404 when assignment not found during delete")
        void delete_notFound() throws Exception {
            doThrow(new ResourceNotFoundException("Assignment not found with id: missing"))
                    .when(assignmentService).deleteAssignment("missing");

            mockMvc.perform(delete("/api/v1/assignments/missing"))
                    .andExpect(status().isNotFound());
        }
    }
}
