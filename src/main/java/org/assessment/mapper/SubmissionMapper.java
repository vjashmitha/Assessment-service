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
        submission.setSubmissionFileUrl(request.getSubmissionFileUrl());
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
                .submissionId(submission.getSubmissionId())
                .assignmentId(submission.getAssignmentId())
                .courseId(submission.getCourseId())
                .trainerId(submission.getTrainerId())
                .learnerId(submission.getLearnerId())
                .learnerName(submission.getLearnerName())
                .submissionFileUrl(submission.getSubmissionFileUrl())
                .status(submission.getStatus())
                .resultStatus(review != null ? review.getResultStatus() : submission.getResultStatus())
                .marksAwarded(review != null ? marks : submission.getMarksAwarded())
                .feedback(review != null ? review.getFeedback() : submission.getFeedback())
                .reviewedBy(submission.getReviewedBy())
                .attemptNumber(submission.getAttemptNumber())
                .submittedAt(submission.getSubmittedAt() != null ? submission.getSubmittedAt().toString() : null)
                .reviewedAt(submission.getReviewedAt() != null ? submission.getReviewedAt().toString() : null)
                .build();
    }
}
