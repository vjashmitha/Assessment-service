package org.assessment.service.impl;

import lombok.RequiredArgsConstructor;
import org.assessment.dto.response.DashboardResponse;
import org.assessment.entity.Assignment;
import org.assessment.entity.Submission;
import org.assessment.enums.ResultStatus;
import org.assessment.enums.SubmissionStatus;
import org.assessment.repository.AssignmentRepository;
import org.assessment.repository.SubmissionRepository;
import org.assessment.service.DashboardService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

	private final AssignmentRepository assignmentRepository;
	private final SubmissionRepository submissionRepository;

	@Override
	public DashboardResponse getInstructorDashboard(String instructorId) {
		List<Assignment> assignments = assignmentRepository.findByTrainerId(instructorId);

		long totalAssignments = assignments.size();
		long totalSubmissions = 0;
		long pendingReviews = 0;
		long gradedSubmissions = 0;
		long passCount = 0;
		long failCount = 0;
		float totalMarks = 0.0f;

		for (Assignment assignment : assignments) {
			List<Submission> submissions = submissionRepository.findByAssignmentId(assignment.getAssignmentId());

			totalSubmissions += submissions.size();

			for (Submission submission : submissions) {
				if (submission.getStatus() == SubmissionStatus.REVIEWED) {
					gradedSubmissions++;

					if (submission.getResultStatus() == ResultStatus.PASS) {
						passCount++;
					}

					if (submission.getResultStatus() == ResultStatus.FAIL) {
						failCount++;
					}

					if (submission.getMarksAwarded() != null) {
						totalMarks += submission.getMarksAwarded();
					}
				} else {
					pendingReviews++;
				}
			}
		}

		float averageScore = gradedSubmissions > 0 ? totalMarks / gradedSubmissions : 0.0f;

		return DashboardResponse.builder().totalAssignments(totalAssignments).totalSubmissions(totalSubmissions)
				.pendingReviews(pendingReviews).gradedSubmissions(gradedSubmissions).passCount(passCount)
				.failCount(failCount).averageScore(averageScore).build();
	}

	@Override
	public DashboardResponse getStudentDashboard(String studentId) {
		List<Submission> submissions = submissionRepository.findByLearnerId(studentId);

		long totalSubmissions = submissions.size();
		long gradedSubmissions = 0;
		long passCount = 0;
		long failCount = 0;
		float totalMarks = 0.0f;

		long pendingOverdue = assignmentRepository.findAll().stream()
				.filter(assignment -> assignment.getDueDate() != null)
				.filter(assignment -> LocalDate.parse(assignment.getDueDate()).isBefore(LocalDate.now()))
				.filter(assignment -> submissionRepository
						.findByAssignmentIdAndLearnerId(assignment.getAssignmentId(), studentId).isEmpty())
				.count();

		for (Submission submission : submissions) {
			if (submission.getStatus() == SubmissionStatus.REVIEWED) {
				gradedSubmissions++;

				if (submission.getResultStatus() == ResultStatus.PASS) {
					passCount++;
				}

				if (submission.getResultStatus() == ResultStatus.FAIL) {
					failCount++;
				}

				if (submission.getMarksAwarded() != null) {
					totalMarks += submission.getMarksAwarded();
				}
			}
		}

		float averageScore = gradedSubmissions > 0 ? totalMarks / gradedSubmissions : 0.0f;

		return DashboardResponse.builder().totalAssignments((long) assignmentRepository.findAll().size())
				.totalSubmissions(totalSubmissions).submittedCount(totalSubmissions).reviewedCount(gradedSubmissions)
				.pendingOverdue(pendingOverdue).gradedSubmissions(gradedSubmissions).passCount(passCount)
				.failCount(failCount).averageScore(averageScore).build();
	}

	@Override
	public DashboardResponse getCourseAssignmentStats(String courseId) {
		List<Assignment> assignments = assignmentRepository.findByCourseId(courseId);

		long totalAssignments = assignments.size();
		long totalSubmissions = 0;

		for (Assignment assignment : assignments) {
			totalSubmissions += submissionRepository.countByAssignmentId(assignment.getAssignmentId());
		}

		return DashboardResponse.builder().totalAssignments(totalAssignments).totalSubmissions(totalSubmissions)
				.build();
	}
}