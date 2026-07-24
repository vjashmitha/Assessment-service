package org.assessment.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Constants Tests")
class ConstantsTest {

    @Test
    @DisplayName("API_VERSION should be /api/v1")
    void apiVersion() {
        assertThat(Constants.API_VERSION).isEqualTo("/api/v1");
    }

    @Test
    @DisplayName("ASSIGNMENT_BASE_URL should include API version")
    void assignmentBaseUrl() {
        assertThat(Constants.ASSIGNMENT_BASE_URL).isEqualTo("/api/v1/assignments");
    }

    @Test
    @DisplayName("SUBMISSION_BASE_URL should include API version")
    void submissionBaseUrl() {
        assertThat(Constants.SUBMISSION_BASE_URL).isEqualTo("/api/v1/submissions");
    }

    @Test
    @DisplayName("REVIEW_BASE_URL should include API version")
    void reviewBaseUrl() {
        assertThat(Constants.REVIEW_BASE_URL).isEqualTo("/api/v1/reviews");
    }

    @Test
    @DisplayName("DASHBOARD_BASE_URL should include API version")
    void dashboardBaseUrl() {
        assertThat(Constants.DASHBOARD_BASE_URL).isEqualTo("/api/v1/dashboard");
    }

    @Test
    @DisplayName("REPORT_BASE_URL should include API version")
    void reportBaseUrl() {
        assertThat(Constants.REPORT_BASE_URL).isEqualTo("/api/v1/reports");
    }

    @Test
    @DisplayName("STUDENT_ASSIGNMENTS_URL should include studentId path variable")
    void studentAssignmentsUrl() {
        assertThat(Constants.STUDENT_ASSIGNMENTS_URL)
                .isEqualTo("/api/v1/students/{studentId}/assignments");
    }

    @Test
    @DisplayName("BEARER_PREFIX should be 'Bearer '")
    void bearerPrefix() {
        assertThat(Constants.BEARER_PREFIX).isEqualTo("Bearer ");
    }

    @Test
    @DisplayName("AUTHORIZATION_HEADER should be 'Authorization'")
    void authorizationHeader() {
        assertThat(Constants.AUTHORIZATION_HEADER).isEqualTo("Authorization");
    }

    @Test
    @DisplayName("USER_ID_HEADER should be 'X-User-Id'")
    void userIdHeader() {
        assertThat(Constants.USER_ID_HEADER).isEqualTo("X-User-Id");
    }

    @Test
    @DisplayName("USER_ROLE_HEADER should be 'X-User-Role'")
    void userRoleHeader() {
        assertThat(Constants.USER_ROLE_HEADER).isEqualTo("X-User-Role");
    }

    @Test
    @DisplayName("all URL constants should start with API_VERSION")
    void allUrlsStartWithApiVersion() {
        assertThat(Constants.ASSIGNMENT_BASE_URL).startsWith(Constants.API_VERSION);
        assertThat(Constants.SUBMISSION_BASE_URL).startsWith(Constants.API_VERSION);
        assertThat(Constants.REVIEW_BASE_URL).startsWith(Constants.API_VERSION);
        assertThat(Constants.DASHBOARD_BASE_URL).startsWith(Constants.API_VERSION);
        assertThat(Constants.REPORT_BASE_URL).startsWith(Constants.API_VERSION);
        assertThat(Constants.STUDENT_ASSIGNMENTS_URL).startsWith(Constants.API_VERSION);
    }
}
