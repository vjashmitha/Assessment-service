package org.assessment.repository;

import org.assessment.entity.Submission;
import org.assessment.enums.SubmissionStatus;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class SubmissionRepository {

    private final DynamoDbTable<Submission> table;

    public SubmissionRepository(DynamoDbTable<Submission> submissionTable) {
        this.table = submissionTable;
    }

    public Submission save(Submission submission) {
        submission.prePersist();
        table.putItem(submission);
        return submission;
    }

    public Optional<Submission> findById(String id) {
        Submission result = table.getItem(Key.builder().partitionValue(id).build());
        return Optional.ofNullable(result);
    }

    public List<Submission> findByAssignmentId(String assignmentId) {
        return table.scan().items().stream()
                .filter(s -> assignmentId.equals(s.getAssignmentId()))
                .collect(Collectors.toList());
    }

    public List<Submission> findByStudentId(String studentId) {
        return table.scan().items().stream()
                .filter(s -> studentId.equals(s.getStudentId()))
                .collect(Collectors.toList());
    }

    public Optional<Submission> findByAssignmentIdAndStudentId(String assignmentId, String studentId) {
        return table.scan().items().stream()
                .filter(s -> assignmentId.equals(s.getAssignmentId()) && studentId.equals(s.getStudentId()))
                .findFirst();
    }

    public List<Submission> findByAssignmentIdAndStatus(String assignmentId, SubmissionStatus status) {
        return table.scan().items().stream()
                .filter(s -> assignmentId.equals(s.getAssignmentId()) && status.name().equals(s.getStatus()))
                .collect(Collectors.toList());
    }

    public long countByAssignmentId(String assignmentId) {
        return table.scan().items().stream()
                .filter(s -> assignmentId.equals(s.getAssignmentId()))
                .count();
    }
}
