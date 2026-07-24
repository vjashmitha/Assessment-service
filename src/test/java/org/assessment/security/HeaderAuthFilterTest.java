package org.assessment.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@DisplayName("HeaderAuthFilter Tests")
class HeaderAuthFilterTest {

    private HeaderAuthFilter filter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        filter = new HeaderAuthFilter();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = mock(FilterChain.class);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // -------------------------------------------------------------------------
    // Authentication population
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("Authentication population")
    class AuthPopulation {

        @Test
        @DisplayName("should set authentication with userId as principal when X-User-Id present")
        void setsAuthentication_whenUserIdHeaderPresent() throws ServletException, IOException {
            request.addHeader("X-User-Id", "user-42");
            request.addHeader("X-User-Role", "INSTRUCTOR");

            filter.doFilterInternal(request, response, filterChain);

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            assertThat(auth).isNotNull();
            assertThat(auth.getPrincipal()).isEqualTo("user-42");
        }

        @Test
        @DisplayName("should set ROLE_ prefixed authority from X-User-Role header")
        void setsRoleAuthority_withRolePrefix() throws ServletException, IOException {
            request.addHeader("X-User-Id", "user-42");
            request.addHeader("X-User-Role", "LEARNER");

            filter.doFilterInternal(request, response, filterChain);

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            assertThat(auth.getAuthorities()).hasSize(1);
            assertThat(auth.getAuthorities().iterator().next().getAuthority())
                    .isEqualTo("ROLE_LEARNER");
        }

        @Test
        @DisplayName("should uppercase role before prefixing ROLE_")
        void uppercasesRole() throws ServletException, IOException {
            request.addHeader("X-User-Id", "user-42");
            request.addHeader("X-User-Role", "admin");  // lowercase input

            filter.doFilterInternal(request, response, filterChain);

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            assertThat(auth.getAuthorities().iterator().next().getAuthority())
                    .isEqualTo("ROLE_ADMIN");
        }

        @Test
        @DisplayName("should set empty authorities when X-User-Role header is absent")
        void emptyAuthorities_whenRoleHeaderAbsent() throws ServletException, IOException {
            request.addHeader("X-User-Id", "user-42");
            // No X-User-Role header

            filter.doFilterInternal(request, response, filterChain);

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            assertThat(auth).isNotNull();
            assertThat(auth.getAuthorities()).isEmpty();
        }

        @Test
        @DisplayName("should set empty authorities when X-User-Role header is blank")
        void emptyAuthorities_whenRoleHeaderBlank() throws ServletException, IOException {
            request.addHeader("X-User-Id", "user-42");
            request.addHeader("X-User-Role", "   ");

            filter.doFilterInternal(request, response, filterChain);

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            assertThat(auth.getAuthorities()).isEmpty();
        }
    }

    // -------------------------------------------------------------------------
    // No authentication when header missing or blank
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("No authentication set")
    class NoAuth {

        @Test
        @DisplayName("should not set authentication when X-User-Id header is absent")
        void noAuth_whenUserIdAbsent() throws ServletException, IOException {
            // No X-User-Id header at all

            filter.doFilterInternal(request, response, filterChain);

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            assertThat(auth).isNull();
        }

        @Test
        @DisplayName("should not set authentication when X-User-Id header is blank")
        void noAuth_whenUserIdBlank() throws ServletException, IOException {
            request.addHeader("X-User-Id", "   ");

            filter.doFilterInternal(request, response, filterChain);

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            assertThat(auth).isNull();
        }
    }

    // -------------------------------------------------------------------------
    // Filter chain always continues
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("Filter chain continuation")
    class FilterChainContinuation {

        @Test
        @DisplayName("should always call filterChain.doFilter with userId present")
        void alwaysCallsFilterChain_withUserId() throws ServletException, IOException {
            request.addHeader("X-User-Id", "user-42");
            request.addHeader("X-User-Role", "ADMIN");

            filter.doFilterInternal(request, response, filterChain);

            verify(filterChain, times(1)).doFilter(request, response);
        }

        @Test
        @DisplayName("should always call filterChain.doFilter even without userId header")
        void alwaysCallsFilterChain_withoutUserId() throws ServletException, IOException {
            filter.doFilterInternal(request, response, filterChain);

            verify(filterChain, times(1)).doFilter(request, response);
        }
    }
}
