package com.example.threedbe.auth.service.client;

import java.util.List;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.example.threedbe.auth.config.GitHubOAuthProperties;
import com.example.threedbe.auth.dto.request.GitHubUserInfo;
import com.example.threedbe.auth.dto.response.GitHubTokenResponse;

import lombok.RequiredArgsConstructor;

@Component("GITHUB")
@RequiredArgsConstructor
public class GitHubOAuthClient implements OAuthClient {

	private final RestTemplate restTemplate = new RestTemplate();
	private final GitHubOAuthProperties gitHubOAuthProperties;

	@Override
	public String requestAccessToken(String code) {
		String tokenUri = "https://github.com/login/oauth/access_token";

		String requestBody = "client_id=" + gitHubOAuthProperties.getClientId()
			+ "&client_secret=" + gitHubOAuthProperties.getClientSecret()
			+ "&code=" + code;

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		headers.setAccept(List.of(MediaType.APPLICATION_JSON));
		HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

		GitHubTokenResponse response = restTemplate.exchange(
			tokenUri,
			HttpMethod.POST,
			request,
			GitHubTokenResponse.class
		).getBody();

		return response.accessToken();
	}

	@Override
	public GitHubUserInfo requestUserInfo(String accessToken) {
		String uri = "https://api.github.com/user";

		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(accessToken);
		HttpEntity<Void> entity = new HttpEntity<>(headers);

		return restTemplate.exchange(
			uri,
			HttpMethod.GET,
			entity,
			GitHubUserInfo.class
		).getBody();
	}

}
