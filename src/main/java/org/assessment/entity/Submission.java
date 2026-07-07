package org.assessment.entity;

import java.time.LocalDateTime;

import org.assessment.enums.ResultStatus;
import org.assessment.enums.SubmissionStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamoDbBean
// Table: "submissions" | Partition key: submissionId
public class Submission {

    private String submissionId;
    private String assignmentId;
    private String learnerId;
    private String submissionFileUrl;
    private SubmissionStatus status;
    private LocalDateTime submittedAt;
   private String courseId;
   private String trainerId; 
   private String feedback;
   private String reviewedBy;  
 
   private Float marksAwarded;
   private LocalDateTime reviewedAt;
   private ResultStatus resultStatus;
   private Integer attemptNumber;
   
   private String learnerName;
    @DynamoDbPartitionKey
    public String getSubmissionId() { return submissionId; }
    public void setSubmissionId(String submissionId) { this.submissionId = submissionId; }

    public String getAssignmentId() { return assignmentId; }
    public void setAssignmentId(String assignmentId) { this.assignmentId = assignmentId; }

    public String getLearnerId() { return learnerId; }
    public void setLearnerId(String learnerId) { this.learnerId = learnerId; }

    public String getSubmissionFileUrl() { return submissionFileUrl; }
    public void setSubmissionFileUrl(String submissionFileUrl) { this.submissionFileUrl = submissionFileUrl; }

    public SubmissionStatus getStatus() { return status; }
    public void setStatus(SubmissionStatus status) { this.status = status; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
	public String getCourseId() {
		return courseId;
	}
	public void setCourseId(String courseId) {
		this.courseId = courseId;
	}
	public String getTrainerId() {
		return trainerId;
	}
	public void setTrainerId(String trainerId) {
		this.trainerId = trainerId;
	}
	 
	public String getFeedback() {
		return feedback;
	}
	public void setFeedback(String feedback) {
		this.feedback = feedback;
	}
	public String getReviewedBy() {
		return reviewedBy;
	}
	public void setReviewedBy(String reviewedBy) {
		this.reviewedBy = reviewedBy;
	}
	public Float getMarksAwarded() {
		return marksAwarded;
	}
	public void setMarksAwarded(Float marksAwarded) {
		this.marksAwarded = marksAwarded;
	}
	public LocalDateTime getReviewedAt() {
		return reviewedAt;
	}
	public void setReviewedAt(LocalDateTime reviewedAt) {
		this.reviewedAt = reviewedAt;
	}
	public ResultStatus getResultStatus() {
		return resultStatus;
	}
	public void setResultStatus(ResultStatus resultStatus) {
		this.resultStatus = resultStatus;
	}
	public Integer getAttemptNumber() {
		return attemptNumber;
	}
	public void setAttemptNumber(Integer attemptNumber) {
		this.attemptNumber = attemptNumber;
	}
	public String getLearnerName() {
		return learnerName;
	}
	public void setLearnerName(String learnerName) {
		this.learnerName = learnerName;
	}
	 
}
