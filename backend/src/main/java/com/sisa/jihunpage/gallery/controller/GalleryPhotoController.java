package com.sisa.jihunpage.gallery.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import com.sisa.jihunpage.gallery.dto.GalleryPhotoCreateRequest;
import com.sisa.jihunpage.gallery.dto.GalleryPhotoResponse;
import com.sisa.jihunpage.gallery.service.GalleryService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/gallery/photos")
public class GalleryPhotoController {

	private static final String LOGIN_MEMBER_ID =
		"LOGIN_MEMBER_ID";

	private final GalleryService galleryService;

	public GalleryPhotoController(GalleryService galleryService) {
		this.galleryService = galleryService;
	}

	@PostMapping(
		consumes = MediaType.MULTIPART_FORM_DATA_VALUE
	)
	public ResponseEntity<GalleryPhotoResponse> createPhoto(
		@Valid
		@ModelAttribute
		GalleryPhotoCreateRequest request,

		@SessionAttribute(
			name = LOGIN_MEMBER_ID,
			required = false
		)
		Long authenticatedMemberId
	) {
		GalleryPhotoResponse response = galleryService.createPhoto(
			authenticatedMemberId,
			request
		);
		return ResponseEntity
			.status(HttpStatus.CREATED)
			.body(response);
	}

	@DeleteMapping("{photoId}")
	public ResponseEntity<Void> deletePhoto(
		@PathVariable("photoId")
		Long photoId,

		@SessionAttribute(
			name = LOGIN_MEMBER_ID,
			required = false
		)
		Long authenticatedMemberId
	) {
		galleryService.deletePhoto(
			authenticatedMemberId,
			photoId
		);

		return ResponseEntity
			.noContent()
			.build();
	}
}
