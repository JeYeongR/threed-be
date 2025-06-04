package com.example.threedbe.auth.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KakaoUserInfo(

	String id,

	@JsonProperty("kakao_account") KakaoAccount kakaoAccount

) implements OAuthUserInfo {

	@Override
	public String email() {
		return kakaoAccount.email();
	}

	@Override
	public String name() {
		return kakaoAccount.profile().nickname();
	}

	@Override
	public String picture() {
		return kakaoAccount.profile().profileImageUrl();
	}

	public record KakaoAccount(
		@JsonProperty("email") String email,
		@JsonProperty("profile") KakaoProfile profile
	) {
	}

	public record KakaoProfile(
		@JsonProperty("nickname") String nickname,
		@JsonProperty("profile_image_url") String profileImageUrl
	) {
	}

}
