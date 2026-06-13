package org.assessment.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "course-service", url = "${services.course-service}")
public interface CourseServiceClient {

    @GetMapping("/api/v1/courses/{courseId}/exists")
    Boolean courseExists(@PathVariable Long courseId);
}
