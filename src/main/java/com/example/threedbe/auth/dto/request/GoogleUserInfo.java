package com.example.threedbe.auth.dto.request;

public record GoogleUserInfo(

	String id,

	String email,

	String name,

	String picture

) implements OAuthUserInfo {
}
