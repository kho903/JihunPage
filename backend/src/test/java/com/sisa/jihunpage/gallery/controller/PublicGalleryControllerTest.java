package com.sisa.jihunpage.gallery.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.sisa.jihunpage.BackendApplication;
import com.sisa.jihunpage.gallery.dto.GalleryOwnerResponse;
import com.sisa.jihunpage.gallery.dto.GalleryPhotoResponse;
import com.sisa.jihunpage.gallery.dto.MemberGalleryResponse;
import com.sisa.jihunpage.gallery.exception.GalleryOwnerNotFoundException;
import com.sisa.jihunpage.gallery.service.GalleryService;

@WebMvcTest(PublicGalleryController.class)
@ContextConfiguration(classes = BackendApplication.class)
class PublicGalleryControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private GalleryService galleryService;

	@Test
	void returnsPublicMemberGallery() throws Exception {
		GalleryOwnerResponse owner =
			new GalleryOwnerResponse(
				"jihun_01",
				"김지훈"
			);

		GalleryPhotoResponse photo =
			new GalleryPhotoResponse(
				1L,
				"Tokyo Tower",
				"도쿄에서 촬영한 사진",
				"Tokyo",
				LocalDate.of(2023, 4, 15),
				"/uploads/gallery/uuid-image.jpg",
				LocalDateTime.of(
					2026,
					7,
					19,
					18,
					0
				)
			);

		MemberGalleryResponse response =
			new MemberGalleryResponse(
				owner,
				List.of(photo)
			);

		given(
			galleryService.getPublicGallery("jihun_01")
		).willReturn(response);

		mockMvc.perform(
				get(
					"/api/members/{userId}/gallery",
					"jihun_01"
				)
			)
			.andExpect(status().isOk())
			.andExpect(
				jsonPath("$.owner.userid")
					.value("jihun_01")
			)
			.andExpect(
				jsonPath("$.owner.username")
					.value("김지훈")
			)
			.andExpect(
				jsonPath("$.photos.length()")
					.value(1)
			)
			.andExpect(
				jsonPath("$.photos[0].id")
					.value(1)
			)
			.andExpect(
				jsonPath("$.photos[0].title")
					.value("Tokyo Tower")
			)
			.andExpect(
				jsonPath("$.photos[0].description")
					.value("도쿄에서 촬영한 사진")
			)
			.andExpect(
				jsonPath("$.photos[0].location")
					.value("Tokyo")
			)
			.andExpect(
				jsonPath("$.photos[0].takenAt")
					.value("2023-04-15")
			)
			.andExpect(
				jsonPath("$.photos[0].imageUrl")
					.value(
						"/uploads/gallery/uuid-image.jpg"
					)
			);

		then(galleryService)
			.should()
			.getPublicGallery("jihun_01");
	}

	@Test
	void returnsEmptyPhotoListWhenMemberHasNoPhotos()
		throws Exception {

		GalleryOwnerResponse owner =
			new GalleryOwnerResponse(
				"jihun_01",
				"김지훈"
			);

		MemberGalleryResponse response =
			new MemberGalleryResponse(
				owner,
				List.of()
			);

		given(
			galleryService.getPublicGallery("jihun_01")
		).willReturn(response);

		mockMvc.perform(
				get(
					"/api/members/{userId}/gallery",
					"jihun_01"
				)
			)
			.andExpect(status().isOk())
			.andExpect(
				jsonPath("$.owner.userid")
					.value("jihun_01")
			)
			.andExpect(
				jsonPath("$.photos").isArray()
			)
			.andExpect(
				jsonPath("$.photos").isEmpty()
			);

		then(galleryService)
			.should()
			.getPublicGallery("jihun_01");
	}

	@Test
	void returnsNotFoundWhenGalleryOwnerDoesNotExist()
		throws Exception {

		given(
			galleryService.getPublicGallery("unknown_user")
		).willThrow(
			new GalleryOwnerNotFoundException(
				"unknown_user"
			)
		);

		mockMvc.perform(
				get(
					"/api/members/{userId}/gallery",
					"unknown_user"
				)
			)
			.andExpect(status().isNotFound())
			.andExpect(
				jsonPath("$.code")
					.value("GALLERY_OWNER_NOT_FOUND")
			)
			.andExpect(
				jsonPath("$.message")
					.value(
						"갤러리 회원을 찾을 수 없습니다 : unknown_user"
					)
			)
			.andExpect(
				jsonPath("$.errors").isEmpty()
			);

		then(galleryService)
			.should()
			.getPublicGallery("unknown_user");
	}
}