package com.example.threedbe.bookmark.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.threedbe.bookmark.domain.Bookmark;
import com.example.threedbe.bookmark.dto.request.BookmarkedPostRequest;
import com.example.threedbe.bookmark.dto.response.BookmarkedPostResponse;
import com.example.threedbe.bookmark.repository.BookmarkRepository;
import com.example.threedbe.common.dto.PageResponse;
import com.example.threedbe.common.exception.ThreedConflictException;
import com.example.threedbe.common.exception.ThreedNotFoundException;
import com.example.threedbe.member.domain.Member;
import com.example.threedbe.post.domain.Post;
import com.example.threedbe.post.service.PostService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookmarkService {

	private final BookmarkRepository bookmarkRepository;
	private final PostService postService;

	@Transactional
	public void createBookmark(Member member, Long postId) {
		Post post = postService.findById(postId);

		bookmarkRepository.findFirstByMemberAndPost(member, post)
			.ifPresent(bookmark -> {
				throw new ThreedConflictException("이미 북마크한 포스트입니다.");
			});

		member.addBookmark(post);
		post.increaseBookmarkCount();
	}

	@Transactional
	public void deleteBookmark(Member member, Long postId) {
		Post post = postService.findById(postId);

		Bookmark bookmark = bookmarkRepository.findFirstByMemberAndPost(member, post)
			.orElseThrow(() -> new ThreedNotFoundException("북마크하지 않은 포스트입니다."));

		member.removeBookmark(bookmark);
		post.decreaseBookmarkCount();
	}

	public PageResponse<BookmarkedPostResponse> findBookmarkedPosts(Member member, BookmarkedPostRequest request) {
		Pageable pageable = request.toPageRequest();
		Page<BookmarkedPostResponse> bookmarkedPosts = postService.findBookmarkedPosts(member.getId(), pageable);

		return PageResponse.from(bookmarkedPosts);
	}

}
