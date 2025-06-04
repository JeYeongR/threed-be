package com.example.threedbe.member.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import com.example.threedbe.auth.domain.RefreshToken;
import com.example.threedbe.bookmark.domain.Bookmark;
import com.example.threedbe.post.domain.Post;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "members")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String nickname;

	@Column(nullable = false)
	private String email;

	@Column(nullable = false)
	private String profileImageUrl;

	@Embedded
	private AuthProvider authProvider;

	@Embedded
	private RefreshToken refreshToken;

	@OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Bookmark> bookmarks;

	private LocalDateTime deletedAt;

	public Member(String nickname, String email, String profileImageUrl, AuthProvider authProvider) {
		this.nickname = nickname;
		this.email = email;
		this.profileImageUrl = profileImageUrl;
		this.authProvider = authProvider;
	}

	public void updateRefreshToken(RefreshToken refreshToken) {
		this.refreshToken = refreshToken;
	}

	public void deleteRefreshToken() {
		this.refreshToken = null;
	}

	public boolean isDeleted() {
		return deletedAt != null;
	}

	public void addBookmark(Post post) {
		Bookmark bookmark = new Bookmark(this, post);
		this.bookmarks.add(bookmark);
		post.getBookmarks().add(bookmark);
	}

	public void removeBookmark(Bookmark bookmark) {
		this.bookmarks.remove(bookmark);
		bookmark.removeMember();
	}

	public String getHardwareNo() {
		return this.authProvider != null ? this.authProvider.getProviderId() : null;
	}

	@Override
	public final boolean equals(Object o) {
		if (!(o instanceof Member member))
			return false;
		return Objects.equals(id, member.id);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(id);
	}

}
