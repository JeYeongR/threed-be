package com.example.threedbe.auth.controller;

import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.threedbe.auth.dto.response.ProviderTypeResponse;
import com.example.threedbe.auth.dto.response.TokenResponse;
import com.example.threedbe.auth.service.AuthService;
import com.example.threedbe.common.annotation.LoginMember;
import com.example.threedbe.member.domain.Member;
import com.example.threedbe.member.domain.ProviderType;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController implements AuthControllerSwagger {

	private final AuthService authService;

	@Override
	@GetMapping("/google/callback")
	public ResponseEntity<TokenResponse> googleCallback(
		@RequestParam("code") String code,
		HttpServletResponse response) {

		TokenResponse tokenResponse = authService.login(ProviderType.GOOGLE, code, response);

		return ResponseEntity.ok(tokenResponse);
	}

	@Override
	@GetMapping("/kakao/callback")
	public ResponseEntity<TokenResponse> kakaoCallback(
		@RequestParam("code") String code,
		HttpServletResponse response) {

		TokenResponse tokenResponse = authService.login(ProviderType.KAKAO, code, response);

		return ResponseEntity.ok(tokenResponse);
	}

	@Override
	@GetMapping("/github/callback")
	public ResponseEntity<TokenResponse> githubCallback(
		@RequestParam("code") String code,
		HttpServletResponse response) {

		TokenResponse tokenResponse = authService.login(ProviderType.GITHUB, code, response);

		return ResponseEntity.ok(tokenResponse);
	}

	@Override
	@PostMapping("/logout")
	public ResponseEntity<ProviderTypeResponse> logout(@LoginMember Member member) {
		ProviderTypeResponse providerTypeResponse = authService.logout(member);

		return ResponseEntity.ok()
			.header("Set-Cookie", createCookie(null, 0).toString())
			.body(providerTypeResponse);
	}

	@Override
	@PostMapping("/reissue")
	public ResponseEntity<TokenResponse> reissueAccessToken(@CookieValue("refreshToken") String refreshToken) {
		TokenResponse tokenResponse = authService.reissueAccessToken(refreshToken);

		return ResponseEntity.ok(tokenResponse);
	}

	private ResponseCookie createCookie(String value, int maxAge) {
		return ResponseCookie.from("refreshToken", value)
			.path("/")
			.maxAge(maxAge)
			.httpOnly(true)
			.build();
	}

}
