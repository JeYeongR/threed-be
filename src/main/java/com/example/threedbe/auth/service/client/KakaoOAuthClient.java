package com.example.threedbe.auth.service.client;

import java.util.List;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.example.threedbe.auth.config.KakaoOAuthProperties;
import com.example.threedbe.auth.dto.request.KakaoUserInfo;
import com.example.threedbe.auth.dto.response.KakaoTokenResponse;

import lombok.RequiredArgsConstructor;

@Component("KAKAO")
@RequiredArgsConstructor
public class KakaoOAuthClient implements OAuthClient {

	private final RestTemplate restTemplate = new RestTemplate();
	private final KakaoOAuthProperties kakaoOAuthProperties;

	@Override
	public String requestAccessToken(String code) {
		String tokenUri = "https://kauth.kakao.com/oauth/token";

		String requestBody = "grant_type=authorization_code"
			+ "&client_id=" + kakaoOAuthProperties.getClientId()
			+ "&redirect_uri=" + kakaoOAuthProperties.getRedirectUri()
			+ "&code=" + code;

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		headers.setAccept(List.of(MediaType.APPLICATION_JSON));
		HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

		KakaoTokenResponse response = restTemplate.exchange(
			tokenUri,
			HttpMethod.POST,
			request,
			KakaoTokenResponse.class
		).getBody();

		return response.accessToken();
	}

	@Override
	public KakaoUserInfo requestUserInfo(String accessToken) {
		String uri = "https://kapi.kakao.com/v2/user/me";

		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(accessToken);
		HttpEntity<Void> entity = new HttpEntity<>(headers);

		return restTemplate.exchange(
			uri,
			HttpMethod.GET,
			entity,
			KakaoUserInfo.class
		).getBody();
	}

}
