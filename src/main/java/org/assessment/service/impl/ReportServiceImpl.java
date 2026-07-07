package org.assessment.service.impl;

import lombok.RequiredArgsConstructor;
import org.assessment.dto.response.ReportResponse;
import org.assessment.dto.response.SubmissionResponse;
import org.assessment.entity.Assignment;
import org.assessment.entity.Submission;
import org.assessment.enums.ResultStatus;
import org.assessment.enums.SubmissionStatus;
import org.assessment.exception.ResourceNotFoundException;
import org.assessment.mapper.SubmissionMapper;
import org.assessment.repository.AssignmentRepository;
import org.assessment.repository.SubmissionRepository;
import org.assessment.service.ReportService;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final AssignmentRepository assignmentRepository;
    private final SubmissionRepository submissionRepository;
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
            submissionResponses.add(submissionMapper.toResponse(submission));

            if (submission.getStatus() != SubmissionStatus.NOT_SUBMITTED) {
                submittedCount++;
            }

            if (submission.getStatus() == SubmissionStatus.REVIEWED) {
                gradedCount++;

                if (submission.getResultStatus() == ResultStatus.PASS) {
                    passCount++;
                } else if (submission.getResultStatus() == ResultStatus.FAIL) {
                    failCount++;
                }

                if (submission.getMarksAwarded() != null) {
                    hasGrades = true;
                    float marks = submission.getMarksAwarded();

                    totalMarks += marks;

                    if (marks > highestScore) {
                        highestScore = marks;
                    }

                    if (marks < lowestScore) {
                        lowestScore = marks;
                    }
                }
            } else {
                pendingCount++;
            }
        }

        float averageScore = hasGrades && gradedCount > 0
                ? totalMarks / gradedCount
                : 0.0f;

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
        return assignmentRepository.findByCourseId(courseId)
                .stream()
                .map(assignment -> getAssignmentReport(assignment.getAssignmentId()))
                .collect(Collectors.toList());
    }

    @Override
    public byte[] exportReportAsCsv(String assignmentId) {
        ReportResponse report = getAssignmentReport(assignmentId);

        StringBuilder csv = new StringBuilder();
        csv.append("SubmissionId,LearnerId,LearnerName,Status,MarksAwarded,ResultStatus,SubmittedAt,ReviewedAt\n");

        report.getSubmissions().forEach(submission ->
                csv.append(nullSafe(submission.getSubmissionId())).append(",")
                        .append(nullSafe(submission.getLearnerId())).append(",")
                        .append(nullSafe(submission.getLearnerName())).append(",")
                        .append(submission.getStatus() != null ? submission.getStatus().name() : "").append(",")
                        .append(submission.getMarksAwarded() != null ? submission.getMarksAwarded() : "").append(",")
                        .append(submission.getResultStatus() != null ? submission.getResultStatus().name() : "").append(",")
                        .append(nullSafe(submission.getSubmittedAt())).append(",")
                        .append(nullSafe(submission.getReviewedAt())).append("\n")
        );

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String nullSafe(String value) {
        return value == null ? "" : value.replace(",", " ");
    }
}