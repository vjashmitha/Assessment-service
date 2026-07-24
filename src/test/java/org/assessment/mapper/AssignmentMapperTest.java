package org.assessment.mapper;

import org.assessment.dto.request.CreateAssignmentRequest;
import org.assessment.dto.response.AssignmentResponse;
import org.assessment.entity.Assignment;
import org.assessment.enums.AssignmentStatus;
import org.assessment.enums.AssignmentType;
import org.assessment.enums.DifficultyLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AssignmentMapper Tests")
class AssignmentMapperTest {

    private AssignmentMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new AssignmentMapper();
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
            CreateAssignmentRequest request = new CreateAssignmentRequest();
            request.setTitle("Spring Boot Basics");
            request.setDescription("Learn Spring Boot");
            request.setCourseId("101");
            request.setTotalMarks(100f);
            request.setPassMarks(50f);
            request.setAssignmentType(AssignmentType.FILE_UPLOAD);
            request.setDifficultyLevel(DifficultyLevel.BEGINNER);
            request.setDueDate(LocalDate.of(2025, 12, 31));

            Assignment entity = mapper.toEntity(request, "instructor-1");

            assertThat(entity).isNotNull();
            assertThat(entity.getTitle()).isEqualTo("Spring Boot Basics");
            assertThat(entity.getDescription()).isEqualTo("Learn Spring Boot");
            assertThat(entity.getCourseId()).isEqualTo("101");
            assertThat(entity.getCreatedBy()).isEqualTo("instructor-1");
            assertThat(entity.getTotalMarks()).isEqualTo(100f);
            assertThat(entity.getPassMarks()).isEqualTo(50f);
            assertThat(entity.getAssignmentType()).isEqualTo(AssignmentType.FILE_UPLOAD);
            assertThat(entity.getDifficultyLevel()).isEqualTo(DifficultyLevel.BEGINNER);
            assertThat(entity.getDueDate()).isEqualTo(LocalDate.of(2025, 12, 31));
        }

        @Test
        @DisplayName("should automatically set status to PUBLISHED")
        void toEntity_setsPublishedStatus() {
            CreateAssignmentRequest request = new CreateAssignmentRequest();
            request.setTitle("Test");
            request.setCourseId("101");
            request.setTotalMarks(50f);
            request.setPassMarks(25f);

            Assignment entity = mapper.toEntity(request, "instructor-1");

            assertThat(entity.getStatus()).isEqualTo(AssignmentStatus.PUBLISHED);
        }

        @Test
        @DisplayName("should generate a non-null UUID as assignmentId")
        void toEntity_generatesAssignmentId() {
            CreateAssignmentRequest request = new CreateAssignmentRequest();
            request.setTitle("Test");
            request.setCourseId("101");
            request.setTotalMarks(50f);
            request.setPassMarks(25f);

            Assignment entity = mapper.toEntity(request, "instructor-1");

            assertThat(entity.getAssignmentId()).isNotNull();
            assertThat(entity.getAssignmentId()).isNotBlank();
        }

        @Test
        @DisplayName("should generate unique assignmentIds for each call")
        void toEntity_generatesUniqueIds() {
            CreateAssignmentRequest request = new CreateAssignmentRequest();
            request.setTitle("Test");
            request.setCourseId("101");
            request.setTotalMarks(50f);
            request.setPassMarks(25f);

            Assignment entity1 = mapper.toEntity(request, "instructor-1");
            Assignment entity2 = mapper.toEntity(request, "instructor-1");

            assertThat(entity1.getAssignmentId()).isNotEqualTo(entity2.getAssignmentId());
        }

        @Test
        @DisplayName("should set createdAt and updatedAt to current time")
        void toEntity_setsTimestamps() {
            LocalDateTime before = LocalDateTime.now().minusSeconds(1);

            CreateAssignmentRequest request = new CreateAssignmentRequest();
            request.setTitle("Test");
            request.setCourseId("101");
            request.setTotalMarks(50f);
            request.setPassMarks(25f);

            Assignment entity = mapper.toEntity(request, "instructor-1");

            assertThat(entity.getCreatedAt()).isAfterOrEqualTo(before);
            assertThat(entity.getUpdatedAt()).isAfterOrEqualTo(before);
        }

        @Test
        @DisplayName("should return null when request is null")
        void toEntity_nullRequest_returnsNull() {
            Assignment entity = mapper.toEntity(null, "instructor-1");

            assertThat(entity).isNull();
        }
    }

    // -------------------------------------------------------------------------
    // toResponse
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("toResponse")
    class ToResponse {

        @Test
        @DisplayName("should map all assignment fields to response")
        void toResponse_mapsAllFields() {
            LocalDateTime now = LocalDateTime.now();
            Assignment assignment = Assignment.builder()
                    .assignmentId("assign-001")
                    .title("Java Basics")
                    .description("Intro to Java")
                    .courseId("101")
                    .courseName("Java Course")
                    .totalMarks(100f)
                    .passMarks(50f)
                    .assignmentType(AssignmentType.PROJECT)
                    .difficultyLevel(DifficultyLevel.ADVANCED)
                    .status(AssignmentStatus.PUBLISHED)
                    .dueDate(LocalDate.of(2025, 12, 31))
                    .assignmentFileUrl("https://s3/file.pdf")
                    .createdBy("instructor-1")
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            AssignmentResponse response = mapper.toResponse(assignment);

            assertThat(response).isNotNull();
            assertThat(response.getAssignmentId()).isEqualTo("assign-001");
            assertThat(response.getTitle()).isEqualTo("Java Basics");
            assertThat(response.getDescription()).isEqualTo("Intro to Java");
            assertThat(response.getCourseId()).isEqualTo("101");
            assertThat(response.getCourseName()).isEqualTo("Java Course");
            assertThat(response.getTotalMarks()).isEqualTo(100f);
            assertThat(response.getPassMarks()).isEqualTo(50f);
            assertThat(response.getAssignmentType()).isEqualTo(AssignmentType.PROJECT);
            assertThat(response.getDifficultyLevel()).isEqualTo(DifficultyLevel.ADVANCED);
            assertThat(response.getStatus()).isEqualTo(AssignmentStatus.PUBLISHED);
            assertThat(response.getDueDate()).isEqualTo("2025-12-31");
            assertThat(response.getAssignmentFileUrl()).isEqualTo("https://s3/file.pdf");
            assertThat(response.getCreatedBy()).isEqualTo("instructor-1");
            assertThat(response.getCreatedAt()).isNotNull();
            assertThat(response.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("should return null dueDate string when assignment dueDate is null")
        void toResponse_nullDueDate() {
            Assignment assignment = Assignment.builder()
                    .assignmentId("assign-001").title("Test")
                    .dueDate(null).build();

            AssignmentResponse response = mapper.toResponse(assignment);

            assertThat(response.getDueDate()).isNull();
        }

        @Test
        @DisplayName("should return null createdAt string when assignment createdAt is null")
        void toResponse_nullCreatedAt() {
            Assignment assignment = Assignment.builder()
                    .assignmentId("assign-001").title("Test")
                    .createdAt(null).updatedAt(null).build();

            AssignmentResponse response = mapper.toResponse(assignment);

            assertThat(response.getCreatedAt()).isNull();
            assertThat(response.getUpdatedAt()).isNull();
        }

        @Test
        @DisplayName("should return null when assignment is null")
        void toResponse_nullAssignment_returnsNull() {
            AssignmentResponse response = mapper.toResponse(null);

            assertThat(response).isNull();
        }
    }
}
