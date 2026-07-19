package com.sisa.jihunpage.gallery.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import com.sisa.jihunpage.BackendApplication;
import com.sisa.jihunpage.auth.exception.UnauthenticatedException;
import com.sisa.jihunpage.gallery.exception.GalleryImageStorageException;
import com.sisa.jihunpage.gallery.exception.GalleryPhotoAccessDeniedException;
import com.sisa.jihunpage.gallery.exception.GalleryPhotoNotFoundException;
import com.sisa.jihunpage.gallery.dto.GalleryPhotoCreateRequest;
import com.sisa.jihunpage.gallery.dto.GalleryPhotoResponse;
import com.sisa.jihunpage.gallery.exception.InvalidGalleryImageException;
import com.sisa.jihunpage.gallery.service.GalleryService;

@WebMvcTest(GalleryPhotoController.class)
@ContextConfiguration(classes = BackendApplication.class)
class GalleryPhotoControllerTest {

	private static final String LOGIN_MEMBER_ID =
		"LOGIN_MEMBER_ID";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private GalleryService galleryService;

	@Test
	void createsGalleryPhotoWhenAuthenticated() throws Exception {
		MockMultipartFile image = new MockMultipartFile(
			"image",
			"tokyo.jpg",
			MediaType.IMAGE_JPEG_VALUE,
			"test-image-content".getBytes()
		);

		GalleryPhotoResponse response =
			new GalleryPhotoResponse(
				1L,
				"Tokyo1",
				"도쿄",
				"Tokyo",
				LocalDate.of(2025, 4, 15),
				"/uploads/gallery/test-image.jpg",
				LocalDateTime.of(
					2026,
					7,
					19,
					18,
					0
				)
			);

		given(
			galleryService.createPhoto(
				eq(1L),
				any(GalleryPhotoCreateRequest.class)
			)
		).willReturn(response);

		mockMvc.perform(
				multipart("/api/gallery/photos")
					.file(image)
					.param("title", "Tokyo1")
					.param(
						"description",
						"도쿄"
					)
					.param("location", "Tokyo")
					.param("takenAt", "2025-04-15")
					.sessionAttr(LOGIN_MEMBER_ID, 1L)
			)
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.id").value(1))
			.andExpect(
				jsonPath("$.title")
					.value("Tokyo1")
			)
			.andExpect(
				jsonPath("$.description")
					.value("도쿄")
			)
			.andExpect(
				jsonPath("$.location")
					.value("Tokyo")
			)
			.andExpect(
				jsonPath("$.takenAt")
					.value("2025-04-15")
			)
			.andExpect(
				jsonPath("$.imageUrl")
					.value(
						"/uploads/gallery/test-image.jpg"
					)
			);

		then(galleryService)
			.should()
			.createPhoto(
				eq(1L),
				any(GalleryPhotoCreateRequest.class)
			);
	}

	@Test
	void returnsUnauthorizedWhenNotAuthenticated()
		throws Exception {

		MockMultipartFile image = new MockMultipartFile(
			"image",
			"tokyo1.jpg",
			MediaType.IMAGE_JPEG_VALUE,
			"test-image-content".getBytes()
		);

		given(
			galleryService.createPhoto(
				isNull(),
				any(GalleryPhotoCreateRequest.class)
			)
		).willThrow(new UnauthenticatedException());

		mockMvc.perform(
				multipart("/api/gallery/photos")
					.file(image)
					.param("title", "Tokyo1")
			)
			.andExpect(status().isUnauthorized())
			.andExpect(
				jsonPath("$.code")
					.value("UNAUTHENTICATED")
			)
			.andExpect(
				jsonPath("$.errors").isEmpty()
			);
	}

	@Test
	void returnsBadRequestWhenTitleIsBlank()
		throws Exception {

		MockMultipartFile image = new MockMultipartFile(
			"image",
			"tokyo.jpg",
			MediaType.IMAGE_JPEG_VALUE,
			"test-image-content".getBytes()
		);

		mockMvc.perform(
				multipart("/api/gallery/photos")
					.file(image)
					.param("title", "   ")
					.sessionAttr(LOGIN_MEMBER_ID, 1L)
			)
			.andExpect(status().isBadRequest())
			.andExpect(
				jsonPath("$.code")
					.value("VALIDATION_FAILED")
			)
			.andExpect(
				jsonPath("$.errors.title")
					.value("사진 제목을 입력해 주세요.")
			);

		then(galleryService)
			.shouldHaveNoInteractions();
	}

	@Test
	void deletesGalleryPhotoWhenOwnerIsAuthenticated()
		throws Exception {

		willDoNothing()
			.given(galleryService)
			.deletePhoto(1L, 10L);

		mockMvc.perform(
				delete("/api/gallery/photos/{photoId}", 10L)
					.sessionAttr(LOGIN_MEMBER_ID, 1L)
			)
			.andExpect(status().isNoContent())
			.andExpect(content().string(""));

		then(galleryService)
			.should()
			.deletePhoto(1L, 10L);
	}

	@Test
	void returnsUnauthorizedWhenDeletingWithoutAuthentication()
		throws Exception {

		willThrow(new UnauthenticatedException())
			.given(galleryService)
			.deletePhoto(isNull(), eq(10L));

		mockMvc.perform(
				delete("/api/gallery/photos/{photoId}", 10L)
			)
			.andExpect(status().isUnauthorized())
			.andExpect(
				jsonPath("$.code")
					.value("UNAUTHENTICATED")
			)
			.andExpect(
				jsonPath("$.message")
					.value("로그인이 필요합니다.")
			)
			.andExpect(
				jsonPath("$.errors").isEmpty()
			);

		then(galleryService)
			.should()
			.deletePhoto(isNull(), eq(10L));
	}

