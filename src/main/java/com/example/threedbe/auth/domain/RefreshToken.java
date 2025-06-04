package com.example.threedbe.auth.domain;

import java.util.Date;

import com.example.threedbe.auth.config.AuthProperties;
import com.example.threedbe.common.exception.ThreedBadRequestException;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken implements JwtToken {

	@Column(name = "refresh_token")
	private String value;

	public RefreshToken(long memberId, AuthProperties authProperties) {
		Date validity = new Date(System.currentTimeMillis() + authProperties.getRefreshExpiration());
		this.value = Jwts.builder()
			.setSubject(String.valueOf(memberId))
			.setExpiration(validity)
			.signWith(SignatureAlgorithm.HS256, authProperties.getRefreshKey())
			.compact();
	}

	public RefreshToken(String rawValue) {
		validate(rawValue);
		this.value = rawValue;
	}

	private void validate(String value) {
		if (value == null || value.trim().isEmpty()) {
			throw new ThreedBadRequestException("잘못된 리프레시 토큰 형식입니다.");
		}
	}

	@Override
	public String getSecretKey(AuthProperties authProperties) {
		return authProperties.getRefreshKey();
	}

}
