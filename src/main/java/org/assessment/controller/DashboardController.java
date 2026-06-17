package org.assessment.controller;

import lombok.RequiredArgsConstructor;
import org.assessment.dto.response.DashboardResponse;
import org.assessment.service.DashboardService;
import org.assessment.util.Constants;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(Constants.DASHBOARD_BASE_URL)
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/instructors/{instructorId}")
    public ResponseEntity<DashboardResponse> getInstructorDashboard(@PathVariable String instructorId) {
        return ResponseEntity.ok(dashboardService.getInstructorDashboard(instructorId));
    }

    @GetMapping("/students/{studentId}")
    public ResponseEntity<DashboardResponse> getStudentDashboard(@PathVariable String studentId) {
        return ResponseEntity.ok(dashboardService.getStudentDashboard(studentId));
    }

    @GetMapping("/courses/{courseId}")
    public ResponseEntity<DashboardResponse> getCourseStats(@PathVariable String courseId) {
        return ResponseEntity.ok(dashboardService.getCourseAssignmentStats(courseId));
    }
}
