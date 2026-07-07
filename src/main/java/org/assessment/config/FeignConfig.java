package org.assessment.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import feign.RequestInterceptor;

@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor ngrokHeaderInterceptor() {
        return template -> template.header("ngrok-skip-browser-warning", "true");
    }
}