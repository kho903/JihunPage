package com.sisa.jihunpage.gallery.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
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

import com.sisa.jihunpage.BackendApplication;
import com.sisa.jihunpage.auth.exception.UnauthenticatedException;
import com.sisa.jihunpage.gallery.dto.GalleryPhotoCreateRequest;
import com.sisa.jihunpage.gallery.dto.GalleryPhotoResponse;
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
}