package org.assessment.service.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.assessment.dto.response.StudentAssignmentResponse;
import org.assessment.entity.Assignment;
import org.assessment.entity.Submission;
import org.assessment.enums.ResultStatus;
import org.assessment.enums.SubmissionStatus;
import org.assessment.exception.ResourceNotFoundException;
import org.assessment.repository.AssignmentRepository;
import org.assessment.repository.SubmissionRepository;
import org.assessment.service.StudentAssignmentService;
import org.assessment.storage.S3Service;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StudentAssignmentServiceImpl implements StudentAssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final SubmissionRepository submissionRepository;  
    private final S3Service s3Service;

    @Override
    public List<StudentAssignmentResponse> getMyAssignments(String studentId) {
        return assignmentRepository.findAll()
                .stream()
                .map(assignment -> buildStudentAssignmentResponse(assignment, studentId))
                .collect(Collectors.toList());
    }

    @Override
    public List<StudentAssignmentResponse> getMyAssignmentsByStatus(String studentId, SubmissionStatus status) {
        return getMyAssignments(studentId)
                .stream()
                .filter(response -> status == null || status == response.getSubmissionStatus())
                .collect(Collectors.toList());
    }

    @Override
    public StudentAssignmentResponse getAssignmentDetail(String assignmentId, String studentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found: " + assignmentId));

        return buildStudentAssignmentResponse(assignment, studentId);
    }

    private StudentAssignmentResponse buildStudentAssignmentResponse(Assignment assignment, String studentId) {
        Optional<Submission> submissionOpt = submissionRepository
                .findByAssignmentIdAndLearnerId(assignment.getAssignmentId(), studentId);

        boolean overdue = false;

        if (assignment.getDueDate() != null) {
            boolean pastDue = LocalDate.parse(assignment.getDueDate()).isBefore(LocalDate.now());
            boolean notSubmitted = submissionOpt.isEmpty()
                    || submissionOpt.get().getStatus() == SubmissionStatus.NOT_SUBMITTED;

            overdue = pastDue && notSubmitted;
        }

        SubmissionStatus submissionStatus = submissionOpt
                .map(Submission::getStatus)
                .orElse(SubmissionStatus.NOT_SUBMITTED);

        Float marksAwarded = submissionOpt
                .map(Submission::getMarksAwarded)
                .orElse(null);

        String feedback = submissionOpt
                .map(Submission::getFeedback)
                .orElse(null);

        ResultStatus resultStatus = submissionOpt
                .map(Submission::getResultStatus)
                .orElse(ResultStatus.PENDING);

        String reviewedAt = submissionOpt
                .filter(submission -> submission.getReviewedAt() != null)
                .map(submission -> submission.getReviewedAt().toString())
                .orElse(null);

        Float scorePercentage = null;

        if (marksAwarded != null
                && assignment.getTotalMarks() != null
                && assignment.getTotalMarks() > 0) {

            scorePercentage = (marksAwarded / assignment.getTotalMarks()) * 100f;
            scorePercentage = Math.round(scorePercentage * 10f) / 10f;
        }

        String assignmentFileName = extractFileName(assignment.getAssignmentFileUrl());

        String submissionFileName = submissionOpt
                .map(Submission::getSubmissionFileUrl)
                .map(this::extractFileName)
                .orElse(null);

        return StudentAssignmentResponse.builder()
                .assignmentId(assignment.getAssignmentId())
                .title(assignment.getTitle())
                .description(assignment.getDescription())
                .courseId(assignment.getCourseId())
                .courseName(assignment.getCourseName())
                .totalMarks(assignment.getTotalMarks())
                .passMarks(assignment.getPassMarks())
                .difficultyLevel(assignment.getDifficultyLevel())
                .assignmentStatus(assignment.getStatus())
                .dueDate(assignment.getDueDate() != null ? assignment.getDueDate().toString() : null)
                .createdAt(assignment.getCreatedAt() != null ? assignment.getCreatedAt().toString() : null)
                .overdue(overdue)
                .assignmentFileUrl(assignment.getAssignmentFileUrl())
                .assignmentFileName(assignmentFileName)
                .submissionId(submissionOpt.map(Submission::getSubmissionId).orElse(null))
                .submissionStatus(submissionStatus)
                .submittedAt(submissionOpt
                        .filter(submission -> submission.getSubmittedAt() != null)
                        .map(submission -> submission.getSubmittedAt().toString())
                        .orElse(null))
                .submissionFileUrl(submissionOpt
                        .map(Submission::getSubmissionFileUrl)
                        .orElse(null))
                .submissionFileName(submissionFileName)
                .marksAwarded(marksAwarded)
                .scorePercentage(scorePercentage)
                .resultStatus(resultStatus)
                .feedback(feedback)
                .reviewedAt(reviewedAt)
                .build();
    }

    private String extractFileName(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }

        int lastSlash = url.lastIndexOf('/');
        return lastSlash >= 0 ? url.substring(lastSlash + 1) : url;
    }
    
    @Override
    public String downloadAssignment(String assignmentId, String studentId) {

        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found"));

        return s3Service.generatePresignedUrl(
                assignment.getAssignmentFileUrl());
    }
    
    @Override
    public String downloadSubmission(String assignmentId,
                                     String studentId) {

        Submission submission = submissionRepository
                .findByAssignmentIdAndLearnerId(
                        assignmentId,
                        studentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Submission not found"));

        return s3Service.generatePresignedUrl(
                submission.getSubmissionFileUrl());
    }
    
    
    
}