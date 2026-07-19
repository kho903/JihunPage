package com.sisa.jihunpage.gallery.dto;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class GalleryPhotoCreateRequest {

	@NotNull(message = "업로드할 이미지 파일을 선택해 주세요.")
	private MultipartFile image;

	@NotBlank(message = "사진 제목을 입력해 주세요.")
	@Size(
		max = 100,
		message = "사진 제목은 100자 이하로 입력해 주세요."
	)
	private String title;

	@Size(
		max = 1000,
		message = "사진 설명은 1000자 이하로 입력해 주세요."
	)
	private String description;

	@Size(
		max = 100,
		message = "촬영 장소는 100자 이하로 입력해 주세요."
	)
	private String location;

	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate takenAt;

	public GalleryPhotoCreateRequest() {
	}

	public MultipartFile getImage() {
		return image;
	}

	public void setImage(MultipartFile image) {
		this.image = image;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public LocalDate getTakenAt() {
		return takenAt;
	}

	public void setTakenAt(LocalDate takenAt) {
		this.takenAt = takenAt;
	}
}
