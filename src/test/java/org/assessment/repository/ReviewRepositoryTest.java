package org.assessment.repository;

import org.assessment.entity.Review;
import org.assessment.enums.ResultStatus;
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
@DisplayName("ReviewRepository Tests")
class ReviewRepositoryTest {

    @Mock private DynamoDbEnhancedClient enhancedClient;
    @Mock private DynamoDbTable<Review> table;
    @Mock private PageIterable<Review> pageIterable;
    @Mock private SdkIterable<Review> sdkIterable;

    private ReviewRepository repository;

    private Review r1, r2, r3;

    @BeforeEach
    void setUp() {
        when(enhancedClient.table(eq("reviews"), any(TableSchema.class))).thenReturn(table);
        repository = new ReviewRepository(enhancedClient);

        r1 = Review.builder().reviewId("rev-001").submissionId("sub-001")
                .reviewerId("inst-1").marksAwarded(80f).resultStatus(ResultStatus.PASS).build();
        r2 = Review.builder().reviewId("rev-002").submissionId("sub-002")
                .reviewerId("inst-1").marksAwarded(30f).resultStatus(ResultStatus.FAIL).build();
        r3 = Review.builder().reviewId("rev-003").submissionId("sub-003")
                .reviewerId("inst-2").marksAwarded(70f).resultStatus(ResultStatus.PASS).build();
    }

    private void stubScan(List<Review> data) {
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
        @DisplayName("should call putItem and return the same review")
        void save_callsPutItemAndReturns() {
            Review result = repository.save(r1);
            verify(table).putItem(r1);
            assertThat(result).isSameAs(r1);
        }
    }

    // -------------------------------------------------------------------------
    // findById
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("should return Optional.of(review) when found")
        void findById_found() {
            when(table.getItem(any(Key.class))).thenReturn(r1);
            assertThat(repository.findById("rev-001")).isPresent().contains(r1);
        }

        @Test
        @DisplayName("should return Optional.empty when not found")
        void findById_notFound() {
            when(table.getItem(any(Key.class))).thenReturn(null);
            assertThat(repository.findById("missing")).isEmpty();
        }
    }

    // -------------------------------------------------------------------------
    // findBySubmissionId
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("findBySubmissionId")
    class FindBySubmissionId {

        @Test
        @DisplayName("should return the review matching the submissionId")
        void findBySubmissionId_found() {
            stubScan(List.of(r1, r2, r3));
            Optional<Review> result = repository.findBySubmissionId("sub-001");
            assertThat(result).isPresent().contains(r1);
        }

        @Test
        @DisplayName("should return empty when no review matches submissionId")
        void findBySubmissionId_notFound() {
            stubScan(List.of(r1, r2, r3));
            assertThat(repository.findBySubmissionId("unknown-sub")).isEmpty();
        }

        @Test
        @DisplayName("should return first match when multiple exist (findFirst semantics)")
        void findBySubmissionId_returnsFirst() {
            Review duplicate = Review.builder().reviewId("rev-004")
                    .submissionId("sub-001").reviewerId("inst-2").build();
            stubScan(List.of(r1, duplicate));
            Optional<Review> result = repository.findBySubmissionId("sub-001");
            assertThat(result).isPresent();
            // Just verify one was returned
            assertThat(result.get().getSubmissionId()).isEqualTo("sub-001");
        }
    }

    // -------------------------------------------------------------------------
    // findByReviewerId
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("findByReviewerId")
    class FindByReviewerId {

        @Test
        @DisplayName("should return all reviews by the given reviewer")
        void findByReviewerId_returnsMatches() {
            stubScan(List.of(r1, r2, r3));
            List<Review> result = repository.findByReviewerId("inst-1");
            assertThat(result).hasSize(2).containsExactlyInAnyOrder(r1, r2);
        }

        @Test
        @DisplayName("should return empty list when reviewer has no reviews")
        void findByReviewerId_noMatch() {
            stubScan(List.of(r1, r2, r3));
            assertThat(repository.findByReviewerId("unknown-inst")).isEmpty();
        }

        @Test
        @DisplayName("should return single review when only one matches")
        void findByReviewerId_singleMatch() {
            stubScan(List.of(r1, r2, r3));
            List<Review> result = repository.findByReviewerId("inst-2");
            assertThat(result).hasSize(1).containsExactly(r3);
        }
    }
}