	@Test
	void returnsForbiddenWhenDeletingAnotherMembersPhoto()
		throws Exception {

		willThrow(
			new GalleryPhotoAccessDeniedException()
		)
			.given(galleryService)
			.deletePhoto(2L, 10L);

		mockMvc.perform(
				delete("/api/gallery/photos/{photoId}", 10L)
					.sessionAttr(LOGIN_MEMBER_ID, 2L)
			)
			.andExpect(status().isForbidden())
			.andExpect(
				jsonPath("$.code")
					.value(
						"GALLERY_PHOTO_ACCESS_DENIED"
					)
			)
			.andExpect(
				jsonPath("$.message")
					.value(
						"본인의 갤러리 사진만 삭제할 수 있습니다."
					)
			)
			.andExpect(
				jsonPath("$.errors").isEmpty()
			);

		then(galleryService)
			.should()
			.deletePhoto(2L, 10L);
	}

	@Test
	void returnsNotFoundWhenDeletingMissingPhoto()
		throws Exception {

		willThrow(
			new GalleryPhotoNotFoundException(999L)
		)
			.given(galleryService)
			.deletePhoto(1L, 999L);

		mockMvc.perform(
				delete("/api/gallery/photos/{photoId}", 999L)
					.sessionAttr(LOGIN_MEMBER_ID, 1L)
			)
			.andExpect(status().isNotFound())
			.andExpect(
				jsonPath("$.code")
					.value("GALLERY_PHOTO_NOT_FOUND")
			)
			.andExpect(
				jsonPath("$.message")
					.value(
						"갤러리 사진을 찾을 수 없습니다: 999"
					)
			)
			.andExpect(
				jsonPath("$.errors").isEmpty()
			);

		then(galleryService)
			.should()
			.deletePhoto(1L, 999L);
	}

	@Test
	void returnsBadRequestWhenGalleryImageFormatIsInvalid()
		throws Exception {

		MockMultipartFile image = new MockMultipartFile(
			"image",
			"document.txt",
			MediaType.TEXT_PLAIN_VALUE,
			"not-an-image".getBytes()
		);

		given(
			galleryService.createPhoto(
				eq(1L),
				any(GalleryPhotoCreateRequest.class)
			)
		).willThrow(
			new InvalidGalleryImageException(
				"JPEG, PNG, WebP 형식의 이미지만 업로드할 수 있습니다."
			)
		);

		mockMvc.perform(
				multipart("/api/gallery/photos")
					.file(image)
					.param("title", "잘못된 이미지")
					.sessionAttr(LOGIN_MEMBER_ID, 1L)
			)
			.andExpect(status().isBadRequest())
			.andExpect(
				jsonPath("$.code")
					.value("INVALID_GALLERY_IMAGE")
			)
			.andExpect(
				jsonPath("$.message")
					.value(
						"JPEG, PNG, WebP 형식의 이미지만 업로드할 수 있습니다."
					)
			)
			.andExpect(
				jsonPath("$.errors").isEmpty()
			);

		then(galleryService)
			.should()
			.createPhoto(
				eq(1L),
				any(GalleryPhotoCreateRequest.class)
			);
	}

	@Test
	void returnsInternalServerErrorWhenGalleryImageStorageFails()
		throws Exception {

		MockMultipartFile image = new MockMultipartFile(
			"image",
			"tokyo.jpg",
			MediaType.IMAGE_JPEG_VALUE,
			"test-image-content".getBytes()
		);

		given(
			galleryService.createPhoto(
				eq(1L),
				any(GalleryPhotoCreateRequest.class)
			)
		).willThrow(
			new GalleryImageStorageException(
				"갤러리 이미지를 저장하는 중 오류가 발생했습니다."
			)
		);

		mockMvc.perform(
				multipart("/api/gallery/photos")
					.file(image)
					.param("title", "Tokyo Tower")
					.sessionAttr(LOGIN_MEMBER_ID, 1L)
			)
			.andExpect(
				status().isInternalServerError()
			)
			.andExpect(
				jsonPath("$.code")
					.value(
						"GALLERY_IMAGE_STORAGE_FAILED"
					)
			)
			.andExpect(
				jsonPath("$.message")
					.value(
						"갤러리 이미지를 처리하는 중 오류가 발생했습니다."
					)
			)
			.andExpect(
				jsonPath("$.errors").isEmpty()
			);

		then(galleryService)
			.should()
			.createPhoto(
				eq(1L),
				any(GalleryPhotoCreateRequest.class)
			);
	}

	@Test
	void returnsPayloadTooLargeWhenUploadSizeIsExceeded()
		throws Exception {

		MockMultipartFile image = new MockMultipartFile(
			"image",
			"large-image.jpg",
			MediaType.IMAGE_JPEG_VALUE,
			"test-image-content".getBytes()
		);

		given(
			galleryService.createPhoto(
				eq(1L),
				any(GalleryPhotoCreateRequest.class)
			)
		).willThrow(
			new MaxUploadSizeExceededException(
				5L * 1024 * 1024
			)
		);

		mockMvc.perform(
				multipart("/api/gallery/photos")
					.file(image)
					.param("title", "용량 초과 이미지")
					.sessionAttr(LOGIN_MEMBER_ID, 1L)
			)
			.andExpect(
				status().isPayloadTooLarge()
			)
			.andExpect(
				jsonPath("$.code")
					.value("GALLERY_IMAGE_TOO_LARGE")
			)
			.andExpect(
				jsonPath("$.message")
					.value(
						"이미지 파일은 5MB 이하만 업로드할 수 있습니다."
					)
			)
			.andExpect(
				jsonPath("$.errors").isEmpty()
			);
	}
}