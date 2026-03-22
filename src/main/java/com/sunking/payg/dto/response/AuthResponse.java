package com.sunking.payg.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Authentication token response")
public class AuthResponse {

    private String accessToken;
    private String tokenType;
    private Long expiresIn;
    private String role;
    private String username;
}
