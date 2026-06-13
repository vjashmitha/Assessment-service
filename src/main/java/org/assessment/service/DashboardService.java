package org.assessment.service;

import org.assessment.dto.response.DashboardResponse;

public interface DashboardService {
    DashboardResponse getInstructorDashboard(String instructorId);
    DashboardResponse getStudentDashboard(String studentId);
    DashboardResponse getCourseAssignmentStats(String courseId);
}
