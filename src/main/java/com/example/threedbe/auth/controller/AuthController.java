package com.example.threedbe.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.threedbe.auth.dto.TokenResponse;
import com.example.threedbe.auth.service.AuthService;
import com.example.threedbe.auth.service.OAuthLoginService;
import com.example.threedbe.member.domain.Member;
import com.example.threedbe.member.domain.ProviderType;
import com.example.threedbe.member.dto.response.UserResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Tag(name = "auth-controller", description = "소셜 로그인 및 인증 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

	private final OAuthLoginService oAuthLoginService;
	private final AuthService authService;

	@GetMapping("/google/callback")
	public ResponseEntity<TokenResponse> googleCallback(@RequestParam("code") String code,
		HttpServletResponse response) {
		TokenResponse tokenResponse = oAuthLoginService.login(ProviderType.GOOGLE, code, response);
		return ResponseEntity.ok(tokenResponse);
	}

	@GetMapping("/kakao/callback")
	public ResponseEntity<TokenResponse> kakaoCallback(@RequestParam("code") String code,
		HttpServletResponse response) {
		TokenResponse tokenResponse = oAuthLoginService.login(ProviderType.KAKAO, code, response);
		return ResponseEntity.ok(tokenResponse);
	}

	@GetMapping("/github/callback")
	public ResponseEntity<TokenResponse> githubCallback(@RequestParam("code") String code,
		HttpServletResponse response) {
		TokenResponse tokenResponse = oAuthLoginService.login(ProviderType.GITHUB, code, response);
		return ResponseEntity.ok(tokenResponse);
	}

	@PostMapping("/logout")
	public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
		String accessToken = request.getHeader("Authorization");
		if (accessToken != null) {
			Member member = authService.parseAccessToken(accessToken);
			authService.logout(member, response);
		} else {
			authService.logout(null, response); // 쿠키만 삭제
		}
		return ResponseEntity.ok().build();
	}

	@Operation(summary = "Access Token 재발급", description = "Refresh Token을 이용해 Access Token을 재발급합니다.")
	@PostMapping("/reissue")
	public ResponseEntity<TokenResponse> reissueAccessToken(HttpServletRequest request) {
		String refreshToken = extractCookie(request);
		String newAccessToken = authService.reissueAccessToken(refreshToken);
		Member member = authService.parseAccessToken("Bearer " + newAccessToken);
		return ResponseEntity.ok(new TokenResponse(newAccessToken, UserResponse.from(member)));
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
