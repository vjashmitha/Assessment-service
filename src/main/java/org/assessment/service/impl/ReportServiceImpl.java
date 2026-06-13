package org.assessment.service.impl;

import lombok.RequiredArgsConstructor;
import org.assessment.dto.response.ReportResponse;
import org.assessment.dto.response.SubmissionResponse;
import org.assessment.entity.Assignment;
import org.assessment.enums.ResultStatus;
import org.assessment.enums.SubmissionStatus;
import org.assessment.exception.ResourceNotFoundException;
import org.assessment.mapper.SubmissionMapper;
import org.assessment.repository.AssignmentRepository;
import org.assessment.repository.SubmissionRepository;
import org.assessment.service.ReportService;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
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

        var submissions = submissionRepository.findByAssignmentId(assignmentId);
        List<SubmissionResponse> submissionResponses = submissions.stream()
                .map(submissionMapper::toResponse).collect(Collectors.toList());

        long submittedCount = submissions.stream()
                .filter(s -> !SubmissionStatus.PENDING.name().equals(s.getStatus())).count();
        long gradedCount = submissions.stream()
                .filter(s -> SubmissionStatus.GRADED.name().equals(s.getStatus())).count();
        long pendingCount = submissions.size() - submittedCount;
        long passCount = submissions.stream()
                .filter(s -> ResultStatus.PASS.name().equals(s.getResultStatus())).count();
        long failCount = submissions.stream()
                .filter(s -> ResultStatus.FAIL.name().equals(s.getResultStatus())).count();
        double averageScore = submissions.stream()
                .filter(s -> s.getObtainedMarks() != null)
                .mapToDouble(s -> s.getObtainedMarks()).average().orElse(0.0);
        double highestScore = submissions.stream()
                .filter(s -> s.getObtainedMarks() != null)
                .mapToDouble(s -> s.getObtainedMarks()).max().orElse(0.0);
        double lowestScore = submissions.stream()
                .filter(s -> s.getObtainedMarks() != null)
                .mapToDouble(s -> s.getObtainedMarks()).min().orElse(0.0);

        return ReportResponse.builder()
                .assignmentId(assignmentId)
                .assignmentTitle(assignment.getTitle())
                .totalStudents((long) submissions.size())
                .submittedCount(submittedCount)
                .pendingCount(pendingCount)
                .gradedCount(gradedCount)
                .averageScore(averageScore)
                .highestScore(highestScore)
                .lowestScore(lowestScore)
                .passCount(passCount)
                .failCount(failCount)
                .submissions(submissionResponses)
                .build();
    }

    @Override
    public List<ReportResponse> getCourseReport(String courseId) {
        return assignmentRepository.findByCourseId(courseId).stream()
                .map(a -> getAssignmentReport(a.getId()))
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
                        .append(s.getStatus()).append(",")
                        .append(s.getObtainedMarks()).append(",")
                        .append(s.getResultStatus()).append(",")
                        .append(s.getSubmittedAt()).append("\n")
        );
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }
}
