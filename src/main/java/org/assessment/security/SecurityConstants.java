package org.assessment.security;

public class SecurityConstants {
    private SecurityConstants() {}

    public static final String[] PUBLIC_URLS = {
            "/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/actuator/health"
    };
}
