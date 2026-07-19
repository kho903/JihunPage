package com.sisa.jihunpage.gallery.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import com.sisa.jihunpage.auth.exception.UnauthenticatedException;
import com.sisa.jihunpage.gallery.domain.GalleryPhoto;
import com.sisa.jihunpage.gallery.dto.GalleryPhotoCreateRequest;
import com.sisa.jihunpage.gallery.dto.GalleryPhotoResponse;
import com.sisa.jihunpage.gallery.exception.GalleryPhotoAccessDeniedException;
import com.sisa.jihunpage.gallery.exception.GalleryPhotoNotFoundException;
import com.sisa.jihunpage.gallery.repository.GalleryPhotoRepository;
import com.sisa.jihunpage.gallery.storage.GalleryImageStorage;
import com.sisa.jihunpage.gallery.storage.StoredGalleryImage;
import com.sisa.jihunpage.member.domain.Member;
import com.sisa.jihunpage.member.repository.MemberRepository;

@ExtendWith(MockitoExtension.class)
class GalleryServiceTest {

	@Mock
	private MemberRepository memberRepository;

	@Mock
	private GalleryPhotoRepository galleryPhotoRepository;

	@Mock
	private GalleryImageStorage galleryImageStorage;

	@InjectMocks
	private GalleryService galleryService;

	@Test
	void createsPhotoForAuthenticatedMember() {
		Member member = mock(Member.class);

		MockMultipartFile image = createImage();

		GalleryPhotoCreateRequest request =
			createPhotoRequest(image);

		StoredGalleryImage storedImage =
			new StoredGalleryImage(
				"uuid-image.jpg",
				"tokyo.jpg",
				"/uploads/gallery/uuid-image.jpg"
			);

		given(memberRepository.findById(1L))
			.willReturn(Optional.of(member));

		given(galleryImageStorage.store(image))
			.willReturn(storedImage);

		given(
			galleryPhotoRepository.saveAndFlush(
				any(GalleryPhoto.class)
			)
		).willAnswer(
			invocation -> invocation.getArgument(0)
		);

		GalleryPhotoResponse response =
			galleryService.createPhoto(1L, request);

		ArgumentCaptor<GalleryPhoto> photoCaptor =
			ArgumentCaptor.forClass(GalleryPhoto.class);

		then(galleryPhotoRepository)
			.should()
			.saveAndFlush(photoCaptor.capture());

		GalleryPhoto savedPhoto =
			photoCaptor.getValue();

		assertSame(member, savedPhoto.getMember());
		assertEquals(
			"Tokyo Tower",
			savedPhoto.getTitle()
		);
		assertEquals(
			"도쿄에서 촬영한 사진",
			savedPhoto.getDescription()
		);
		assertEquals(
			"Tokyo",
			savedPhoto.getLocation()
		);
		assertEquals(
			LocalDate.of(2023, 4, 15),
			savedPhoto.getTakenAt()
		);
		assertEquals(
			"/uploads/gallery/uuid-image.jpg",
			savedPhoto.getImageUrl()
		);
		assertEquals(
			"uuid-image.jpg",
			savedPhoto.getStoredFileName()
		);
		assertEquals(
			"tokyo.jpg",
			savedPhoto.getOriginalFileName()
		);

		assertEquals(
			"Tokyo Tower",
			response.title()
		);
		assertEquals(
			"도쿄에서 촬영한 사진",
			response.description()
		);
		assertEquals(
			"Tokyo",
			response.location()
		);
		assertEquals(
			"/uploads/gallery/uuid-image.jpg",
			response.imageUrl()
		);

		then(galleryImageStorage)
			.should()
			.store(image);
	}

	@Test
	void rejectsPhotoCreationWithoutAuthentication() {
		GalleryPhotoCreateRequest request =
			createPhotoRequest(createImage());

		assertThrows(
			UnauthenticatedException.class,
			() -> galleryService.createPhoto(
				null,
				request
			)
		);

		then(memberRepository)
			.shouldHaveNoInteractions();

		then(galleryPhotoRepository)
			.shouldHaveNoInteractions();

		then(galleryImageStorage)
			.shouldHaveNoInteractions();
	}

