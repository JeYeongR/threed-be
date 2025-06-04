package com.example.threedbe.post.dto.response;

import java.time.LocalDateTime;

import com.example.threedbe.post.domain.CompanyPost;

import io.swagger.v3.oas.annotations.media.Schema;

public record CompanyPostDetailResponse(

	@Schema(description = "포스트 아이디", example = "1")
	long id,

	@Schema(description = "제목", example = "FE News 25년 5월 소식을 전해드립니다!")
	String title,

	@Schema(description = "요약 내용", example = "2025년 5월의 소식에서는 리액트 컴파일러의 RC 단계 도달")
	String content,

	@Schema(description = "썸네일 이미지 주소", example = "https://d2.naver.com/content/images/2023/07/-----------2023-07-06------4-16-49.png")
	String thumbnailImageUrl,

	@Schema(description = "분야", example = "Frontend")
	String field,

	@Schema(description = "저자")
	AuthorResponse author,

	@Schema(description = "조회수", example = "0")
	int viewCount,

	@Schema(description = "생성일", example = "2025-05-08T20:12:14")
	LocalDateTime createdAt,

	@Schema(description = "포스트 원본 링크", example = "https://d2.naver.com/news/7606230")
	String sourceUrl,

	@Schema(description = "북마크수", example = "0")
	int bookmarkCount,

	@Schema(description = "북마크 여부", example = "true")
	boolean isBookmarked,

	@Schema(description = "다음 포스트 아이디", example = "3")
	Long nextId,

	@Schema(description = "이전 포스트 아이디", example = "1")
	Long previousId

) {

	public static CompanyPostDetailResponse from(
		CompanyPost companyPost,
		int bookmarkCount,
		boolean isBookmarked,
		Long nextId,
		Long previousId) {

		return new CompanyPostDetailResponse(
			companyPost.getId(),
			companyPost.getTitle(),
			companyPost.getContent(),
			companyPost.getThumbnailImageUrl(),
			companyPost.getField().getName(),
			AuthorResponse.from(companyPost.getCompany()),
			companyPost.getViewCount(),
			companyPost.getCreatedAt(),
			companyPost.getSourceUrl(),
			bookmarkCount,
			isBookmarked,
			nextId,
			previousId
		);
	}

}
