package org.assessment.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class CommonUtil {
    private CommonUtil() {}

    public static String extractUserIdFromRequest() {
        HttpServletRequest request = getCurrentRequest();
        String userId = request.getHeader(Constants.USER_ID_HEADER);
        if (userId == null || userId.isBlank()) {
            throw new IllegalStateException("User ID not found in request headers");
        }
        return userId;
    }

    public static String extractUserRoleFromRequest() {
        HttpServletRequest request = getCurrentRequest();
        return request.getHeader(Constants.USER_ROLE_HEADER);
    }

    public static String extractTokenFromRequest() {
        HttpServletRequest request = getCurrentRequest();
        String authHeader = request.getHeader(Constants.AUTHORIZATION_HEADER);
        if (authHeader != null && authHeader.startsWith(Constants.BEARER_PREFIX)) {
            return authHeader.substring(Constants.BEARER_PREFIX.length());
        }
        return null;
    }

    private static HttpServletRequest getCurrentRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    }
}
