package com.sisa.jihunpage.gallery.storage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.sisa.jihunpage.gallery.exception.GalleryImageStorageException;

@Component
public class LocalGalleryImageStorage implements GalleryImageStorage{


	private static final String PUBLIC_IMAGE_URL_PREFIX =
		"/uploads/gallery/";

	private final GalleryUploadDirectory uploadDirectory;
	private final GalleryImageValidator imageValidator;

	public LocalGalleryImageStorage(
		GalleryUploadDirectory uploadDirectory,
		GalleryImageValidator imageValidator
	) {
		this.uploadDirectory = uploadDirectory;
		this.imageValidator = imageValidator;
	}

	@Override
	public StoredGalleryImage store(MultipartFile image) {
		String extension = imageValidator.validateAndGetExtension(image);

		String storedFileName = UUID.randomUUID() + extension;
		String originalFilename = sanitizeOriginalFilename(image.getOriginalFilename());

		Path targetPath = resolveStoredFile(storedFileName);
		try (InputStream inputStream = image.getInputStream()) {
			Files.copy(inputStream, targetPath);
		} catch (IOException exception) {
			throw new GalleryImageStorageException(
				"갤러리 이미지를 저장하는 중 오류가 발생했습니다.",
				exception
			);
		}
		String imageUrl = PUBLIC_IMAGE_URL_PREFIX + storedFileName;
		return new StoredGalleryImage(
			storedFileName,
			originalFilename,
			imageUrl
		);
	}

	@Override
	public void delete(String storedFileName) {
		Path targetPath = resolveStoredFile(storedFileName);

		try {
			Files.deleteIfExists(targetPath);
		} catch (IOException exception) {
			throw new GalleryImageStorageException(
				"갤러리 이미지를 삭제하는 중 오류가 발생했습니다.",
				exception
			);
		}
	}

	private String sanitizeOriginalFilename(
		String originalFilename
	) {
		if (
			originalFilename == null
				|| originalFilename.isBlank()
		) {
			return "image";
		}

		String normalizedFilename =
			originalFilename.replace("\\", "/");

		String safeFilename =
			normalizedFilename.substring(
				normalizedFilename.lastIndexOf("/") + 1
			);

		if (safeFilename.isBlank()) {
			return "image";
		}

		if (safeFilename.length() > 255) {
			return safeFilename.substring(0, 255);
		}

		return safeFilename;
	}

	private Path resolveStoredFile(String storedFileName) {
		if (
			storedFileName == null
				|| storedFileName.isBlank()
		) {
			throw new GalleryImageStorageException(
				"갤러리 이미지 파일명이 올바르지 않습니다."
			);
		}

		Path targetPath = uploadDirectory
			.getPath()
			.resolve(storedFileName)
			.normalize();

		if (!targetPath.startsWith(uploadDirectory.getPath())) {
			throw new GalleryImageStorageException(
				"잘못된 갤러리 이미지 저장 경로입니다."
			);
		}

		return targetPath;
	}
}
