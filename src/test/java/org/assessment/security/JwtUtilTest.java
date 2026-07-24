package org.assessment.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("JwtUtil Tests")
class JwtUtilTest {

    // Use a 256-bit (32-char) secret to satisfy HMAC-SHA256 key length requirement
    private static final String SECRET = "test-secret-key-for-unit-tests-32chars!!";

    private JwtUtil jwtUtil;
    private Key signingKey;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(SECRET);
        signingKey = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    /** Helper to build a valid, non-expired token */
    private String buildToken(String subject, String role, long expiryMs) {
        return Jwts.builder()
                .setSubject(subject)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiryMs))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /** Helper to build an already-expired token */
    private String buildExpiredToken(String subject) {
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis() - 10000))
                .setExpiration(new Date(System.currentTimeMillis() - 5000))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // -------------------------------------------------------------------------
    // parseToken
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("parseToken")
    class ParseToken {

        @Test
        @DisplayName("should parse valid token and return claims")
        void parse_validToken_returnsClaims() {
            String token = buildToken("user-42", "INSTRUCTOR", 60_000);

            Claims claims = jwtUtil.parseToken(token);

            assertThat(claims.getSubject()).isEqualTo("user-42");
            assertThat(claims.get("role", String.class)).isEqualTo("INSTRUCTOR");
        }

        @Test
        @DisplayName("should strip Bearer prefix before parsing")
        void parse_stripsBearerPrefix() {
            String token = buildToken("user-42", "LEARNER", 60_000);

            Claims claims = jwtUtil.parseToken("Bearer " + token);

            assertThat(claims.getSubject()).isEqualTo("user-42");
        }

        @Test
        @DisplayName("should throw JwtException for tampered token")
        void parse_tamperedToken_throwsException() {
            String token = buildToken("user-42", "ADMIN", 60_000);
            String tampered = token.substring(0, token.length() - 4) + "XXXX";

            assertThatThrownBy(() -> jwtUtil.parseToken(tampered))
                    .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("should throw JwtException for expired token")
        void parse_expiredToken_throwsException() {
            String token = buildExpiredToken("user-42");

            assertThatThrownBy(() -> jwtUtil.parseToken(token))
                    .isInstanceOf(Exception.class);
        }
    }

    // -------------------------------------------------------------------------
    // extractUserId
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("extractUserId")
    class ExtractUserId {

        @Test
        @DisplayName("should extract subject as userId")
        void extractUserId_returnsSubject() {
            String token = buildToken("user-99", "LEARNER", 60_000);

            String userId = jwtUtil.extractUserId(token);

            assertThat(userId).isEqualTo("user-99");
        }

        @Test
        @DisplayName("should extract userId when token has Bearer prefix")
        void extractUserId_withBearerPrefix() {
            String token = buildToken("user-99", "LEARNER", 60_000);

            String userId = jwtUtil.extractUserId("Bearer " + token);

            assertThat(userId).isEqualTo("user-99");
        }
    }

    // -------------------------------------------------------------------------
    // extractClaim
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("extractClaim")
    class ExtractClaim {

        @Test
        @DisplayName("should extract custom claim by key")
        void extractClaim_role() {
            String token = buildToken("user-1", "TRAINER", 60_000);

            String role = jwtUtil.extractClaim(token, "role");

            assertThat(role).isEqualTo("TRAINER");
        }

        @Test
        @DisplayName("should return null for non-existent claim key")
        void extractClaim_missingKey_returnsNull() {
            String token = buildToken("user-1", "TRAINER", 60_000);

            String value = jwtUtil.extractClaim(token, "nonexistent");

            assertThat(value).isNull();
        }
    }

    // -------------------------------------------------------------------------
    // isTokenValid
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("isTokenValid")
    class IsTokenValid {

        @Test
        @DisplayName("should return true for a valid non-expired token")
        void isValid_validToken_returnsTrue() {
            String token = buildToken("user-1", "ADMIN", 60_000);

            assertThat(jwtUtil.isTokenValid(token)).isTrue();
        }

        @Test
        @DisplayName("should return false for an expired token")
        void isValid_expiredToken_returnsFalse() {
            String token = buildExpiredToken("user-1");

            assertThat(jwtUtil.isTokenValid(token)).isFalse();
        }

        @Test
        @DisplayName("should return false for a tampered token")
        void isValid_tamperedToken_returnsFalse() {
            String token = buildToken("user-1", "ADMIN", 60_000);
            String tampered = token.substring(0, token.length() - 4) + "XXXX";

            assertThat(jwtUtil.isTokenValid(tampered)).isFalse();
        }

        @Test
        @DisplayName("should return false for a null token")
        void isValid_nullToken_returnsFalse() {
            assertThat(jwtUtil.isTokenValid(null)).isFalse();
        }

        @Test
        @DisplayName("should return false for a blank token")
        void isValid_blankToken_returnsFalse() {
            assertThat(jwtUtil.isTokenValid("   ")).isFalse();
        }
    }

    // -------------------------------------------------------------------------
    // isTokenExpired
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("isTokenExpired")
    class IsTokenExpired {

        @Test
        @DisplayName("should return false for a valid non-expired token")
        void isExpired_validToken_returnsFalse() {
            String token = buildToken("user-1", "ADMIN", 60_000);

            assertThat(jwtUtil.isTokenExpired(token)).isFalse();
        }

        @Test
        @DisplayName("should return true for an expired token")
        void isExpired_expiredToken_returnsTrue() {
            String token = buildExpiredToken("user-1");

            assertThat(jwtUtil.isTokenExpired(token)).isTrue();
        }

        @Test
        @DisplayName("should return true for an invalid/tampered token")
        void isExpired_invalidToken_returnsTrue() {
            assertThat(jwtUtil.isTokenExpired("not.a.token")).isTrue();
        }
    }
}
