package org.assessment.controller;

import lombok.RequiredArgsConstructor;
import org.assessment.dto.response.ReportResponse;
import org.assessment.service.ReportService;
import org.assessment.util.Constants;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(Constants.REPORT_BASE_URL)
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/assignment/{assignmentId}")
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    public ResponseEntity<ReportResponse> getAssignmentReport(@PathVariable String assignmentId) {
        return ResponseEntity.ok(reportService.getAssignmentReport(assignmentId));
    }

    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    public ResponseEntity<List<ReportResponse>> getCourseReport(@PathVariable String courseId) {
        return ResponseEntity.ok(reportService.getCourseReport(courseId));
    }

    @GetMapping("/assignment/{assignmentId}/export")
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportReportAsCsv(@PathVariable String assignmentId) {
        byte[] csv = reportService.exportReportAsCsv(assignmentId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=report-" + assignmentId + ".csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }
}