	@Test
	void deletesStoredImageWhenDatabaseCreationFails() {
		Member member = mock(Member.class);

		MockMultipartFile image = createImage();

		GalleryPhotoCreateRequest request =
			createPhotoRequest(image);

		StoredGalleryImage storedImage =
			new StoredGalleryImage(
				"uuid-image.jpg",
				"tokyo.jpg",
				"/uploads/gallery/uuid-image.jpg"
			);

		RuntimeException databaseException =
			new RuntimeException("DB 저장 실패");

		given(memberRepository.findById(1L))
			.willReturn(Optional.of(member));

		given(galleryImageStorage.store(image))
			.willReturn(storedImage);

		given(
			galleryPhotoRepository.saveAndFlush(
				any(GalleryPhoto.class)
			)
		).willThrow(databaseException);

		RuntimeException thrownException =
			assertThrows(
				RuntimeException.class,
				() -> galleryService.createPhoto(
					1L,
					request
				)
			);

		assertSame(
			databaseException,
			thrownException
		);

		then(galleryImageStorage)
			.should()
			.delete("uuid-image.jpg");
	}

	@Test
	void deletesPhotoWhenAuthenticatedMemberIsOwner() {
		Member authenticatedMember =
			mock(Member.class);

		GalleryPhoto galleryPhoto =
			mock(GalleryPhoto.class);

		given(authenticatedMember.getId())
			.willReturn(1L);

		given(galleryPhoto.getMember())
			.willReturn(authenticatedMember);

		given(galleryPhoto.getStoredFileName())
			.willReturn("uuid-image.jpg");

		given(memberRepository.findById(1L))
			.willReturn(
				Optional.of(authenticatedMember)
			);

		given(galleryPhotoRepository.findById(10L))
			.willReturn(Optional.of(galleryPhoto));

		galleryService.deletePhoto(1L, 10L);

		InOrder deletionOrder = inOrder(
			galleryPhotoRepository,
			galleryImageStorage
		);

		deletionOrder.verify(
			galleryPhotoRepository
		).delete(galleryPhoto);

		deletionOrder.verify(
			galleryPhotoRepository
		).flush();

		deletionOrder.verify(
			galleryImageStorage
		).delete("uuid-image.jpg");
	}

	@Test
	void rejectsDeletingAnotherMembersPhoto() {
		Member authenticatedMember =
			mock(Member.class);

		Member photoOwner =
			mock(Member.class);

		GalleryPhoto galleryPhoto =
			mock(GalleryPhoto.class);

		given(authenticatedMember.getId())
			.willReturn(2L);

		given(photoOwner.getId())
			.willReturn(1L);

		given(galleryPhoto.getMember())
			.willReturn(photoOwner);

		given(memberRepository.findById(2L))
			.willReturn(
				Optional.of(authenticatedMember)
			);

		given(galleryPhotoRepository.findById(10L))
			.willReturn(Optional.of(galleryPhoto));

		assertThrows(
			GalleryPhotoAccessDeniedException.class,
			() -> galleryService.deletePhoto(
				2L,
				10L
			)
		);

		then(galleryPhotoRepository)
			.should(never())
			.delete(any(GalleryPhoto.class));

		then(galleryPhotoRepository)
			.should(never())
			.flush();

		then(galleryImageStorage)
			.shouldHaveNoInteractions();
	}

	@Test
	void rejectsDeletingMissingPhoto() {
		Member authenticatedMember =
			mock(Member.class);

		given(memberRepository.findById(1L))
			.willReturn(
				Optional.of(authenticatedMember)
			);

		given(galleryPhotoRepository.findById(999L))
			.willReturn(Optional.empty());

		GalleryPhotoNotFoundException exception =
			assertThrows(
				GalleryPhotoNotFoundException.class,
				() -> galleryService.deletePhoto(
					1L,
					999L
				)
			);

		assertEquals(
			"갤러리 사진을 찾을 수 없습니다: 999",
			exception.getMessage()
		);

		then(galleryPhotoRepository)
			.should(never())
			.delete(any(GalleryPhoto.class));

		then(galleryPhotoRepository)
			.should(never())
			.flush();

		then(galleryImageStorage)
			.shouldHaveNoInteractions();
	}

	private MockMultipartFile createImage() {
		return new MockMultipartFile(
			"image",
			"tokyo.jpg",
			MediaType.IMAGE_JPEG_VALUE,
			"test-image-content".getBytes(
				StandardCharsets.UTF_8
			)
		);
	}

	private GalleryPhotoCreateRequest createPhotoRequest(
		MockMultipartFile image
	) {
		GalleryPhotoCreateRequest request =
			new GalleryPhotoCreateRequest();

		request.setImage(image);
		request.setTitle("  Tokyo Tower  ");
		request.setDescription(
			"  도쿄에서 촬영한 사진  "
		);
		request.setLocation("  Tokyo  ");
		request.setTakenAt(
			LocalDate.of(2023, 4, 15)
		);

		return request;
	}
}