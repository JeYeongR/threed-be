package com.example.threedbe.post.service;

import java.awt.image.BufferedImage;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.threedbe.bookmark.repository.BookmarkRepository;
import com.example.threedbe.common.dto.ListResponse;
import com.example.threedbe.common.dto.PageResponse;
import com.example.threedbe.common.exception.ThreedBadRequestException;
import com.example.threedbe.common.exception.ThreedNotFoundException;
import com.example.threedbe.common.service.S3Service;
import com.example.threedbe.member.domain.Member;
import com.example.threedbe.member.dto.response.AuthoredPostResponse;
import com.example.threedbe.post.domain.Field;
import com.example.threedbe.post.domain.MemberPost;
import com.example.threedbe.post.domain.PopularCondition;
import com.example.threedbe.post.domain.Skill;
import com.example.threedbe.post.dto.request.MemberPostImageRequest;
import com.example.threedbe.post.dto.request.MemberPostPopularRequest;
import com.example.threedbe.post.dto.request.MemberPostSaveRequest;
import com.example.threedbe.post.dto.request.MemberPostSearchRequest;
import com.example.threedbe.post.dto.request.MemberPostUpdateRequest;
import com.example.threedbe.post.dto.response.MemberPostDetailResponse;
import com.example.threedbe.post.dto.response.MemberPostEditResponse;
import com.example.threedbe.post.dto.response.MemberPostResponse;
import com.example.threedbe.post.dto.response.MemberPostSaveResponse;
import com.example.threedbe.post.dto.response.MemberPostUpdateResponse;
import com.example.threedbe.post.dto.response.PresignedUrlResponse;
import com.example.threedbe.post.repository.MemberPostRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberPostService {

	private final MemberPostRepository memberPostRepository;
	private final BookmarkRepository bookmarkRepository;
	private final SkillService skillService;
	private final ThumbnailService thumbnailService;
	private final S3Service s3Service;

	@Transactional
	public MemberPostSaveResponse saveDraft(Member member) {
		MemberPost memberPost = memberPostRepository.save(new MemberPost(member));

		return MemberPostSaveResponse.from(memberPost);
	}

	@Transactional
	public PresignedUrlResponse generateImageUrl(Member member, Long postId, MemberPostImageRequest request) {
		MemberPost memberPost = findMemberPostById(postId);
		validateAuthor(memberPost, member);

		PresignedUrlResponse presignedUrlResponse = s3Service.generatePresignedUrl(postId, request.fileName());
		memberPost.addImage(presignedUrlResponse.fileUrl());

		return presignedUrlResponse;
	}

	@Transactional
	public MemberPostSaveResponse save(Member member, Long postId, MemberPostSaveRequest request) {
		MemberPost memberPost = findMemberPostById(postId);
		validateAuthor(memberPost, member);
		validateNotPublished(memberPost);

		String title = request.title();
		Field field = Field.fromName(request.field());
		BufferedImage thumbnailImage = thumbnailService.createThumbnailImage(title);
		String thumbnailUrl = s3Service.uploadThumbnailImage(thumbnailImage, title);
		List<Skill> skills = skillService.findOrCreateSkills(request.skills());
		memberPost.publish(title, request.content(), field, thumbnailUrl, skills);

		return MemberPostSaveResponse.from(memberPost);
	}

	public PageResponse<MemberPostResponse> search(MemberPostSearchRequest request) {
		List<Field> fields = Field.fromNames(request.fields());
		List<String> skillNames = request.skills();
		boolean excludeSkillNames = skillNames != null && skillNames.contains(Skill.ETC);
		String keyword = request.extractKeyword();
		Pageable pageable = request.toPageRequest();
		Page<MemberPost> memberPosts = memberPostRepository.searchMemberPosts(
			fields,
			excludeSkillNames ? Skill.filterExcludedSkillNames(skillNames) : skillNames,
			keyword,
			excludeSkillNames,
			pageable);
		if (memberPosts.isEmpty()) {
			return PageResponse.from(Page.empty(pageable));
		}

		LocalDateTime now = LocalDateTime.now();
		List<Long> popularPostIds = findPopularPostIds(now);
		Page<MemberPostResponse> responsePage =
			memberPosts.map(post -> toMemberPostResponse(post, now, popularPostIds));

		return PageResponse.from(responsePage);
	}

	@Transactional
	public MemberPostDetailResponse findMemberPostDetail(Member member, Long postId) {
		MemberPost memberPost = findMemberPostDetailById(postId);
		memberPost.increaseViewCount();

		int bookmarkCount = memberPost.getBookmarkCount();
		boolean isBookmarked = bookmarkRepository.existsByMemberAndPost(member, memberPost);
		boolean isMyPost = memberPost.isAuthor(member);

		LocalDateTime publishedAt = memberPost.getPublishedAt();
		Long nextId = memberPostRepository.findNextId(publishedAt)
			.orElse(null);
		Long prevId = memberPostRepository.findPreviousId(publishedAt)
			.orElse(null);

		return MemberPostDetailResponse.from(memberPost, bookmarkCount, isBookmarked, isMyPost, nextId, prevId);
	}

	public MemberPostEditResponse findMemberPostForEdit(Member member, Long postId) {
		MemberPost memberPost = findMemberPostDetailById(postId);
		validateAuthor(memberPost, member);
		boolean isMyPost = memberPost.isAuthor(member);

		return MemberPostEditResponse.from(memberPost, isMyPost);
	}

	public ListResponse<MemberPostResponse> findPopularMemberPosts(MemberPostPopularRequest request) {
		PopularCondition condition = PopularCondition.fromName(request.condition());

		LocalDateTime now = LocalDateTime.now();
		LocalDateTime startDate = condition.calculateStartDate(now);

		List<MemberPost> popularPosts = memberPostRepository.findPopularPosts(startDate);

		List<MemberPostResponse> posts = popularPosts.stream()
			.map(post -> toMemberPostResponse(post, now))
			.toList();

		return ListResponse.from(posts);
	}

	@Transactional
	public MemberPostUpdateResponse update(Member member, Long postId, MemberPostUpdateRequest request) {
		MemberPost memberPost = findMemberPostDetailById(postId);
		validateAuthor(memberPost, member);
		validatePublished(memberPost);

		String newTitle = request.title();
		Field field = Field.fromName(request.field());
		String thumbnailUrl = memberPost.getThumbnailImageUrl();
		if (memberPost.isTitleChanged(newTitle)) {
			s3Service.deleteThumbnail(thumbnailUrl);
			BufferedImage thumbnailImage = thumbnailService.createThumbnailImage(newTitle);
			thumbnailUrl = s3Service.uploadThumbnailImage(thumbnailImage, newTitle);
		}
		Set<String> currentSkillNames = memberPost.getSkillNames();
		Set<String> requestSkillNames = request.skills();
		List<Skill> skills = currentSkillNames.equals(requestSkillNames)
			? memberPost.getExistingSkills()
			: skillService.findOrCreateSkills(requestSkillNames);
		memberPost.update(newTitle, request.content(), field, thumbnailUrl, skills);

		return MemberPostUpdateResponse.from(memberPost);
	}

	@Transactional
	public void delete(Member member, Long postId) {
		MemberPost memberPost = findMemberPostDetailById(postId);
		validateAuthor(memberPost, member);
		validatePublished(memberPost);

		memberPostRepository.delete(memberPost);
	}

	public Page<AuthoredPostResponse> findAuthoredPosts(Long memberId, Pageable pageable) {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime startDate = PopularCondition.WEEK.calculateStartDate(now);

		Page<MemberPost> memberPosts = memberPostRepository.findMemberPostsByMemberId(memberId, pageable);
		if (memberPosts.isEmpty()) {
			return Page.empty(pageable);
		}

		List<Long> popularPostIds = findPopularPostIds(startDate);

		return memberPosts.map(post -> toAuthoredPostResponse(post, now, popularPostIds));
	}

	public List<Long> findPopularPostIds(LocalDateTime now) {
		return memberPostRepository.findPopularPostIds(PopularCondition.WEEK.calculateStartDate(now));
	}

	private void validateAuthor(MemberPost post, Member member) {
		if (post.isNotAuthor(member)) {
			throw new ThreedBadRequestException("회원 포스트 작성자가 아닙니다: " + post.getId());
		}
	}

	private void validateNotPublished(MemberPost memberPost) {
		if (memberPost.isPublished()) {
			throw new ThreedBadRequestException("이미 출판된 포스트입니다: " + memberPost.getId());
		}
	}

	private void validatePublished(MemberPost memberPost) {
		if (memberPost.isNotPublished()) {
			throw new ThreedBadRequestException("출판 전 포스트입니다: " + memberPost.getId());
		}
	}

	private MemberPostResponse toMemberPostResponse(MemberPost post, LocalDateTime now, List<Long> popularPostIds) {
		boolean isNew = post.isNew(now);
		boolean isHot = popularPostIds.contains(post.getId());

		return MemberPostResponse.from(post, isNew, isHot);
	}

	private MemberPost findMemberPostById(Long postId) {
		return memberPostRepository.findMemberPostById(postId)
			.orElseThrow(() -> new ThreedNotFoundException("회원 포스트가 존재하지 않습니다: " + postId));
	}

	private MemberPost findMemberPostDetailById(Long postId) {
		return memberPostRepository.findMemberPostDetailById(postId)
			.orElseThrow(() -> new ThreedNotFoundException("회사 포스트가 존재하지 않습니다: " + postId));
	}

	private MemberPostResponse toMemberPostResponse(MemberPost post, LocalDateTime now) {
		boolean isNew = post.isNew(now);

		return MemberPostResponse.from(post, isNew, true);
	}

	private AuthoredPostResponse toAuthoredPostResponse(
		MemberPost post,
		LocalDateTime now,
		List<Long> popularPostIds) {

		boolean isNew = post.isNew(now);
		boolean isHot = popularPostIds.contains(post.getId());

		return AuthoredPostResponse.from(post, isNew, isHot);
	}

}
