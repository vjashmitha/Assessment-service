package org.assessment.security;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.assessment.util.Constants;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Forwards the Authorization token and user headers from the incoming request
 * to all outbound Feign client calls (inter-service communication).
 */
@Component
public class FeignAuthInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes attrs =
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) return;

        HttpServletRequest request = attrs.getRequest();

        // Forward the JWT token
        String authHeader = request.getHeader(Constants.AUTHORIZATION_HEADER);
        if (authHeader != null && !authHeader.isBlank()) {
            template.header(Constants.AUTHORIZATION_HEADER, authHeader);
        }

        // Forward user context headers
        String userId = request.getHeader(Constants.USER_ID_HEADER);
        if (userId != null && !userId.isBlank()) {
            template.header(Constants.USER_ID_HEADER, userId);
        }

        String userRole = request.getHeader(Constants.USER_ROLE_HEADER);
        if (userRole != null && !userRole.isBlank()) {
            template.header(Constants.USER_ROLE_HEADER, userRole);
        }
    }
}
