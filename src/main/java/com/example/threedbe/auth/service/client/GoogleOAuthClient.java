package com.example.threedbe.auth.service.client;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.example.threedbe.auth.config.GoogleOAuthProperties;
import com.example.threedbe.auth.dto.request.GoogleUserInfo;
import com.example.threedbe.auth.dto.response.GoogleTokenResponse;

import lombok.RequiredArgsConstructor;

@Component("GOOGLE")
@RequiredArgsConstructor
public class GoogleOAuthClient implements OAuthClient {

	private final RestTemplate restTemplate = new RestTemplate();
	private final GoogleOAuthProperties googleOAuthProperties;

	@Override
	public String requestAccessToken(String code) {
		String tokenUri = "https://oauth2.googleapis.com/token";

		String requestBody = "grant_type=authorization_code"
			+ "&client_id=" + googleOAuthProperties.getClientId()
			+ "&client_secret=" + googleOAuthProperties.getClientSecret()
			+ "&redirect_uri=" + googleOAuthProperties.getRedirectUri()
			+ "&code=" + code;

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

		GoogleTokenResponse response = restTemplate.exchange(
			tokenUri,
			HttpMethod.POST,
			request,
			GoogleTokenResponse.class
		).getBody();

		return response.accessToken();
	}

	@Override
	public GoogleUserInfo requestUserInfo(String accessToken) {
		String uri = "https://www.googleapis.com/oauth2/v2/userinfo";

		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(accessToken);
		HttpEntity<Void> entity = new HttpEntity<>(headers);

		return restTemplate.exchange(
			uri,
			HttpMethod.GET,
			entity,
			GoogleUserInfo.class
		).getBody();
	}

}
