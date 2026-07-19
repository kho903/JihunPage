package com.sisa.jihunpage.gallery.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sisa.jihunpage.gallery.domain.GalleryPhoto;
import com.sisa.jihunpage.gallery.dto.MemberGalleryResponse;
import com.sisa.jihunpage.gallery.exception.GalleryOwnerNotFoundException;
import com.sisa.jihunpage.gallery.repository.GalleryPhotoRepository;
import com.sisa.jihunpage.member.domain.Member;
import com.sisa.jihunpage.member.repository.MemberRepository;

@Service
@Transactional(readOnly = true)
public class GalleryService {

	private final MemberRepository memberRepository;
	private final GalleryPhotoRepository galleryPhotoRepository;

	public GalleryService(MemberRepository memberRepository, GalleryPhotoRepository galleryPhotoRepository) {
		this.memberRepository = memberRepository;
		this.galleryPhotoRepository = galleryPhotoRepository;
	}

	public MemberGalleryResponse getPublicGallery(String userId) {
		Member member = memberRepository.findByUserId(userId)
			.orElseThrow(
				() -> new GalleryOwnerNotFoundException(userId)
			);

		List<GalleryPhoto> galleryPhotos = galleryPhotoRepository
			.findAllByMember_IdOrderByCreatedAtDesc(
				member.getId()
			);

		return MemberGalleryResponse.from(
			member,
			galleryPhotos
		);
	}
}
