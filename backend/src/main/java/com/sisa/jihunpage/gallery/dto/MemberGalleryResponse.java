package com.sisa.jihunpage.gallery.dto;

import java.util.List;

import com.sisa.jihunpage.gallery.domain.GalleryPhoto;
import com.sisa.jihunpage.member.domain.Member;

public record MemberGalleryResponse(
	GalleryOwnerResponse owner,
	List<GalleryPhotoResponse> photos
) {

	public static MemberGalleryResponse from(
		Member member,
		List<GalleryPhoto> galleryPhotos
	) {
		GalleryOwnerResponse ownerResponse = GalleryOwnerResponse.from(member);

		List<GalleryPhotoResponse> photoResponses = galleryPhotos.stream()
			.map(GalleryPhotoResponse::from)
			.toList();

		return new MemberGalleryResponse(
			ownerResponse,
			photoResponses
		);
	}
}
