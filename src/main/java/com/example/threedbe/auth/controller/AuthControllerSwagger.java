package com.example.threedbe.auth.controller;

import org.springframework.http.ResponseEntity;

import com.example.threedbe.auth.dto.response.ProviderTypeResponse;
import com.example.threedbe.auth.dto.response.TokenResponse;
import com.example.threedbe.common.annotation.SwaggerErrorCode400;
import com.example.threedbe.common.annotation.SwaggerErrorCode401;
import com.example.threedbe.common.annotation.SwaggerErrorCode500;
import com.example.threedbe.member.domain.Member;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;

@Tag(name = "Auth API")
public interface AuthControllerSwagger {

	@Operation(
		summary = "구글 소셜 로그인",
		description = "refreshToken은 쿠키에 저장되며, accessToken은 응답 본문에 포함됩니다.",
		responses = {
			@ApiResponse(
				responseCode = "200",
				description = "구글 소셜 로그인 성공",
				content = @Content(schema = @Schema(implementation = TokenResponse.class))),
		})
	@SwaggerErrorCode400
	@SwaggerErrorCode500
	ResponseEntity<TokenResponse> googleCallback(String code, @Parameter(hidden = true) HttpServletResponse response);

	@Operation(
		summary = "카카오 소셜 로그인",
		description = "refreshToken은 쿠키에 저장되며, accessToken은 응답 본문에 포함됩니다.",
		responses = {
			@ApiResponse(
				responseCode = "200",
				description = "카카오 소셜 로그인 성공",
				content = @Content(schema = @Schema(implementation = TokenResponse.class))),
		})
	@SwaggerErrorCode400
	@SwaggerErrorCode500
	ResponseEntity<TokenResponse> kakaoCallback(String code, @Parameter(hidden = true) HttpServletResponse response);

	@Operation(
		summary = "깃허브 소셜 로그인",
		description = "refreshToken은 쿠키에 저장되며, accessToken은 응답 본문에 포함됩니다.",
		responses = {
			@ApiResponse(
				responseCode = "200",
				description = "깃허브 소셜 로그인 성공",
				content = @Content(schema = @Schema(implementation = TokenResponse.class))),
		})
	@SwaggerErrorCode400
	@SwaggerErrorCode500
	ResponseEntity<TokenResponse> githubCallback(String code, @Parameter(hidden = true) HttpServletResponse response);

	@Operation(
		summary = "로그아웃",
		responses = {
			@ApiResponse(
				responseCode = "200",
				description = "로그아웃 성공",
				content = @Content(schema = @Schema(implementation = ProviderTypeResponse.class))),
		})
	@SwaggerErrorCode400
	@SwaggerErrorCode401
	@SwaggerErrorCode500
	@SecurityRequirement(name = "Authorization")
	ResponseEntity<ProviderTypeResponse> logout(@Parameter(hidden = true) Member member, HttpServletResponse response);

	@Operation(
		summary = "엑세스 토큰 재발급",
		description = "리프레시 토큰을 쿠키에서 추출하여 엑세스 토큰을 재발급합니다. 리프레시 토큰이 유효하지 않거나 만료된 경우 401 에러가 발생합니다.",
		responses = {
			@ApiResponse(
				responseCode = "200",
				description = "엑세스 토큰 재발급 성공",
				content = @Content(schema = @Schema(implementation = TokenResponse.class))),
		})
	@SwaggerErrorCode400
	@SwaggerErrorCode401
	@SwaggerErrorCode500
	@SecurityRequirement(name = "Authorization")
	ResponseEntity<TokenResponse> reissueAccessToken(String refreshToken);

}
