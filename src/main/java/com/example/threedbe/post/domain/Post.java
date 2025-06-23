package com.example.threedbe.post.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.SQLDelete;

import com.example.threedbe.bookmark.domain.Bookmark;
import com.example.threedbe.common.domain.BaseEntity;
import com.example.threedbe.common.exception.ThreedBadRequestException;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "posts", indexes = @Index(name = "idx_posts_published_at_field", columnList = "published_at DESC, field"))
@Entity
@Getter
@SQLDelete(sql = "UPDATE posts SET updated_at = NOW() WHERE id = ?")
@Inheritance(strategy = InheritanceType.JOINED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DiscriminatorColumn(name = "post_type")
public abstract class Post extends BaseEntity {

	private static final int NEW_POST_DAYS_THRESHOLD = 7;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String title;

	@Column(columnDefinition = "TEXT")
	private String content;

	private String thumbnailImageUrl;

	@Enumerated(value = EnumType.STRING)
	private Field field;

	@Column(nullable = false)
	@ColumnDefault("0")
	private int viewCount;

	@Column(nullable = false)
	@ColumnDefault("0")
	private int bookmarkCount;

	@OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Bookmark> bookmarks;

	private LocalDateTime publishedAt;

	public void increaseViewCount() {
		this.viewCount++;
	}

	public void increaseBookmarkCount() {
		this.bookmarkCount++;
	}

	public void decreaseBookmarkCount() {
		this.bookmarkCount--;
	}

	public boolean isNew(LocalDateTime now) {
		return publishedAt.isAfter(now.minusDays(NEW_POST_DAYS_THRESHOLD));
	}

	protected void markAsPublished() {
		this.publishedAt = LocalDateTime.now();
	}

	protected void update(String title, String content, String thumbnailImageUrl, Field field) {
		validateTitle(title);
		validateContent(content);

		this.title = title;
		this.content = content;
		this.thumbnailImageUrl = thumbnailImageUrl;
		this.field = field;
	}

	private void validateTitle(String title) {
		if (title.length() > 100) {
			throw new ThreedBadRequestException("게시글 제목은 100자 이내로 작성해야 합니다.");
		}
	}

	private void validateContent(String content) {
		if (content.length() > 10000) {
			throw new ThreedBadRequestException("게시글 내용은 10,000자 이내로 작성해야 합니다.");
		}
	}

	@Override
	public final boolean equals(Object o) {
		if (!(o instanceof Post post))
			return false;

		return Objects.equals(id, post.id);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(id);
	}

}
