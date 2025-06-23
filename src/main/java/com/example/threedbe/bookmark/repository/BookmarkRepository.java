package com.example.threedbe.bookmark.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.threedbe.bookmark.domain.Bookmark;
import com.example.threedbe.member.domain.Member;
import com.example.threedbe.post.domain.Post;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long>, BookmarkRepositoryCustom {

	boolean existsByMemberAndPost(Member member, Post post);

}
