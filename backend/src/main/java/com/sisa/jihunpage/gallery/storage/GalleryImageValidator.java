package com.sisa.jihunpage.gallery.storage;

import java.util.Locale;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.sisa.jihunpage.gallery.exception.InvalidGalleryImageException;

@Component
public class GalleryImageValidator {

	private static final long MAX_FILE_SIZE =
		5L * 1024 * 1024;

	private static final Map<String, String> ALLOWED_CONTENT_TYPES =
		Map.of(
			MediaType.IMAGE_JPEG_VALUE, ".jpg",
			MediaType.IMAGE_PNG_VALUE, ".png",
			"image/webp", ".webp"
		);

	public String validateAndGetExtension(
		MultipartFile image
	) {
		validateNotEmpty(image);
		validateFileSize(image);

		return getExtension(image.getContentType());
	}

	private void validateNotEmpty(MultipartFile image) {
		if (image == null || image.isEmpty()) {
			throw new InvalidGalleryImageException(
				"업로드할 이미지 파일을 선택해 주세요."
			);
		}
	}

	private void validateFileSize(MultipartFile image) {
		if (image.getSize() > MAX_FILE_SIZE) {
			throw new InvalidGalleryImageException(
				"이미지 파일은 5MB 이하만 업로드할 수 있습니다."
			);
		}
	}

	private String getExtension(String contentType) {
		String normalizedContentType =
			contentType == null
				? "" : contentType.toLowerCase(Locale.ROOT);

		String extension = ALLOWED_CONTENT_TYPES.get(normalizedContentType);

		if (extension == null) {
			throw new InvalidGalleryImageException(
				"JPEG, PNG, WebP 형식의 이미지만 업로드할 수 있습니다."
			);
		}

		return extension;
	}
}