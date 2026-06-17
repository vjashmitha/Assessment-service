package org.assessment.service.impl;

import lombok.RequiredArgsConstructor;
import org.assessment.dto.response.ReportResponse;
import org.assessment.dto.response.SubmissionResponse;
import org.assessment.entity.Assignment;
import org.assessment.entity.Review;
import org.assessment.entity.Submission;
import org.assessment.enums.ResultStatus;
import org.assessment.enums.SubmissionStatus;
import org.assessment.exception.ResourceNotFoundException;
import org.assessment.mapper.SubmissionMapper;
import org.assessment.repository.AssignmentRepository;
import org.assessment.repository.ReviewRepository;
import org.assessment.repository.SubmissionRepository;
import org.assessment.service.ReportService;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final AssignmentRepository assignmentRepository;
    private final SubmissionRepository submissionRepository;
    private final ReviewRepository reviewRepository;
    private final SubmissionMapper submissionMapper;

    @Override
    public ReportResponse getAssignmentReport(String assignmentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + assignmentId));

        List<Submission> submissions = submissionRepository.findByAssignmentId(assignmentId);
        List<SubmissionResponse> submissionResponses = new ArrayList<>();

        long submittedCount = 0;
        long gradedCount = 0;
        long pendingCount = 0;
        long passCount = 0;
        long failCount = 0;
        float totalMarks = 0.0f;
        float highestScore = 0.0f;
        float lowestScore = Float.MAX_VALUE;
        boolean hasGrades = false;

        for (Submission submission : submissions) {
            Optional<Review> reviewOpt = reviewRepository.findBySubmissionId(submission.getSubmissionId());
            Review review = reviewOpt.orElse(null);
            
            submissionResponses.add(submissionMapper.toResponse(submission, review));

            if (submission.getStatus() != SubmissionStatus.NOT_SUBMITTED) {
                submittedCount++;
            }

            if (submission.getStatus() == SubmissionStatus.REVIEWED) {
                gradedCount++;
            } else {
                pendingCount++;
            }

            if (review != null) {
                if (review.getResultStatus() == ResultStatus.PASS) {
                    passCount++;
                } else if (review.getResultStatus() == ResultStatus.FAIL) {
                    failCount++;
                }
                if (review.getMarksAwarded() != null) {
                    hasGrades = true;
                    float marks = review.getMarksAwarded();
                    totalMarks += marks;
                    if (marks > highestScore) {
                        highestScore = marks;
                    }
                    if (marks < lowestScore) {
                        lowestScore = marks;
                    }
                }
            }
        }

        float averageScore = (hasGrades && gradedCount > 0) ? (totalMarks / gradedCount) : 0.0f;
        float finalLowestScore = hasGrades ? lowestScore : 0.0f;

        return ReportResponse.builder()
                .assignmentId(assignmentId)
                .assignmentTitle(assignment.getTitle())
                .dueDate(assignment.getDueDate() != null ? assignment.getDueDate().toString() : null)
                .status(assignment.getStatus())
                .totalStudents((long) submissions.size())
                .submittedCount(submittedCount)
                .pendingCount(pendingCount)
                .gradedCount(gradedCount)
                .averageScore(averageScore)
                .highestScore(highestScore)
                .lowestScore(finalLowestScore)
                .passCount(passCount)
                .failCount(failCount)
                .submissions(submissionResponses)
                .build();
    }

    @Override
    public List<ReportResponse> getCourseReport(String courseId) {
        return assignmentRepository.findByCourseId(courseId).stream()
                .map(a -> getAssignmentReport(a.getAssignmentId()))
                .collect(Collectors.toList());
    }

    @Override
    public byte[] exportReportAsCsv(String assignmentId) {
        ReportResponse report = getAssignmentReport(assignmentId);
        StringBuilder csv = new StringBuilder();
        csv.append("SubmissionId,StudentId,Status,ObtainedMarks,ResultStatus,SubmittedAt\n");
        report.getSubmissions().forEach(s ->
                csv.append(s.getId()).append(",")
                        .append(s.getStudentId()).append(",")
                        .append(s.getStatus() != null ? s.getStatus().name() : "").append(",")
                        .append(s.getObtainedMarks() != null ? s.getObtainedMarks() : "").append(",")
                        .append(s.getResultStatus() != null ? s.getResultStatus().name() : "").append(",")
                        .append(s.getSubmittedAt() != null ? s.getSubmittedAt() : "").append("\n")
        );
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }
}
