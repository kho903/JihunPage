package com.sisa.jihunpage.gallery.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.sisa.jihunpage.member.domain.Member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "gallery_photos")
public class GalleryPhoto {

	public GalleryPhoto() {
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name="member_id", nullable = false)
	private Member member;

	@Column(nullable = false, length = 100)
	private String title;

	@Column(length = 1000)
	private String description;

	@Column(length = 100)
	private String location;

	@Column(name = "taken_at")
	private LocalDate takenAt;

	@Column(name = "image_url", nullable = false, length = 500)
	private String imageUrl;

	@Column(name = "stored_file_name", nullable = false, length = 255)
	private String storedFileName;

	@Column(name = "original_file_name", nullable = false, length = 255)
	private String originalFileName;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@PrePersist
	private void prePersist() {
		if (createdAt == null) {
			createdAt = LocalDateTime.now();
		}
	}

	public Long getId() {
		return id;
	}

	public Member getMember() {
		return member;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public String getLocation() {
		return location;
	}

	public LocalDate getTakenAt() {
		return takenAt;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public String getStoredFileName() {
		return storedFileName;
	}

	public String getOriginalFileName() {
		return originalFileName;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}
}
