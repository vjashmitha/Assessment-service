package org.assessment.client;

import org.assessment.config.FeignConfig;
import org.assessment.dto.response.CourseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
@FeignClient(
        name = "course-service",
        url = "${services.course-service}",
        configuration = FeignConfig.class
)
public interface CourseServiceClient {

    @GetMapping("/api/v1/course/{courseId}/exists")
    Boolean courseExists(@PathVariable("courseId") String courseId);
    

    
    
    @GetMapping("/api/v1/course/{courseId}/summary")
    CourseResponse getCourseById(@PathVariable("courseId") String courseId);
}