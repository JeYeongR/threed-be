package com.example.threedbe.post.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.threedbe.bookmark.repository.BookmarkRepository;
import com.example.threedbe.common.dto.ListResponse;
import com.example.threedbe.common.dto.PageResponse;
import com.example.threedbe.common.exception.ThreedNotFoundException;
import com.example.threedbe.member.domain.Member;
import com.example.threedbe.post.domain.Company;
import com.example.threedbe.post.domain.CompanyPost;
import com.example.threedbe.post.domain.Field;
import com.example.threedbe.post.domain.PopularCondition;
import com.example.threedbe.post.dto.request.CompanyPostPopularRequest;
import com.example.threedbe.post.dto.request.CompanyPostSearchRequest;
import com.example.threedbe.post.dto.response.CompanyPostDetailResponse;
import com.example.threedbe.post.dto.response.CompanyPostResponse;
import com.example.threedbe.post.repository.CompanyPostRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanyPostService {

	private final CompanyPostRepository companyPostRepository;
	private final BookmarkRepository bookmarkRepository;

	public PageResponse<CompanyPostResponse> search(CompanyPostSearchRequest request) {
		List<Field> fields = Field.fromNames(request.fields());
		List<Company> companies = Company.fromNames(request.companies());
		boolean excludeCompanies = companies.contains(Company.ETC);
		String keyword = request.extractKeyword();
		Pageable pageable = request.toPageRequest();
		Page<CompanyPost> resultPage = companyPostRepository.searchCompanyPosts(
			fields,
			excludeCompanies ? Company.filterExcludedCompanies(companies) : companies,
			keyword,
			excludeCompanies,
			pageable);
		if (resultPage.isEmpty()) {
			return PageResponse.from(Page.empty(pageable));
		}

		LocalDateTime now = LocalDateTime.now();
		List<Long> popularPostIds = findPopularPostIds(now);
		Page<CompanyPostResponse> responsePage =
			resultPage.map(post -> toCompanyPostResponse(post, now, popularPostIds));

		return PageResponse.from(responsePage);
	}

	@Transactional
	public CompanyPostDetailResponse findCompanyPostDetail(Member member, Long postId) {
		CompanyPost companyPost = findCompanyPostById(postId);
		companyPost.increaseViewCount();

		int bookmarkCount = companyPost.getBookmarkCount();
		boolean isBookmarked = bookmarkRepository.existsByMemberAndPost(member, companyPost);

		LocalDateTime publishedAt = companyPost.getPublishedAt();
		Long nextId = companyPostRepository.findNextId(publishedAt)
			.orElse(null);
		Long prevId = companyPostRepository.findPreviousId(publishedAt)
			.orElse(null);

		return CompanyPostDetailResponse.from(companyPost, bookmarkCount, isBookmarked, nextId, prevId);
	}

	public ListResponse<CompanyPostResponse> findPopularCompanyPosts(CompanyPostPopularRequest request) {
		PopularCondition condition = PopularCondition.fromName(request.condition());

		LocalDateTime now = LocalDateTime.now();
		LocalDateTime startDate = condition.calculateStartDate(now);

		List<CompanyPost> popularPosts = companyPostRepository.findPopularPosts(startDate);

		List<CompanyPostResponse> responses = popularPosts.stream()
			.map(post -> toCompanyPostResponse(post, now))
			.toList();

		return ListResponse.from(responses);
	}

	public List<Long> findPopularPostIds(LocalDateTime now) {
		return companyPostRepository.findPopularPostIds(PopularCondition.WEEK.calculateStartDate(now));
	}

	private CompanyPostResponse toCompanyPostResponse(CompanyPost post, LocalDateTime now, List<Long> popularPostIds) {
		boolean isNew = post.isNew(now);
		boolean isHot = popularPostIds.contains(post.getId());

		return CompanyPostResponse.from(post, isNew, isHot);
	}

	private CompanyPost findCompanyPostById(Long postId) {
		return companyPostRepository.findCompanyPostDetailById(postId)
			.orElseThrow(() -> new ThreedNotFoundException("회사 포스트가 존재하지 않습니다: " + postId));
	}

	private CompanyPostResponse toCompanyPostResponse(CompanyPost post, LocalDateTime now) {
		boolean isNew = post.isNew(now);

		return CompanyPostResponse.from(post, isNew, true);
	}

}
