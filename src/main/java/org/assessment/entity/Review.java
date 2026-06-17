package org.assessment.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.time.LocalDateTime;

import org.assessment.enums.ResultStatus;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamoDbBean
// Table: "reviews" | Partition key: reviewId
public class Review {

    private String reviewId;
    private String submissionId;
    private String reviewerId;
    private Float marksAwarded;
    private String feedback;
    private ResultStatus resultStatus;
    private LocalDateTime reviewedAt;

    @DynamoDbPartitionKey
    public String getReviewId() { return reviewId; }
    public void setReviewId(String reviewId) { this.reviewId = reviewId; }

    public String getSubmissionId() { return submissionId; }
    public void setSubmissionId(String submissionId) { this.submissionId = submissionId; }

    public String getReviewerId() { return reviewerId; }
    public void setReviewerId(String reviewerId) { this.reviewerId = reviewerId; }

    public Float getMarksAwarded() { return marksAwarded; }
    public void setMarksAwarded(Float marksAwarded) { this.marksAwarded = marksAwarded; }

    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }

    public ResultStatus getResultStatus() { return resultStatus; }
    public void setResultStatus(ResultStatus resultStatus) { this.resultStatus = resultStatus; }

    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }
}
