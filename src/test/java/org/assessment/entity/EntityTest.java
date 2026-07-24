package org.assessment.entity;

import org.assessment.enums.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Entity Classes Tests")
class EntityTest {

    // -------------------------------------------------------------------------
    // Assignment entity
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("Assignment")
    class AssignmentTest {

        @Test
        @DisplayName("should build with all fields using builder")
        void builder_setsAllFields() {
            LocalDate due = LocalDate.of(2025, 12, 31);
            LocalDateTime now = LocalDateTime.now();

            Assignment a = Assignment.builder()
                    .assignmentId("a-001")
                    .title("Spring Boot Basics")
                    .description("Intro assignment")
                    .courseId("c-101")
                    .courseName("Spring Course")
                    .totalMarks(100f)
                    .passMarks(50f)
                    .assignmentType(AssignmentType.FILE_UPLOAD)
                    .difficultyLevel(DifficultyLevel.BEGINNER)
                    .status(AssignmentStatus.PUBLISHED)
                    .dueDate(due)
                    .assignmentFileUrl("https://s3/file.pdf")
                    .createdBy("instructor-1")
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            assertThat(a.getAssignmentId()).isEqualTo("a-001");
            assertThat(a.getTitle()).isEqualTo("Spring Boot Basics");
            assertThat(a.getDescription()).isEqualTo("Intro assignment");
            assertThat(a.getCourseId()).isEqualTo("c-101");
            assertThat(a.getCourseName()).isEqualTo("Spring Course");
            assertThat(a.getTotalMarks()).isEqualTo(100f);
            assertThat(a.getPassMarks()).isEqualTo(50f);
            assertThat(a.getAssignmentType()).isEqualTo(AssignmentType.FILE_UPLOAD);
            assertThat(a.getDifficultyLevel()).isEqualTo(DifficultyLevel.BEGINNER);
            assertThat(a.getStatus()).isEqualTo(AssignmentStatus.PUBLISHED);
            assertThat(a.getDueDate()).isEqualTo(due);
            assertThat(a.getAssignmentFileUrl()).isEqualTo("https://s3/file.pdf");
            assertThat(a.getCreatedBy()).isEqualTo("instructor-1");
            assertThat(a.getCreatedAt()).isEqualTo(now);
            assertThat(a.getUpdatedAt()).isEqualTo(now);
        }

        @Test
        @DisplayName("no-args constructor should create empty object")
        void noArgsConstructor() {
            Assignment a = new Assignment();
            assertThat(a.getAssignmentId()).isNull();
            assertThat(a.getTitle()).isNull();
        }

        @Test
        @DisplayName("setters should update fields")
        void setters_updateFields() {
            Assignment a = new Assignment();
            a.setAssignmentId("a-002");
            a.setTitle("Advanced Java");
            a.setStatus(AssignmentStatus.CLOSED);
            a.setTotalMarks(200f);

            assertThat(a.getAssignmentId()).isEqualTo("a-002");
            assertThat(a.getTitle()).isEqualTo("Advanced Java");
            assertThat(a.getStatus()).isEqualTo(AssignmentStatus.CLOSED);
            assertThat(a.getTotalMarks()).isEqualTo(200f);
        }

        @Test
        @DisplayName("all-args constructor should set all fields")
        void allArgsConstructor() {
            LocalDateTime now = LocalDateTime.now();
            Assignment a = new Assignment("id", "title", "desc", "cId", "cName",
                    100f, 50f, AssignmentType.QUIZ, DifficultyLevel.ADVANCED,
                    AssignmentStatus.DRAFT, LocalDate.now(), "url", "creator", now, now);

            assertThat(a.getAssignmentId()).isEqualTo("id");
            assertThat(a.getTitle()).isEqualTo("title");
            assertThat(a.getAssignmentType()).isEqualTo(AssignmentType.QUIZ);
        }
    }

    // -------------------------------------------------------------------------
    // Submission entity
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("Submission")
    class SubmissionTest {

