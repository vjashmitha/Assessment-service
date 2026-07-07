package org.assessment.dto.response;

import lombok.Builder;
import lombok.Data;
import org.assessment.enums.ResultStatus;
import org.assessment.enums.SubmissionStatus;

@Data
@Builder
public class SubmissionResponse {
	private String submissionId;
	private String assignmentId;
	private String courseId;
	private String learnerId;
	private String learnerName;
	private String trainerId;
	private String submissionFileUrl;
	private SubmissionStatus status;
	private ResultStatus resultStatus;
	private Float marksAwarded;
	private String feedback;
	private String submittedAt;
	private String reviewedBy;
	private String reviewedAt;
	private Integer attemptNumber;
	private String learnerUsername;
	private String learnerEmail;
}
