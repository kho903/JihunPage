package com.sisa.jihunpage.gallery.storage;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import com.sisa.jihunpage.gallery.exception.InvalidGalleryImageException;

class LocalGalleryImageStorageTest {

	@TempDir
	Path tempDirectory;

	private LocalGalleryImageStorage imageStorage;

	@BeforeEach
	void setUp() {
		GalleryUploadDirectory uploadDirectory
			= new GalleryUploadDirectory(tempDirectory.toString());

		GalleryImageValidator imageValidator
			= new GalleryImageValidator();

		imageStorage = new LocalGalleryImageStorage(
			uploadDirectory,
			imageValidator
		);
	}

	@Test
	void storesGalleryImage() throws IOException {
		byte[] imageBytes =
			"test-image-content"
				.getBytes(StandardCharsets.UTF_8);

		MockMultipartFile image = new MockMultipartFile(
			"image",
			"tokyo.jpg",
			MediaType.IMAGE_JPEG_VALUE,
			imageBytes
		);

		StoredGalleryImage storedImage =
			imageStorage.store(image);

		Path storedFilePath = tempDirectory.resolve(
			storedImage.storedFileName()
		);

		assertEquals(
			"tokyo.jpg",
			storedImage.originalFileName()
		);

		assertTrue(
			storedImage.storedFileName().endsWith(".jpg")
		);

		assertEquals(
			"/uploads/gallery/"
				+ storedImage.storedFileName(),
			storedImage.imageUrl()
		);

		assertTrue(Files.exists(storedFilePath));

		assertArrayEquals(
			imageBytes,
			Files.readAllBytes(storedFilePath)
		);
	}

	@Test
	void deletesStoredGalleryImage() {
		MockMultipartFile image = new MockMultipartFile(
			"image",
			"tokyo.png",
			MediaType.IMAGE_PNG_VALUE,
			"test-image-content".getBytes(StandardCharsets.UTF_8)
		);

		StoredGalleryImage storedImage =
			imageStorage.store(image);

		Path storedFilePath = tempDirectory.resolve(
			storedImage.storedFileName()
		);

		assertTrue(Files.exists(storedFilePath));

		imageStorage.delete(
			storedImage.storedFileName()
		);

		assertFalse(Files.exists(storedFilePath));
	}

	@Test
	void rejectsEmptyGalleryImage() {
		MockMultipartFile emptyImage =
			new MockMultipartFile(
				"image",
				"",
				MediaType.IMAGE_JPEG_VALUE,
				new byte[0]
			);

		InvalidGalleryImageException exception =
			assertThrows(
				InvalidGalleryImageException.class,
				() -> imageStorage.store(emptyImage)
			);

		assertEquals(
			"업로드할 이미지 파일을 선택해 주세요.",
			exception.getMessage()
		);
	}
}