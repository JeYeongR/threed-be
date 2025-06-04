package com.example.threedbe.member.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.threedbe.auth.domain.RefreshToken;
import com.example.threedbe.common.dto.PageResponse;
import com.example.threedbe.common.exception.ThreedConflictException;
import com.example.threedbe.common.exception.ThreedNotFoundException;
import com.example.threedbe.common.exception.ThreedUnauthorizedException;
import com.example.threedbe.member.domain.AuthProvider;
import com.example.threedbe.member.domain.Member;
import com.example.threedbe.member.domain.ProviderType;
import com.example.threedbe.member.dto.request.AuthoredPostRequest;
import com.example.threedbe.member.dto.response.AuthoredPostResponse;
import com.example.threedbe.member.repository.MemberRepository;
import com.example.threedbe.post.service.MemberPostService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

	private final MemberRepository memberRepository;
	private final MemberPostService memberPostService;

	public Member findById(Long memberId) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new ThreedNotFoundException("존재하지 않는 회원입니다."));

		if (member.isDeleted()) {
			throw new ThreedUnauthorizedException("탈퇴한 회원입니다.");
		}

		return member;
	}

	@Transactional
	public Member findOrCreate(
		String email,
		String name,
		String profileImageUrl,
		ProviderType providerType,
		String providerId
	) {
		return memberRepository.findByAuthProviderProviderTypeAndAuthProviderProviderId(providerType, providerId)
			.or(() -> {
				Optional<Member> sameEmail = memberRepository.findByEmail(email);
				if (sameEmail.isPresent()) {
					ProviderType existingProvider = sameEmail.get().getAuthProvider().getProviderType();
					throw new ThreedConflictException(String.format(
						"'%s' 이메일은 이미 %s 계정으로 가입되어 있습니다. 해당 소셜로 로그인해주세요.",
						email,
						existingProvider.name()
					));
				}
				AuthProvider authProvider = new AuthProvider(providerType, providerId);
				Member newMember = new Member(authProvider, email, name, profileImageUrl);
				return Optional.of(memberRepository.save(newMember));
			})
			.get();
	}

	public Member findByRefreshToken(RefreshToken refreshToken) {
		return memberRepository.findFirstByRefreshToken(refreshToken)
			.orElseThrow(() -> new ThreedNotFoundException("존재하지 않는 회원입니다."));
	}

	public Optional<Member> findByProviderAndProviderId(ProviderType providerType, String providerId) {
		return memberRepository.findByAuthProviderProviderTypeAndAuthProviderProviderId(providerType, providerId);
	}

	public Optional<Member> findByEmail(String email) {
		return memberRepository.findByEmail(email);
	}

	public PageResponse<AuthoredPostResponse> findAuthoredPosts(Member member, AuthoredPostRequest request) {
		Pageable pageable = request.toPageRequest();
		Page<AuthoredPostResponse> authoredPosts = memberPostService.findAuthoredPosts(member.getId(), pageable);

		return PageResponse.from(authoredPosts);
	}

}
