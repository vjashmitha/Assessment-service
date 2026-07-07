package org.assessment.client;

import org.assessment.dto.response.UserSummaryResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "auth-service", url = "${services.auth-service}")
public interface AuthServiceClient {

    @GetMapping("/api/v1/auth/internal/user/id/{userId}")
    UserSummaryResponse getUserById(@PathVariable("userId") String userId);
}