        @Test
        @DisplayName("should build with all fields using builder")
        void builder_setsAllFields() {
            LocalDateTime now = LocalDateTime.now();

            Submission s = Submission.builder()
                    .submissionId("sub-001")
                    .assignmentId("a-001")
                    .learnerId("learner-1")
                    .submissionFileUrl("https://s3/sub.pdf")
                    .status(SubmissionStatus.SUBMITTED)
                    .submittedAt(now)
                    .build();

            assertThat(s.getSubmissionId()).isEqualTo("sub-001");
            assertThat(s.getAssignmentId()).isEqualTo("a-001");
            assertThat(s.getLearnerId()).isEqualTo("learner-1");
            assertThat(s.getSubmissionFileUrl()).isEqualTo("https://s3/sub.pdf");
            assertThat(s.getStatus()).isEqualTo(SubmissionStatus.SUBMITTED);
            assertThat(s.getSubmittedAt()).isEqualTo(now);
        }

        @Test
        @DisplayName("no-args constructor should create empty object")
        void noArgsConstructor() {
            Submission s = new Submission();
            assertThat(s.getSubmissionId()).isNull();
            assertThat(s.getStatus()).isNull();
        }

        @Test
        @DisplayName("setters should update fields")
        void setters_updateFields() {
            Submission s = new Submission();
            s.setSubmissionId("sub-002");
            s.setStatus(SubmissionStatus.REVIEWED);
            s.setLearnerId("learner-42");

            assertThat(s.getSubmissionId()).isEqualTo("sub-002");
            assertThat(s.getStatus()).isEqualTo(SubmissionStatus.REVIEWED);
            assertThat(s.getLearnerId()).isEqualTo("learner-42");
        }

        @Test
        @DisplayName("all-args constructor should set all fields")
        void allArgsConstructor() {
            LocalDateTime now = LocalDateTime.now();
            Submission s = new Submission("s1", "a1", "l1", "url", SubmissionStatus.UNDER_REVIEW, now);

            assertThat(s.getSubmissionId()).isEqualTo("s1");
            assertThat(s.getStatus()).isEqualTo(SubmissionStatus.UNDER_REVIEW);
        }
    }

    // -------------------------------------------------------------------------
    // Review entity
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("Review")
    class ReviewEntityTest {

        @Test
        @DisplayName("should build with all fields using builder")
        void builder_setsAllFields() {
            LocalDateTime now = LocalDateTime.now();

            Review r = Review.builder()
                    .reviewId("rev-001")
                    .submissionId("sub-001")
                    .reviewerId("instructor-1")
                    .marksAwarded(85f)
                    .feedback("Excellent work")
                    .resultStatus(ResultStatus.PASS)
                    .reviewedAt(now)
                    .build();

            assertThat(r.getReviewId()).isEqualTo("rev-001");
            assertThat(r.getSubmissionId()).isEqualTo("sub-001");
            assertThat(r.getReviewerId()).isEqualTo("instructor-1");
            assertThat(r.getMarksAwarded()).isEqualTo(85f);
            assertThat(r.getFeedback()).isEqualTo("Excellent work");
            assertThat(r.getResultStatus()).isEqualTo(ResultStatus.PASS);
            assertThat(r.getReviewedAt()).isEqualTo(now);
        }

        @Test
        @DisplayName("no-args constructor should create empty object")
        void noArgsConstructor() {
            Review r = new Review();
            assertThat(r.getReviewId()).isNull();
            assertThat(r.getResultStatus()).isNull();
        }

        @Test
        @DisplayName("setters should update fields")
        void setters_updateFields() {
            Review r = new Review();
            r.setReviewId("rev-002");
            r.setMarksAwarded(30f);
            r.setResultStatus(ResultStatus.FAIL);
            r.setFeedback("Needs improvement");

            assertThat(r.getReviewId()).isEqualTo("rev-002");
            assertThat(r.getMarksAwarded()).isEqualTo(30f);
            assertThat(r.getResultStatus()).isEqualTo(ResultStatus.FAIL);
            assertThat(r.getFeedback()).isEqualTo("Needs improvement");
        }

        @Test
        @DisplayName("all-args constructor should set all fields")
        void allArgsConstructor() {
            LocalDateTime now = LocalDateTime.now();
            Review r = new Review("r1", "s1", "rev1", 70f, "ok", ResultStatus.PASS, now);

            assertThat(r.getReviewId()).isEqualTo("r1");
            assertThat(r.getResultStatus()).isEqualTo(ResultStatus.PASS);
        }
    }
}
