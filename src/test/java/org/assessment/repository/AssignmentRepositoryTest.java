package org.assessment.repository;

import org.assessment.entity.Assignment;
import org.assessment.enums.AssignmentStatus;
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

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AssignmentRepository Tests")
class AssignmentRepositoryTest {

    @Mock private DynamoDbEnhancedClient enhancedClient;
    @Mock private DynamoDbTable<Assignment> table;
    @Mock private PageIterable<Assignment> pageIterable;
    @Mock private SdkIterable<Assignment> sdkIterable;

    private AssignmentRepository repository;

    private Assignment a1, a2, a3;

    @BeforeEach
    void setUp() {
        when(enhancedClient.table(eq("assignments"), any(TableSchema.class))).thenReturn(table);
        repository = new AssignmentRepository(enhancedClient);

        a1 = Assignment.builder().assignmentId("a-001").courseId("c-1").createdBy("inst-1").build();
        a2 = Assignment.builder().assignmentId("a-002").courseId("c-1").createdBy("inst-2").build();
        a3 = Assignment.builder().assignmentId("a-003").courseId("c-2").createdBy("inst-1").build();
    }

    private void stubScan(List<Assignment> data) {
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
        @DisplayName("should call putItem and return the same assignment")
        void save_callsPutItemAndReturns() {
            Assignment result = repository.save(a1);
            verify(table).putItem(a1);
            assertThat(result).isSameAs(a1);
        }

        @Test
        @DisplayName("should return the saved assignment unchanged")
        void save_returnsSameObject() {
            a1.setTitle("Test Assignment");
            Assignment result = repository.save(a1);
            assertThat(result.getTitle()).isEqualTo("Test Assignment");
        }
    }

    // -------------------------------------------------------------------------
    // findById
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("should return Optional.of(assignment) when found")
        void findById_found() {
            when(table.getItem(any(Key.class))).thenReturn(a1);
            Optional<Assignment> result = repository.findById("a-001");
            assertThat(result).isPresent().contains(a1);
        }

        @Test
        @DisplayName("should return Optional.empty when DynamoDB returns null")
        void findById_notFound() {
            when(table.getItem(any(Key.class))).thenReturn(null);
            Optional<Assignment> result = repository.findById("missing");
            assertThat(result).isEmpty();
        }
    }

    // -------------------------------------------------------------------------
    // findAll
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("findAll")
    class FindAll {

        @Test
        @DisplayName("should return all assignments from table scan")
        void findAll_returnsList() {
            stubScan(List.of(a1, a2, a3));
            List<Assignment> result = repository.findAll();
            assertThat(result).hasSize(3).containsExactlyInAnyOrder(a1, a2, a3);
        }

        @Test
        @DisplayName("should return empty list when table is empty")
        void findAll_empty() {
            stubScan(List.of());
            List<Assignment> result = repository.findAll();
            assertThat(result).isEmpty();
        }
    }

    // -------------------------------------------------------------------------
    // deleteById
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("deleteById")
    class DeleteById {

        @Test
        @DisplayName("should call deleteItem on the table")
        void deleteById_callsDeleteItem() {
            repository.deleteById("a-001");
            verify(table).deleteItem(any(Key.class));
        }
    }

    // -------------------------------------------------------------------------
    // findByCourseId
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("findByCourseId")
    class FindByCourseId {

        @Test
        @DisplayName("should return only assignments matching courseId")
        void findByCourseId_filtersCorrectly() {
            stubScan(List.of(a1, a2, a3));
            List<Assignment> result = repository.findByCourseId("c-1");
            assertThat(result).hasSize(2).containsExactlyInAnyOrder(a1, a2);
        }

        @Test
        @DisplayName("should return empty list when no assignments match courseId")
        void findByCourseId_noMatch() {
            stubScan(List.of(a1, a2, a3));
            List<Assignment> result = repository.findByCourseId("no-course");
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should return single assignment when only one matches")
        void findByCourseId_singleMatch() {
            stubScan(List.of(a1, a2, a3));
            List<Assignment> result = repository.findByCourseId("c-2");
            assertThat(result).hasSize(1).containsExactly(a3);
        }
    }

    // -------------------------------------------------------------------------
    // findByCreatedBy
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("findByCreatedBy")
    class FindByCreatedBy {

        @Test
        @DisplayName("should return assignments created by the instructor")
        void findByCreatedBy_filtersCorrectly() {
            stubScan(List.of(a1, a2, a3));
            List<Assignment> result = repository.findByCreatedBy("inst-1");
            assertThat(result).hasSize(2).containsExactlyInAnyOrder(a1, a3);
        }

        @Test
        @DisplayName("should return empty list when instructor has no assignments")
        void findByCreatedBy_noMatch() {
            stubScan(List.of(a1, a2, a3));
            List<Assignment> result = repository.findByCreatedBy("unknown");
            assertThat(result).isEmpty();
        }
    }
}
