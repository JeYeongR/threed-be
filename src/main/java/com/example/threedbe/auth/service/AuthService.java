package com.example.threedbe.auth.service;

import java.util.Map;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.threedbe.auth.domain.AccessToken;
import com.example.threedbe.auth.domain.RefreshToken;
import com.example.threedbe.auth.dto.request.OAuthUserInfo;
import com.example.threedbe.auth.dto.response.ProviderTypeResponse;
import com.example.threedbe.auth.dto.response.TokenResponse;
import com.example.threedbe.auth.service.client.OAuthClient;
import com.example.threedbe.member.domain.Member;
import com.example.threedbe.member.domain.ProviderType;
import com.example.threedbe.member.service.MemberService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final JwtTokenProvider jwtTokenProvider;
	private final MemberService memberService;
	private final Map<String, OAuthClient> oauthClients;

	private static final String REFRESH_TOKEN = "refreshToken";

	public Member parseAccessToken(String rawAccessToken) {
		AccessToken accessToken = new AccessToken(rawAccessToken);
		jwtTokenProvider.validate(accessToken);
		Long memberId = jwtTokenProvider.parseAccessToken(accessToken);

		return memberService.findById(memberId);
	}

	@Transactional
	public TokenResponse login(ProviderType providerType, String code) {
		OAuthClient oAuthClient = oauthClients.get(providerType.name());
		String requestAccessToken = oAuthClient.requestAccessToken(code);
		OAuthUserInfo userInfo = oAuthClient.requestUserInfo(requestAccessToken);

		Member member = memberService.findOrCreate(userInfo, providerType);

		AccessToken accessToken = jwtTokenProvider.createAccessToken(member.getId());
		RefreshToken refreshToken = jwtTokenProvider.createRefreshToken();
		member.login(refreshToken);

		return new TokenResponse(accessToken.getValue(), refreshToken.getValue());
	}

	public String createRefreshTokenCookie(String value) {
		long refreshTokenExpiration = jwtTokenProvider.getRefreshTokenExpiration();

		return ResponseCookie.from(REFRESH_TOKEN, value)
			.path("/")
			.maxAge(refreshTokenExpiration)
			.httpOnly(true)
			.build()
			.toString();
	}

	@Transactional
	public ProviderTypeResponse logout(Member member) {
		member.logout();

		return ProviderTypeResponse.from(member.getAuthProvider().getProviderType());
	}

	public String deleteRefreshTokenCookie() {
		return ResponseCookie.from(REFRESH_TOKEN)
			.path("/")
			.maxAge(0)
			.httpOnly(true)
			.build()
			.toString();
	}

	public TokenResponse reissueAccessToken(String refreshTokenValue) {
		RefreshToken refreshToken = new RefreshToken(refreshTokenValue);
		jwtTokenProvider.validate(refreshToken);
		Member member = memberService.findByRefreshToken(refreshToken);

		AccessToken accessToken = jwtTokenProvider.createAccessToken(member.getId());

		return TokenResponse.from(accessToken);
	}

}
