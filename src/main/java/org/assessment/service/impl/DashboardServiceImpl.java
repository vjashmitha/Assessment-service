package org.assessment.service.impl;

import lombok.RequiredArgsConstructor;
import org.assessment.dto.response.DashboardResponse;
import org.assessment.enums.ResultStatus;
import org.assessment.enums.SubmissionStatus;
import org.assessment.repository.AssignmentRepository;
import org.assessment.repository.AssignmentReviewRepository;
import org.assessment.repository.SubmissionRepository;
import org.assessment.service.DashboardService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final AssignmentRepository assignmentRepository;
    private final SubmissionRepository submissionRepository;
    private final AssignmentReviewRepository reviewRepository;

    @Override
    public DashboardResponse getInstructorDashboard(String instructorId) {
        var assignments = assignmentRepository.findByInstructorId(instructorId);
        long totalAssignments = assignments.size();
        long totalSubmissions = assignments.stream()
                .mapToLong(a -> submissionRepository.countByAssignmentId(a.getId()))
                .sum();
        long pendingReviews = assignments.stream()
                .flatMap(a -> submissionRepository.findByAssignmentIdAndStatus(a.getId(), SubmissionStatus.SUBMITTED).stream())
                .count();
        long gradedSubmissions = totalSubmissions - pendingReviews;

        return DashboardResponse.builder()
                .totalAssignments(totalAssignments)
                .totalSubmissions(totalSubmissions)
                .pendingReviews(pendingReviews)
                .gradedSubmissions(gradedSubmissions)
                .build();
    }

    @Override
    public DashboardResponse getStudentDashboard(String studentId) {
        var submissions = submissionRepository.findByStudentId(studentId);
        long totalSubmissions = submissions.size();
        long gradedSubmissions = submissions.stream()
                .filter(s -> SubmissionStatus.GRADED.name().equals(s.getStatus())).count();
        long passCount = submissions.stream()
                .filter(s -> ResultStatus.PASS.name().equals(s.getResultStatus())).count();
        long failCount = submissions.stream()
                .filter(s -> ResultStatus.FAIL.name().equals(s.getResultStatus())).count();
        double averageScore = submissions.stream()
                .filter(s -> s.getObtainedMarks() != null)
                .mapToDouble(s -> s.getObtainedMarks())
                .average().orElse(0.0);

        return DashboardResponse.builder()
                .totalSubmissions(totalSubmissions)
                .gradedSubmissions(gradedSubmissions)
                .passCount(passCount)
                .failCount(failCount)
                .averageScore(averageScore)
                .build();
    }

    @Override
    public DashboardResponse getCourseAssignmentStats(String courseId) {
        var assignments = assignmentRepository.findByCourseId(courseId);
        long totalAssignments = assignments.size();
        long totalSubmissions = assignments.stream()
                .mapToLong(a -> submissionRepository.countByAssignmentId(a.getId()))
                .sum();

        return DashboardResponse.builder()
                .totalAssignments(totalAssignments)
                .totalSubmissions(totalSubmissions)
                .build();
    }
}
