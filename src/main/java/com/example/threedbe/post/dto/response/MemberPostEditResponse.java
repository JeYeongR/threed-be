package com.example.threedbe.post.dto.response;

import java.util.List;

import com.example.threedbe.post.domain.MemberPost;

import io.swagger.v3.oas.annotations.media.Schema;

public record MemberPostEditResponse(

	@Schema(description = "포스트 아이디", example = "1")
	long id,

	@Schema(description = "제목", example = "FE News 25년 5월 소식을 전해드립니다!")
	String title,

	@Schema(description = "내용", example = "2025년 5월의 소식에서는 리액트 컴파일러의 RC 단계 도달")
	String content,

	@Schema(description = "분야", example = "Frontend")
	String field,

	@Schema(description = "기술들", example = "[\"REACT\", \"JAVASCRIPT\"]")
	List<String> skills,

	@Schema(description = "내 포스트 여부", example = "true")
	boolean isMyPost

) {

	public static MemberPostEditResponse from(MemberPost memberPost, boolean isMyPost) {
		return new MemberPostEditResponse(
			memberPost.getId(),
			memberPost.getTitle(),
			memberPost.getContent(),
			memberPost.getField().getName(),
			memberPost.getSkills()
				.stream()
				.map(skill -> skill.getSkill().getName())
				.toList(),
			isMyPost
		);
	}

}
