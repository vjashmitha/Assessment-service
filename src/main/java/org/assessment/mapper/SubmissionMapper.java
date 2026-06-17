package org.assessment.mapper;

import org.assessment.dto.request.SubmitAssignmentRequest;
import org.assessment.dto.response.SubmissionResponse;
import org.assessment.entity.Submission;
import org.assessment.entity.Review;
import org.assessment.enums.SubmissionStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class SubmissionMapper {

    public Submission toEntity(SubmitAssignmentRequest request, String learnerId) {
        if (request == null) {
            return null;
        }
        Submission submission = new Submission();
        submission.setSubmissionId(UUID.randomUUID().toString());
        submission.setAssignmentId(request.getAssignmentId());
        submission.setLearnerId(learnerId);
        submission.setSubmissionFileUrl(request.getFileUrl());
        submission.setStatus(SubmissionStatus.SUBMITTED);
        submission.setSubmittedAt(LocalDateTime.now());
        return submission;
    }

    public SubmissionResponse toResponse(Submission submission) {
        return toResponse(submission, null);
    }

    public SubmissionResponse toResponse(Submission submission, Review review) {
        if (submission == null) {
            return null;
        }

        Float marks = null;
        if (review != null && review.getMarksAwarded() != null) {
            marks = review.getMarksAwarded();
        }

        return SubmissionResponse.builder()
                .id(submission.getSubmissionId())
                .assignmentId(submission.getAssignmentId())
                .studentId(submission.getLearnerId())
                .content(null)
                .fileUrl(submission.getSubmissionFileUrl())
                .status(submission.getStatus())
                .resultStatus(review != null ? review.getResultStatus() : null)
                .obtainedMarks(marks)
                .feedback(review != null ? review.getFeedback() : null)
                .submittedAt(submission.getSubmittedAt() != null ? submission.getSubmittedAt().toString() : null)
                .createdAt(submission.getSubmittedAt() != null ? submission.getSubmittedAt().toString() : null)
                .updatedAt(submission.getSubmittedAt() != null ? submission.getSubmittedAt().toString() : null)
                .build();
    }
}
