package com.sisa.jihunpage.gallery.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.sisa.jihunpage.gallery.domain.GalleryPhoto;

public record GalleryPhotoResponse(
	Long id,
	String title,
	String description,
	String location,
	LocalDate takenAt,
	String imageUrl,
	LocalDateTime createdAt
) {

	public static GalleryPhotoResponse from(
		GalleryPhoto galleryPhoto
	) {
		return new GalleryPhotoResponse(
			galleryPhoto.getId(),
			galleryPhoto.getTitle(),
			galleryPhoto.getDescription(),
			galleryPhoto.getLocation(),
			galleryPhoto.getTakenAt(),
			galleryPhoto.getImageUrl(),
			galleryPhoto.getCreatedAt()
		);
	}
}
