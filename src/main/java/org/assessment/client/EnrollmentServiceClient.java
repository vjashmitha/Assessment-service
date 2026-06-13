package org.assessment.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "enrollment-service", url = "${services.enrollment-service}")
public interface EnrollmentServiceClient {

    @GetMapping("/api/v1/enrollments/check")
    Boolean isEnrolled(@RequestParam Long studentId, @RequestParam Long courseId);

    @GetMapping("/api/v1/enrollments/count")
    Long getEnrolledCount(@RequestParam Long courseId);
}
