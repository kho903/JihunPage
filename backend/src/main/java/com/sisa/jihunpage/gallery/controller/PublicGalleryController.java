package com.sisa.jihunpage.gallery.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sisa.jihunpage.gallery.dto.MemberGalleryResponse;
import com.sisa.jihunpage.gallery.service.GalleryService;

@RestController
@RequestMapping("/api/members")
public class PublicGalleryController {

	private final GalleryService galleryService;

	public PublicGalleryController(GalleryService galleryService) {
		this.galleryService = galleryService;
	}

	@GetMapping("/{userId}/gallery")
	public ResponseEntity<MemberGalleryResponse> getPublicGallery(
		@PathVariable("userId") String userId
	) {
		MemberGalleryResponse response =
			galleryService.getPublicGallery(userId);

		return ResponseEntity.ok(response);
	}
}