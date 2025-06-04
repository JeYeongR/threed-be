package com.example.threedbe.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GitHubTokenResponse(

	@JsonProperty("access_token") String accessToken,

	@JsonProperty("token_type") String tokenType,

	String scope

) {
}
