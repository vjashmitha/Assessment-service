package org.assessment.security;

import feign.RequestTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("FeignAuthInterceptor Tests")
class FeignAuthInterceptorTest {

    private FeignAuthInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new FeignAuthInterceptor();
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    private MockHttpServletRequest bindRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        return request;
    }

    // -------------------------------------------------------------------------
    // apply — header forwarding
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("apply — header forwarding")
    class Apply {

        @Test
        @DisplayName("should forward Authorization header to Feign template")
        void forwardsAuthorizationHeader() {
            MockHttpServletRequest request = bindRequest();
            request.addHeader("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.test.sig");

            RequestTemplate template = new RequestTemplate();
            interceptor.apply(template);

            assertThat(template.headers().get("Authorization"))
                    .isNotNull()
                    .contains("Bearer eyJhbGciOiJIUzI1NiJ9.test.sig");
        }

        @Test
        @DisplayName("should forward X-User-Id header to Feign template")
        void forwardsUserIdHeader() {
            MockHttpServletRequest request = bindRequest();
            request.addHeader("X-User-Id", "user-42");

            RequestTemplate template = new RequestTemplate();
            interceptor.apply(template);

            assertThat(template.headers().get("X-User-Id"))
                    .isNotNull()
                    .contains("user-42");
        }

        @Test
        @DisplayName("should forward X-User-Role header to Feign template")
        void forwardsUserRoleHeader() {
            MockHttpServletRequest request = bindRequest();
            request.addHeader("X-User-Role", "INSTRUCTOR");

            RequestTemplate template = new RequestTemplate();
            interceptor.apply(template);

            assertThat(template.headers().get("X-User-Role"))
                    .isNotNull()
                    .contains("INSTRUCTOR");
        }

        @Test
        @DisplayName("should forward all three headers when all are present")
        void forwardsAllThreeHeaders() {
            MockHttpServletRequest request = bindRequest();
            request.addHeader("Authorization", "Bearer token123");
            request.addHeader("X-User-Id", "user-99");
            request.addHeader("X-User-Role", "ADMIN");

            RequestTemplate template = new RequestTemplate();
            interceptor.apply(template);

            assertThat(template.headers().get("Authorization")).contains("Bearer token123");
            assertThat(template.headers().get("X-User-Id")).contains("user-99");
            assertThat(template.headers().get("X-User-Role")).contains("ADMIN");
        }

        @Test
        @DisplayName("should not add Authorization header when it is absent")
        void doesNotAddAuthHeader_whenAbsent() {
            MockHttpServletRequest request = bindRequest();
            request.addHeader("X-User-Id", "user-42");

            RequestTemplate template = new RequestTemplate();
            interceptor.apply(template);

            assertThat(template.headers().get("Authorization")).isNullOrEmpty();
        }

        @Test
        @DisplayName("should not add X-User-Id header when it is blank")
        void doesNotAddUserIdHeader_whenBlank() {
            MockHttpServletRequest request = bindRequest();
            request.addHeader("X-User-Id", "   ");

            RequestTemplate template = new RequestTemplate();
            interceptor.apply(template);

            assertThat(template.headers().get("X-User-Id")).isNullOrEmpty();
        }

        @Test
        @DisplayName("should not add Authorization header when it is blank")
        void doesNotAddAuthHeader_whenBlank() {
            MockHttpServletRequest request = bindRequest();
            request.addHeader("Authorization", "   ");

            RequestTemplate template = new RequestTemplate();
            interceptor.apply(template);

            assertThat(template.headers().get("Authorization")).isNullOrEmpty();
        }

        @Test
        @DisplayName("should not add X-User-Role header when it is blank")
        void doesNotAddUserRoleHeader_whenBlank() {
            MockHttpServletRequest request = bindRequest();
            request.addHeader("X-User-Role", "   ");

            RequestTemplate template = new RequestTemplate();
            interceptor.apply(template);

            assertThat(template.headers().get("X-User-Role")).isNullOrEmpty();
        }

        @Test
        @DisplayName("should not add X-User-Role header when it is absent")
        void doesNotAddRoleHeader_whenAbsent() {
            bindRequest();

            RequestTemplate template = new RequestTemplate();
            interceptor.apply(template);

            assertThat(template.headers().get("X-User-Role")).isNullOrEmpty();
        }

        @Test
        @DisplayName("should do nothing when no request context is bound")
        void doesNothing_whenNoRequestContext() {
            // No RequestContextHolder bound
            RequestContextHolder.resetRequestAttributes();

            RequestTemplate template = new RequestTemplate();
            interceptor.apply(template); // must not throw

            assertThat(template.headers()).isEmpty();
        }
    }
}
