package org.assessment.util;

public class Constants {
    private Constants() {}

    public static final String API_VERSION = "/api/v1";
    public static final String ASSIGNMENT_BASE_URL = API_VERSION + "/assignments";
    public static final String SUBMISSION_BASE_URL = API_VERSION + "/submissions";
    public static final String REVIEW_BASE_URL = API_VERSION + "/reviews";
    public static final String DASHBOARD_BASE_URL = API_VERSION + "/dashboard";
    public static final String REPORT_BASE_URL = API_VERSION + "/reports";

    public static final String BEARER_PREFIX = "Bearer ";
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String USER_ID_HEADER = "X-User-Id";
    public static final String USER_ROLE_HEADER = "X-User-Role";
}
