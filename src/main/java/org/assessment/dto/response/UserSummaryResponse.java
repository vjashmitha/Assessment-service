package org.assessment.dto.response;

import lombok.Data;

@Data
public class UserSummaryResponse {
    private String userId;
    private String name;
    private String username;
    private String email;
    private String role;
    private String status;
}