package org.assessment.repository;

import org.assessment.entity.Submission;
import org.assessment.enums.SubmissionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubmissionRepository Tests")
class SubmissionRepositoryTest {

    @Mock private DynamoDbEnhancedClient enhancedClient;
    @Mock private DynamoDbTable<Submission> table;
    @Mock private PageIterable<Submission> pageIterable;
    @Mock private SdkIterable<Submission> sdkIterable;

    private SubmissionRepository repository;

    private Submission s1, s2, s3, s4;

    @BeforeEach
    void setUp() {
        when(enhancedClient.table(eq("submissions"), any(TableSchema.class))).thenReturn(table);
        repository = new SubmissionRepository(enhancedClient);

        s1 = Submission.builder().submissionId("s-001").assignmentId("a-1").learnerId("l-1")
                .status(SubmissionStatus.SUBMITTED).build();
        s2 = Submission.builder().submissionId("s-002").assignmentId("a-1").learnerId("l-2")
                .status(SubmissionStatus.REVIEWED).build();
        s3 = Submission.builder().submissionId("s-003").assignmentId("a-2").learnerId("l-1")
                .status(SubmissionStatus.SUBMITTED).build();
        s4 = Submission.builder().submissionId("s-004").assignmentId("a-1").learnerId("l-3")
                .status(SubmissionStatus.UNDER_REVIEW).build();
    }

    private void stubScan(List<Submission> data) {
        when(table.scan()).thenReturn(pageIterable);
        when(pageIterable.items()).thenReturn(sdkIterable);
        when(sdkIterable.stream()).thenReturn(data.stream());
    }

    // -------------------------------------------------------------------------
    // save
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("should call putItem and return the same submission")
        void save_callsPutItemAndReturns() {
            Submission result = repository.save(s1);
            verify(table).putItem(s1);
            assertThat(result).isSameAs(s1);
        }
    }

    // -------------------------------------------------------------------------
    // findById
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("should return Optional.of(submission) when found")
        void findById_found() {
            when(table.getItem(any(Key.class))).thenReturn(s1);
            assertThat(repository.findById("s-001")).isPresent().contains(s1);
        }

        @Test
        @DisplayName("should return Optional.empty when not found")
        void findById_notFound() {
            when(table.getItem(any(Key.class))).thenReturn(null);
            assertThat(repository.findById("missing")).isEmpty();
        }
    }

    // -------------------------------------------------------------------------
    // findByAssignmentId
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("findByAssignmentId")
    class FindByAssignmentId {

        @Test
        @DisplayName("should return all submissions for an assignment")
        void findByAssignmentId_returnsMatches() {
            stubScan(List.of(s1, s2, s3, s4));
            List<Submission> result = repository.findByAssignmentId("a-1");
            assertThat(result).hasSize(3).containsExactlyInAnyOrder(s1, s2, s4);
        }

        @Test
        @DisplayName("should return empty list when no submissions for assignment")
        void findByAssignmentId_noMatch() {
            stubScan(List.of(s1, s2, s3, s4));
            assertThat(repository.findByAssignmentId("no-assign")).isEmpty();
        }
    }

    // -------------------------------------------------------------------------
    // findByLearnerId
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("findByLearnerId")
    class FindByLearnerId {

        @Test
        @DisplayName("should return all submissions by a learner")
        void findByLearnerId_returnsMatches() {
            stubScan(List.of(s1, s2, s3, s4));
            List<Submission> result = repository.findByLearnerId("l-1");
            assertThat(result).hasSize(2).containsExactlyInAnyOrder(s1, s3);
        }

        @Test
        @DisplayName("should return empty list when learner has no submissions")
        void findByLearnerId_noMatch() {
            stubScan(List.of(s1, s2, s3, s4));
            assertThat(repository.findByLearnerId("unknown-learner")).isEmpty();
        }
    }

    // -------------------------------------------------------------------------
    // findByAssignmentIdAndLearnerId
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("findByAssignmentIdAndLearnerId")
    class FindByAssignmentIdAndLearnerId {

        @Test
        @DisplayName("should return matching submission when both assignment and learner match")
        void findByAssignmentAndLearner_found() {
            stubScan(List.of(s1, s2, s3, s4));
            Optional<Submission> result = repository.findByAssignmentIdAndLearnerId("a-1", "l-1");
            assertThat(result).isPresent().contains(s1);
        }

        @Test
        @DisplayName("should return empty when assignment matches but learner does not")
        void findByAssignmentAndLearner_learnerMismatch() {
            stubScan(List.of(s1, s2, s3, s4));
            Optional<Submission> result = repository.findByAssignmentIdAndLearnerId("a-1", "unknown");
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should return empty when neither matches")
        void findByAssignmentAndLearner_noMatch() {
            stubScan(List.of(s1, s2, s3, s4));
            Optional<Submission> result = repository.findByAssignmentIdAndLearnerId("x", "y");
            assertThat(result).isEmpty();
        }
    }

    // -------------------------------------------------------------------------
    // findByAssignmentIdAndStatus
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("findByAssignmentIdAndStatus")
    class FindByAssignmentIdAndStatus {

        @Test
        @DisplayName("should return submissions matching assignment and status")
        void findByAssignmentAndStatus_returnsMatches() {
            stubScan(List.of(s1, s2, s3, s4));
            List<Submission> result =
                    repository.findByAssignmentIdAndStatus("a-1", SubmissionStatus.SUBMITTED);
            assertThat(result).hasSize(1).containsExactly(s1);
        }

        @Test
        @DisplayName("should return empty list when no submissions match")
        void findByAssignmentAndStatus_noMatch() {
            stubScan(List.of(s1, s2, s3, s4));
            List<Submission> result =
                    repository.findByAssignmentIdAndStatus("a-1", SubmissionStatus.NOT_SUBMITTED);
            assertThat(result).isEmpty();
        }
    }

    // -------------------------------------------------------------------------
    // countByAssignmentId
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("countByAssignmentId")
    class CountByAssignmentId {

        @Test
        @DisplayName("should return count of submissions for an assignment")
        void count_returnsCorrectCount() {
            stubScan(List.of(s1, s2, s3, s4));
            long count = repository.countByAssignmentId("a-1");
            assertThat(count).isEqualTo(3L);
        }

        @Test
        @DisplayName("should return 0 when no submissions for assignment")
        void count_returnsZeroWhenNoMatch() {
            stubScan(List.of(s1, s2, s3, s4));
            long count = repository.countByAssignmentId("no-assign");
            assertThat(count).isZero();
        }
    }
}
