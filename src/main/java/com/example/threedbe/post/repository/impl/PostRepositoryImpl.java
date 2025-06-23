package com.example.threedbe.post.repository.impl;

import static com.example.threedbe.bookmark.domain.QBookmark.*;
import static com.example.threedbe.post.domain.QCompanyPost.*;
import static com.example.threedbe.post.domain.QMemberPost.*;
import static com.example.threedbe.post.domain.QMemberPostSkill.*;
import static com.example.threedbe.post.domain.QPost.*;
import static com.example.threedbe.post.domain.QSkill.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import com.example.threedbe.bookmark.dto.response.BookmarkedPostResponse;
import com.example.threedbe.post.domain.CompanyPost;
import com.example.threedbe.post.domain.MemberPost;
import com.example.threedbe.post.domain.Post;
import com.example.threedbe.post.repository.PostRepositoryCustom;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class PostRepositoryImpl implements PostRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	public PostRepositoryImpl(EntityManager em) {
		this.queryFactory = new JPAQueryFactory(em);
	}

	@Override
	public Optional<Post> findPostById(Long postId) {
		BooleanExpression isNotDeletedMemberPost = JPAExpressions.selectOne()
			.from(memberPost)
			.where(memberPost.id.eq(postId), memberPost.deletedAt.isNull())
			.exists();

		Post foundPost = queryFactory.selectFrom(post)
			.setLockMode(LockModeType.PESSIMISTIC_WRITE)
			.where(
				post.id.eq(postId),
				post.publishedAt.isNotNull(),
				post.instanceOf(MemberPost.class).not().or(isNotDeletedMemberPost))
			.fetchFirst();

		return Optional.ofNullable(foundPost);
	}

	@Override
	public Page<BookmarkedPostResponse> findBookmarkedPostsByMemberId(Long memberId, Pageable pageable) {
		List<Long> postIds = findBookmarkedPostIds(memberId, pageable);

		if (postIds.isEmpty()) {
			return new PageImpl<>(List.of(), pageable, 0);
		}

		Map<Long, BookmarkedPostResponse> responseMap = new HashMap<>();
		fetchCompanyPosts(postIds, responseMap);
		fetchMemberPosts(postIds, responseMap);

		List<BookmarkedPostResponse> responses = postIds.stream()
			.map(responseMap::get)
			.filter(Objects::nonNull)
			.toList();

		JPAQuery<Long> countQuery = createBookmarkedPostsCountQuery(memberId);

		return PageableExecutionUtils.getPage(responses, pageable, countQuery::fetchOne);
	}

	private List<Long> findBookmarkedPostIds(Long memberId, Pageable pageable) {
		return queryFactory.select(post.id)
			.from(post)
			.join(bookmark).on(bookmark.post.eq(post))
			.leftJoin(memberPost).on(post.id.eq(memberPost.id))
			.where(
				bookmark.member.id.eq(memberId),
				post.instanceOf(CompanyPost.class)
					.or(post.instanceOf(MemberPost.class)
						.and(memberPost.deletedAt.isNull())
						.and(memberPost.publishedAt.isNotNull())))
			.orderBy(post.publishedAt.desc())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();
	}

	private void fetchCompanyPosts(List<Long> postIds, Map<Long, BookmarkedPostResponse> responseMap) {
		queryFactory.selectFrom(companyPost)
			.where(companyPost.id.in(postIds))
			.fetch()
			.forEach(companyPost -> responseMap.put(
				companyPost.getId(),
				BookmarkedPostResponse.from(companyPost)));
	}

	private void fetchMemberPosts(List<Long> postIds, Map<Long, BookmarkedPostResponse> responseMap) {
		queryFactory.selectFrom(memberPost)
			.join(memberPost.member).fetchJoin()
			.leftJoin(memberPost.skills, memberPostSkill).fetchJoin()
			.leftJoin(memberPostSkill.skill, skill).fetchJoin()
			.where(memberPost.id.in(postIds))
			.distinct()
			.fetch()
			.forEach(memberPost -> responseMap.put(
				memberPost.getId(),
				BookmarkedPostResponse.from(memberPost)));
	}

	private JPAQuery<Long> createBookmarkedPostsCountQuery(Long memberId) {
		return queryFactory.select(post.countDistinct())
			.from(post)
			.join(bookmark).on(bookmark.post.eq(post))
			.leftJoin(memberPost).on(post.id.eq(memberPost.id))
			.where(
				bookmark.member.id.eq(memberId),
				post.instanceOf(CompanyPost.class)
					.or(post.instanceOf(MemberPost.class)
						.and(memberPost.deletedAt.isNull())
						.and(memberPost.publishedAt.isNotNull())));
	}

}
