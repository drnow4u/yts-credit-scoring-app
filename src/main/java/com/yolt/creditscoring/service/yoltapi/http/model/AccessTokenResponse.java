package com.yolt.creditscoring.service.yoltapi.http.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AccessTokenResponse {

    @JsonProperty("access_token")
    String accessToken;

    @JsonProperty("expires_in")
    Long expiresIn;

    @JsonProperty("scope")
    String scope;

    @JsonProperty("token_type")
    String tokenType;
}
