package com.sisa.jihunpage.gallery.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sisa.jihunpage.auth.exception.UnauthenticatedException;
import com.sisa.jihunpage.gallery.domain.GalleryPhoto;
import com.sisa.jihunpage.gallery.dto.GalleryPhotoCreateRequest;
import com.sisa.jihunpage.gallery.dto.GalleryPhotoResponse;
import com.sisa.jihunpage.gallery.dto.MemberGalleryResponse;
import com.sisa.jihunpage.gallery.exception.GalleryOwnerNotFoundException;
import com.sisa.jihunpage.gallery.exception.GalleryPhotoAccessDeniedException;
import com.sisa.jihunpage.gallery.exception.GalleryPhotoNotFoundException;
import com.sisa.jihunpage.gallery.repository.GalleryPhotoRepository;
import com.sisa.jihunpage.gallery.storage.GalleryImageStorage;
import com.sisa.jihunpage.gallery.storage.StoredGalleryImage;
import com.sisa.jihunpage.member.domain.Member;
import com.sisa.jihunpage.member.repository.MemberRepository;

@Service
@Transactional(readOnly = true)
public class GalleryService {

	private final MemberRepository memberRepository;
	private final GalleryPhotoRepository galleryPhotoRepository;
	private GalleryImageStorage galleryImageStorage;

	public GalleryService(MemberRepository memberRepository, GalleryPhotoRepository galleryPhotoRepository,
		GalleryImageStorage galleryImageStorage) {
		this.memberRepository = memberRepository;
		this.galleryPhotoRepository = galleryPhotoRepository;
		this.galleryImageStorage = galleryImageStorage;
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

	@Transactional
	public GalleryPhotoResponse createPhoto(
		Long authenticatedMemberId,
		GalleryPhotoCreateRequest request
	) {
		Member member = findAuthenticatedMember(authenticatedMemberId);

		StoredGalleryImage storedImage = galleryImageStorage.store(request.getImage());

		try {
			GalleryPhoto galleryPhoto = new GalleryPhoto(
				member,
				request.getTitle().trim(),
				normalizeOptionalValue(
					request.getDescription()
				),
				normalizeOptionalValue(
					request.getLocation()
				),
				request.getTakenAt(),
				storedImage.imageUrl(),
				storedImage.storedFileName(),
				storedImage.originalFileName()
			);

			GalleryPhoto savedPhoto =
				galleryPhotoRepository.saveAndFlush(
					galleryPhoto
				);

			return GalleryPhotoResponse.from(savedPhoto);
		} catch (RuntimeException exception) {
			deleteStoredImageAfterFailure(
				storedImage.storedFileName(),
				exception
			);

			throw exception;
		}
	}

	@Transactional
	public void deletePhoto(
		Long authenticatedMemberId,
		Long photoId
	) {
		Member authenticatedMember = findAuthenticatedMember(authenticatedMemberId);

		GalleryPhoto galleryPhoto = galleryPhotoRepository.findById(photoId)
			.orElseThrow(
				() -> new GalleryPhotoNotFoundException(
					photoId
				)
			);

		validatePhotoOwner(
			galleryPhoto,
			authenticatedMember
		);

		String storedFileName = galleryPhoto.getStoredFileName();

		galleryPhotoRepository.delete(galleryPhoto);
		galleryPhotoRepository.flush();

		galleryImageStorage.delete(storedFileName);
	}

	private Member findAuthenticatedMember(
		Long authenticatedMemberId
	) {
		if (authenticatedMemberId == null) {
			throw new UnauthenticatedException();
		}

		return memberRepository.
			findById(authenticatedMemberId)
			.orElseThrow(UnauthenticatedException::new);
	}

	private void validatePhotoOwner(GalleryPhoto galleryPhoto, Member authenticatedMember) {
		Long photoOwnerId = galleryPhoto.getMember().getId();

		if (!photoOwnerId.equals(authenticatedMember.getId())) {
			throw new GalleryPhotoAccessDeniedException();
		}
	}

	private String normalizeOptionalValue(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}

		return value.trim();
	}

	private void deleteStoredImageAfterFailure(
		String storedFileName,
		RuntimeException originalException
	) {
		try {
			galleryImageStorage.delete(storedFileName);
		} catch (RuntimeException cleanupException) {
			originalException.addSuppressed(
				cleanupException
			);
		}
	}
}
