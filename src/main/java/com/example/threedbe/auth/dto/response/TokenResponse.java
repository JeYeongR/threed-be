package com.example.threedbe.auth.dto.response;

import com.example.threedbe.auth.domain.AccessToken;
import com.example.threedbe.auth.domain.RefreshToken;
import com.fasterxml.jackson.annotation.JsonIgnore;

public record TokenResponse(

	String accessToken,

	@JsonIgnore
	String refreshToken

) {

	public static TokenResponse from(AccessToken accessToken) {
		return new TokenResponse(accessToken.getValue());
	}

	private TokenResponse(String accessToken) {
		this(accessToken, null);
	}

	public static TokenResponse of(AccessToken accessToken, RefreshToken refreshToken) {
		return new TokenResponse(accessToken.getValue(), refreshToken.getValue());
	}

}
