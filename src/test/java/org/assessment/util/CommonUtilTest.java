package org.assessment.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("CommonUtil Tests")
class CommonUtilTest {

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        RequestContextHolder.resetRequestAttributes();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        RequestContextHolder.resetRequestAttributes();
    }

    /** Helper to bind a MockHttpServletRequest to the RequestContextHolder */
    private MockHttpServletRequest bindRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        return request;
    }

    /** Helper to set a principal in the SecurityContext */
    private void setSecurityPrincipal(String userId, String role) {
        List<SimpleGrantedAuthority> authorities = role != null
                ? List.of(new SimpleGrantedAuthority("ROLE_" + role))
                : List.of();
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userId, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    // -------------------------------------------------------------------------
    // extractUserIdFromRequest
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("extractUserIdFromRequest")
    class ExtractUserId {

        @Test
        @DisplayName("should return userId from SecurityContext principal")
        void returnsUserId_fromSecurityContext() {
            bindRequest();
            setSecurityPrincipal("user-42", "INSTRUCTOR");

            String userId = CommonUtil.extractUserIdFromRequest();

            assertThat(userId).isEqualTo("user-42");
        }

        @Test
        @DisplayName("should fall back to X-User-Id header when SecurityContext has no principal")
        void fallback_toHeader_whenNoSecurityContext() {
            MockHttpServletRequest request = bindRequest();
            request.addHeader("X-User-Id", "user-99");
            // No authentication set in SecurityContext

            String userId = CommonUtil.extractUserIdFromRequest();

            assertThat(userId).isEqualTo("user-99");
        }

        @Test
        @DisplayName("should fall back to X-User-Id header when principal is not a String")
        void fallback_toHeader_whenPrincipalIsNotString() {
            bindRequest().addHeader("X-User-Id", "user-55");
            // Set a non-String principal
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(12345L, null, List.of());
            SecurityContextHolder.getContext().setAuthentication(auth);

            String userId = CommonUtil.extractUserIdFromRequest();

            assertThat(userId).isEqualTo("user-55");
        }

        @Test
        @DisplayName("should throw IllegalStateException when no userId in SecurityContext or header")
        void throws_whenNoPrincipalAndNoHeader() {
            bindRequest(); // no X-User-Id header, no SecurityContext

            assertThatThrownBy(CommonUtil::extractUserIdFromRequest)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("User ID not found in request headers");
        }

        @Test
        @DisplayName("should throw IllegalStateException when X-User-Id header is blank")
        void throws_whenHeaderIsBlank() {
            MockHttpServletRequest request = bindRequest();
            request.addHeader("X-User-Id", "   ");

            assertThatThrownBy(CommonUtil::extractUserIdFromRequest)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("User ID not found in request headers");
        }

        @Test
        @DisplayName("should prefer SecurityContext over header when both are present")
        void prefersSecurityContext_overHeader() {
            MockHttpServletRequest request = bindRequest();
            request.addHeader("X-User-Id", "header-user");
            setSecurityPrincipal("context-user", "ADMIN");

            String userId = CommonUtil.extractUserIdFromRequest();

            assertThat(userId).isEqualTo("context-user");
        }
    }

    // -------------------------------------------------------------------------
    // extractUserRoleFromRequest
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("extractUserRoleFromRequest")
    class ExtractUserRole {

        @Test
        @DisplayName("should return role from SecurityContext without ROLE_ prefix")
        void returnsRole_fromSecurityContext_stripsPrefix() {
            bindRequest();
            setSecurityPrincipal("user-42", "INSTRUCTOR");

            String role = CommonUtil.extractUserRoleFromRequest();

            assertThat(role).isEqualTo("INSTRUCTOR");
        }

        @Test
        @DisplayName("should return authority as-is when it does not start with ROLE_")
        void returnsAuthority_whenNoPrefixToStrip() {
            bindRequest();
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    "user-42", null,
                    List.of(new SimpleGrantedAuthority("ADMIN")));  // no ROLE_ prefix
            SecurityContextHolder.getContext().setAuthentication(auth);

            String role = CommonUtil.extractUserRoleFromRequest();

            assertThat(role).isEqualTo("ADMIN");
        }

        @Test
        @DisplayName("should fall back to X-User-Role header when no SecurityContext authority")
        void fallback_toHeader_whenNoAuthority() {
            MockHttpServletRequest request = bindRequest();
            request.addHeader("X-User-Role", "LEARNER");
            // Authentication with no authorities
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken("user-42", null, List.of());
            SecurityContextHolder.getContext().setAuthentication(auth);

            String role = CommonUtil.extractUserRoleFromRequest();

            assertThat(role).isEqualTo("LEARNER");
        }

        @Test
        @DisplayName("should return null when no SecurityContext and no role header")
        void returnsNull_whenNoRoleAnywhere() {
            bindRequest(); // no header, no security context

            String role = CommonUtil.extractUserRoleFromRequest();

            assertThat(role).isNull();
        }
    }

    // -------------------------------------------------------------------------
    // extractTokenFromRequest
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("extractTokenFromRequest")
    class ExtractToken {

        @Test
        @DisplayName("should return raw token after stripping Bearer prefix")
        void returnsToken_withoutBearerPrefix() {
            MockHttpServletRequest request = bindRequest();
            request.addHeader("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.payload.sig");

            String token = CommonUtil.extractTokenFromRequest();

            assertThat(token).isEqualTo("eyJhbGciOiJIUzI1NiJ9.payload.sig");
        }

        @Test
        @DisplayName("should return null when Authorization header is absent")
        void returnsNull_whenNoAuthorizationHeader() {
            bindRequest();

            String token = CommonUtil.extractTokenFromRequest();

            assertThat(token).isNull();
        }

        @Test
        @DisplayName("should return null when Authorization header does not start with Bearer")
        void returnsNull_whenNoBearerPrefix() {
            MockHttpServletRequest request = bindRequest();
            request.addHeader("Authorization", "Basic dXNlcjpwYXNz");

            String token = CommonUtil.extractTokenFromRequest();

            assertThat(token).isNull();
        }
    }
}
