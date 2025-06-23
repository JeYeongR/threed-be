package com.example.threedbe.post.repository.impl;

import static com.example.threedbe.post.domain.QCompanyPost.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import com.example.threedbe.post.domain.Company;
import com.example.threedbe.post.domain.CompanyPost;
import com.example.threedbe.post.domain.Field;
import com.example.threedbe.post.repository.CompanyPostRepositoryCustom;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;

@Repository
public class CompanyPostRepositoryImpl implements CompanyPostRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	public CompanyPostRepositoryImpl(EntityManager em) {
		this.queryFactory = new JPAQueryFactory(em);
	}

	@Override
	public Page<CompanyPost> searchCompanyPosts(
		List<Field> fields,
		List<Company> companies,
		String keyword,
		boolean excludeCompanies,
		Pageable pageable) {

		BooleanBuilder whereClause = new BooleanBuilder().and(companyPost.publishedAt.isNotNull())
			.and(fieldsIn(fields))
			.and(companiesFilter(companies, excludeCompanies))
			.and(keywordContains(keyword));

		List<CompanyPost> content = queryFactory.selectFrom(companyPost)
			.where(whereClause)
			.orderBy(companyPost.publishedAt.desc())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		JPAQuery<Long> countQuery = queryFactory.select(companyPost.count())
			.from(companyPost)
			.where(whereClause);

		return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
	}

	@Override
	public List<Long> findPopularPostIds(LocalDateTime publishedAfter) {
		return queryFactory.select(companyPost.id)
			.from(companyPost)
			.where(companyPost.publishedAt.after(publishedAfter))
			.orderBy(companyPost.viewCount.add(companyPost.bookmarks.size().multiply(2L)).desc())
			.limit(10)
			.fetch();
	}

	@Override
	public Optional<Long> findNextId(LocalDateTime publishedAt) {
		Long postId = queryFactory
			.select(companyPost.id)
			.from(companyPost)
			.where(companyPost.publishedAt.lt(publishedAt))
			.orderBy(companyPost.publishedAt.desc())
			.fetchFirst();

		return Optional.ofNullable(postId);
	}

	@Override
	public Optional<Long> findPreviousId(LocalDateTime publishedAt) {
		Long postId = queryFactory
			.select(companyPost.id)
			.from(companyPost)
			.where(companyPost.publishedAt.gt(publishedAt))
			.orderBy(companyPost.publishedAt.asc())
			.fetchFirst();

		return Optional.ofNullable(postId);
	}

	@Override
	public List<CompanyPost> findPopularPosts(LocalDateTime publishedAfter) {
		return queryFactory
			.selectFrom(companyPost)
			.where(companyPost.publishedAt.after(publishedAfter))
			.orderBy(companyPost.viewCount.add(companyPost.bookmarks.size().multiply(2L)).desc())
			.limit(10)
			.fetch();
	}

	@Override
	public Optional<CompanyPost> findCompanyPostDetailById(Long postId) {
		CompanyPost post = queryFactory
			.selectFrom(companyPost)
			.where(companyPost.id.eq(postId), companyPost.publishedAt.isNotNull())
			.fetchOne();

		return Optional.ofNullable(post);
	}

	private BooleanExpression fieldsIn(List<Field> fields) {
		return fields != null && !fields.isEmpty() ? companyPost.field.in(fields) : null;
	}

	private BooleanExpression companiesFilter(List<Company> companies, boolean exclude) {
		if (companies == null || companies.isEmpty()) {
			return null;
		}

		return exclude ? companyPost.company.notIn(companies) : companyPost.company.in(companies);
	}

	private BooleanExpression keywordContains(String keyword) {
		return keyword != null ?
			companyPost.title.containsIgnoreCase(keyword)
				.or(companyPost.content.containsIgnoreCase(keyword)) : null;
	}

}
