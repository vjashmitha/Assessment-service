package org.assessment.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class CommonUtil {
    private CommonUtil() {}

    /**
     * Returns the current user ID.
     * First checks the Spring SecurityContext (set by HeaderAuthFilter),
     * then falls back to the raw X-User-Id header.
     */
    public static String extractUserIdFromRequest() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof String principal && !principal.isBlank()) {
            return principal;
        }
        // Fallback: read directly from header
        HttpServletRequest request = getCurrentRequest();
        String userId = request.getHeader(Constants.USER_ID_HEADER);
        if (userId == null || userId.isBlank()) {
            throw new IllegalStateException("User ID not found in request headers");
        }
        return userId;
    }

    public static String extractUserRoleFromRequest() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getAuthorities() != null && !auth.getAuthorities().isEmpty()) {
            String authority = auth.getAuthorities().iterator().next().getAuthority();
            // Strip the ROLE_ prefix added by HeaderAuthFilter
            return authority.startsWith("ROLE_") ? authority.substring(5) : authority;
        }
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
