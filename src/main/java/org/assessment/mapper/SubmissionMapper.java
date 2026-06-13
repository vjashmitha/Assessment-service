package org.assessment.mapper;

import org.assessment.dto.request.SubmitAssignmentRequest;
import org.assessment.dto.response.SubmissionResponse;
import org.assessment.entity.Submission;
import org.assessment.enums.SubmissionStatus;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SubmissionMapper {

    public Submission toEntity(SubmitAssignmentRequest request, String studentId) {
        Submission submission = new Submission();
        submission.setId(UUID.randomUUID().toString());
        submission.setAssignmentId(request.getAssignmentId());
        submission.setStudentId(studentId);
        submission.setContent(request.getContent());
        submission.setFileUrl(request.getFileUrl());
        submission.setStatusEnum(SubmissionStatus.SUBMITTED);
        return submission;
    }

    public SubmissionResponse toResponse(Submission submission) {
        return SubmissionResponse.builder()
                .id(submission.getId())
                .assignmentId(submission.getAssignmentId())
                .studentId(submission.getStudentId())
                .content(submission.getContent())
                .fileUrl(submission.getFileUrl())
                .status(submission.getStatusEnum())
                .resultStatus(submission.getResultStatusEnum())
                .obtainedMarks(submission.getObtainedMarks())
                .submittedAt(submission.getSubmittedAt())
                .createdAt(submission.getCreatedAt())
                .updatedAt(submission.getUpdatedAt())
                .build();
    }
}
