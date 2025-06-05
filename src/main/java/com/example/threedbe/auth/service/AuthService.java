package com.example.threedbe.auth.service;

import java.util.Map;

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

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final JwtTokenProvider jwtTokenProvider;
	private final MemberService memberService;
	private final Map<String, OAuthClient> oauthClients;

	public Member parseAccessToken(String rawAccessToken) {
		AccessToken accessToken = new AccessToken(rawAccessToken);
		jwtTokenProvider.validate(accessToken);
		Long memberId = jwtTokenProvider.parseAccessToken(accessToken);

		return memberService.findById(memberId);
	}

	@Transactional
	public TokenResponse login(ProviderType providerType, String code, HttpServletResponse response) {
		OAuthClient oAuthClient = oauthClients.get(providerType.name());
		String requestAccessToken = oAuthClient.requestAccessToken(code);
		OAuthUserInfo userInfo = oAuthClient.requestUserInfo(requestAccessToken);

		Member member = memberService.findOrCreate(userInfo, providerType);

		AccessToken accessToken = jwtTokenProvider.createAccessToken(member.getId());
		RefreshToken refreshToken = jwtTokenProvider.createRefreshToken();
		member.login(refreshToken);

		response.addCookie(createCookie(refreshToken.getValue(), 60 * 60 * 24 * 28));

		return new TokenResponse(accessToken.getValue());
	}

	public String reissueAccessToken(HttpServletRequest request) {
		String refreshTokenValue = extractCookie(request);
		RefreshToken refreshToken = new RefreshToken(refreshTokenValue);
		jwtTokenProvider.validate(refreshToken);
		Member member = memberService.findByRefreshToken(refreshToken);

		return jwtTokenProvider.createAccessToken(member.getId()).getValue();
	}

	@Transactional
	public ProviderTypeResponse logout(Member member, HttpServletResponse response) {
		member.logout();

		response.addCookie(createCookie(null, 0));

		return ProviderTypeResponse.from(member.getAuthProvider().getProviderType());
	}

	private Cookie createCookie(String value, int maxAge) {
		Cookie cookie = new Cookie("refreshToken", value);
		cookie.setHttpOnly(true);
		cookie.setPath("/");
		cookie.setMaxAge(maxAge);
		return cookie;
	}

	private String extractCookie(HttpServletRequest request) {
		if (request.getCookies() == null)
			return null;
		for (Cookie cookie : request.getCookies()) {
			if (cookie.getName().equals("refreshToken")) {
				return cookie.getValue();
			}
		}
		return null;
	}

}
