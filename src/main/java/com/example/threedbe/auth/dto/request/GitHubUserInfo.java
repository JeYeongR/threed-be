package com.example.threedbe.auth.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GitHubUserInfo(

	String id,

	String login,

	@JsonProperty("avatar_url") String avatarUrl

) implements OAuthUserInfo {

	@Override
	public String email() {
		return login + "@github.com";
	}

	@Override
	public String name() {
		return login;
	}

	@Override
	public String picture() {
		return avatarUrl;
	}

}
