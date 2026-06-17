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

/**
 * This filter trusts the X-User-Id and X-User-Role headers forwarded by the API Gateway.
 * The Gateway is responsible for validating the JWT before forwarding requests here.
 */
@Component
public class HeaderAuthFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String userId = request.getHeader(Constants.USER_ID_HEADER);
        String userRole = request.getHeader(Constants.USER_ROLE_HEADER);

        if (userId != null && !userId.isBlank()) {
            List<SimpleGrantedAuthority> authorities = List.of();
            if (userRole != null && !userRole.isBlank()) {
                authorities = List.of(new SimpleGrantedAuthority("ROLE_" + userRole.toUpperCase()));
            }

            UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userId, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        filterChain.doFilter(request, response);
    }
}
