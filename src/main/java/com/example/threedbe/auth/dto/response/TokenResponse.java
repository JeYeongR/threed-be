package com.example.threedbe.auth.dto.response;

import com.example.threedbe.auth.domain.AccessToken;

public record TokenResponse(

	String accessToken

) {

	public static TokenResponse from(AccessToken accessToken) {
		return new TokenResponse(accessToken.getValue());
	}

}
