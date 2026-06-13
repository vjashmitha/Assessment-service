package org.assessment.repository;

import org.assessment.entity.AssignmentReview;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class AssignmentReviewRepository {

    private final DynamoDbTable<AssignmentReview> table;

    public AssignmentReviewRepository(DynamoDbTable<AssignmentReview> assignmentReviewTable) {
        this.table = assignmentReviewTable;
    }

    public AssignmentReview save(AssignmentReview review) {
        review.prePersist();
        table.putItem(review);
        return review;
    }

    public Optional<AssignmentReview> findById(String id) {
        AssignmentReview result = table.getItem(Key.builder().partitionValue(id).build());
        return Optional.ofNullable(result);
    }

    public Optional<AssignmentReview> findBySubmissionId(String submissionId) {
        return table.scan().items().stream()
                .filter(r -> submissionId.equals(r.getSubmissionId()))
                .findFirst();
    }

    public List<AssignmentReview> findByReviewerId(String reviewerId) {
        return table.scan().items().stream()
                .filter(r -> reviewerId.equals(r.getReviewerId()))
                .collect(Collectors.toList());
    }
}
