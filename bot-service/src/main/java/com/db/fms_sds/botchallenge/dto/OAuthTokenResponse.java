package com.db.fms_sds.botchallenge.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Represents response of OAuth Token Endpoint.
 * Example JSON:
 * {
 *   "refresh_token_expires_in": "86399",
 *   "token_type": "Bearer",
 *   "access_token": "...",
 *   "refresh_token": "...",
 *   "expires_in": "1799"
 * }
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OAuthTokenResponse {

    @JsonProperty("refresh_token_expires_in")
    private String refreshTokenExpiresIn;

    @JsonProperty("token_type")
    private String tokenType;

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("expires_in")
    private String expiresIn;
}

