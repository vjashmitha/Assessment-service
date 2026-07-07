package org.assessment.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.assessment.util.Constants;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class HeaderAuthFilter extends OncePerRequestFilter {
	public static final String USER_ID_HEADER = "X-User-Id";
	public static final String USER_ROLE_HEADER = "X-User-Role";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String userId = request.getHeader(Constants.USER_ID_HEADER);
        String userRole = request.getHeader(Constants.USER_ROLE_HEADER);

        if (userId != null && !userId.isBlank()) {

            List<SimpleGrantedAuthority> authorities = List.of();

            if (userRole != null && !userRole.isBlank()) {
                String normalizedRole = userRole.trim().toUpperCase();

                if (!normalizedRole.startsWith("ROLE_")) {
                    normalizedRole = "ROLE_" + normalizedRole;
                }

                authorities = List.of(new SimpleGrantedAuthority(normalizedRole));
            }

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userId, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
        System.out.println("X-User-Id = " + userId);
        System.out.println("X-User-Role = " + userRole);
    }
    
}