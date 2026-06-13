package org.assessment.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "notification-service", url = "${services.notification-service}")
public interface NotificationServiceClient {

    @PostMapping("/api/v1/notifications/send")
    void sendNotification(@RequestBody Map<String, Object> payload);
}
