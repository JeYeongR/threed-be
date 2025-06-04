package com.example.threedbe.auth.service.client;

import com.example.threedbe.auth.dto.request.OAuthUserInfo;

// 통일된 OAuth 클라이언트 인터페이스: access_token만 리턴

public interface OAuthClient {

	String requestAccessToken(String code);

	OAuthUserInfo requestUserInfo(String accessToken);

}
