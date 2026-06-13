package org.assessment.repository;

import org.assessment.entity.Assignment;
import org.assessment.enums.AssignmentStatus;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class AssignmentRepository {

    private final DynamoDbTable<Assignment> table;

    public AssignmentRepository(DynamoDbTable<Assignment> assignmentTable) {
        this.table = assignmentTable;
    }

    public Assignment save(Assignment assignment) {
        assignment.prePersist();
        table.putItem(assignment);
        return assignment;
    }

    public Optional<Assignment> findById(String id) {
        Assignment result = table.getItem(Key.builder().partitionValue(id).build());
        return Optional.ofNullable(result);
    }

    public List<Assignment> findAll() {
        return table.scan(ScanEnhancedRequest.builder().build())
                .items().stream().collect(Collectors.toList());
    }

    public List<Assignment> findByCourseId(String courseId) {
        return table.scan().items().stream()
                .filter(a -> courseId.equals(a.getCourseId()))
                .collect(Collectors.toList());
    }

    public List<Assignment> findByInstructorId(String instructorId) {
        return table.scan().items().stream()
                .filter(a -> instructorId.equals(a.getInstructorId()))
                .collect(Collectors.toList());
    }

    public List<Assignment> findByCourseIdAndStatus(String courseId, AssignmentStatus status) {
        return table.scan().items().stream()
                .filter(a -> courseId.equals(a.getCourseId()) && status.name().equals(a.getStatus()))
                .collect(Collectors.toList());
    }

    public void delete(Assignment assignment) {
        table.deleteItem(assignment);
    }

    public long count() {
        return table.scan().items().stream().count();
    }
}
