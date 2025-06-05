package com.example.threedbe.auth.dto.response;

import com.example.threedbe.member.domain.ProviderType;

public record ProviderTypeResponse(

	String providerType

) {

	public static ProviderTypeResponse from(ProviderType providerType) {
		return new ProviderTypeResponse(providerType.name());
	}

}
