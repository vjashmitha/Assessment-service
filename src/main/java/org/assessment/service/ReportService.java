package org.assessment.service;

import org.assessment.dto.response.ReportResponse;

import java.util.List;

public interface ReportService {
    ReportResponse getAssignmentReport(String assignmentId);
    List<ReportResponse> getCourseReport(String courseId);
    byte[] exportReportAsCsv(String assignmentId);
}
