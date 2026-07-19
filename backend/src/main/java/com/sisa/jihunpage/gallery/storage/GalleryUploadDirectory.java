package com.sisa.jihunpage.gallery.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.sisa.jihunpage.gallery.exception.GalleryImageStorageException;

@Component
public class GalleryUploadDirectory {

	private final Path path;

	public GalleryUploadDirectory(
		@Value("${app.gallery.upload-directory}")
		String uploadDirectory
	) {
		this.path = Paths.get(uploadDirectory)
			.toAbsolutePath()
			.normalize();

		createDirectory();
	}

	public Path getPath() {
		return path;
	}

	private void createDirectory() {
		try {
			Files.createDirectories(path);
		} catch (IOException exception) {
			throw new GalleryImageStorageException(
				"갤러리 이미지 업로드 폴더를 생성할 수 없습니다.",
				exception
			);
		}
	}
}
