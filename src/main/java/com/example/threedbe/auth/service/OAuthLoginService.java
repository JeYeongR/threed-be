package com.example.threedbe.auth.service;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.threedbe.auth.domain.AccessToken;
import com.example.threedbe.auth.domain.RefreshToken;
import com.example.threedbe.auth.dto.TokenResponse;
import com.example.threedbe.auth.dto.userinfo.OAuthUserInfo;
import com.example.threedbe.auth.service.client.OAuthClient;
import com.example.threedbe.member.domain.Member;
import com.example.threedbe.member.domain.ProviderType;
import com.example.threedbe.member.dto.response.UserResponse;
import com.example.threedbe.member.service.MemberService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OAuthLoginService {

	private final Map<String, OAuthClient> oauthClients;
	private final MemberService memberService;
	private final JwtTokenProvider jwtTokenProvider;

	@Transactional
	public TokenResponse login(ProviderType providerType, String code, HttpServletResponse response) {
		OAuthClient oAuthClient = oauthClients.get(providerType.name());
		String accessToken = oAuthClient.requestAccessToken(code);
		OAuthUserInfo userInfo = oAuthClient.requestUserInfo(accessToken);

		Member member = memberService.findOrCreate(
			userInfo.email(),
			userInfo.name(),
			userInfo.picture(),
			providerType,
			userInfo.id()
		);

		AccessToken newAccessToken = jwtTokenProvider.createAccessToken(member.getId());
		RefreshToken refreshToken = jwtTokenProvider.createRefreshToken(member.getId());
		member.updateRefreshToken(refreshToken);

		response.addCookie(createCookie("accessToken", newAccessToken.getValue(), 60 * 60 * 2));
		response.addCookie(createCookie("refreshToken", refreshToken.getValue(), 60 * 60 * 24 * 28));

		UserResponse userResponse = UserResponse.from(member);
		return new TokenResponse(newAccessToken.getValue(), userResponse);
	}

	private Cookie createCookie(String name, String value, int maxAge) {
		Cookie cookie = new Cookie(name, value);
		cookie.setHttpOnly(true);
		cookie.setPath("/");
		cookie.setMaxAge(maxAge);
		return cookie;
	}

}
