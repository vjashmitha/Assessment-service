package org.assessment.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", url = "${services.user-service}")
public interface UserServiceClient {

    @GetMapping("/api/v1/users/{userId}/exists")
    Boolean userExists(@PathVariable Long userId);
}
