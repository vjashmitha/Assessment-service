package org.assessment.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

/**
 * Utility for parsing and validating JWT tokens.
 *
 * In this microservice architecture the API Gateway is the primary validator —
 * it validates the JWT and forwards X-User-Id / X-User-Role headers.
 * HeaderAuthFilter reads those headers to build the SecurityContext.
 *
 * This class exists so the service can ALSO independently verify a token
 * if needed (e.g. for added security, or when calling without a gateway).
 */
@Component
@Slf4j
public class JwtUtil {

    private final Key signingKey;

    public JwtUtil(@Value("${jwt.secret}") String secret) {
        // Build an HMAC-SHA key from the configured secret
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Parse and validate a JWT token string.
     * Strips the "Bearer " prefix if present.
     *
     * @param token raw Authorization header value or plain token string
     * @return Claims object containing userId, role, expiry, etc.
     * @throws JwtException if the token is invalid or expired
     */
    public Claims parseToken(String token) {
        String cleanToken = stripBearer(token);
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(cleanToken)
                .getBody();
    }

    /**
     * Extract the userId (subject) from a token.
     */
    public String extractUserId(String token) {
        return parseToken(token).getSubject();
    }

    /**
     * Extract a specific claim by key.
     * Example: extractClaim(token, "role") → "INSTRUCTOR"
     */
    public String extractClaim(String token, String claimKey) {
        Claims claims = parseToken(token);
        return claims.get(claimKey, String.class);
    }

    /**
     * Check if a token is valid (signature OK and not expired).
     */
    public boolean isTokenValid(String token) {
        try {
            Claims claims = parseToken(token);
            return !claims.getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Check if a token has expired.
     */
    public boolean isTokenExpired(String token) {
        try {
            return parseToken(token).getExpiration().before(new Date());
        } catch (JwtException e) {
            return true;
        }
    }

    /**
     * Strip the "Bearer " prefix if present.
     */
    private String stripBearer(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return token;
    }
}
