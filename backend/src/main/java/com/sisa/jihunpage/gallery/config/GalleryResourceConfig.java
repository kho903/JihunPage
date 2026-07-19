package com.sisa.jihunpage.gallery.config;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class GalleryResourceConfig implements WebMvcConfigurer {

	private static final String GALLERY_URL_PATTERN =
		"/uploads/gallery/**";

	private final String resourceLocation;

	public GalleryResourceConfig(
		@Value("${app.gallery.upload-directory}")
		String uploadDirectory
	) {
		Path galleryUploadPath = Paths.get(uploadDirectory)
			.toAbsolutePath()
			.normalize();

		String location = galleryUploadPath
			.toUri()
			.toString();

		this.resourceLocation = location.endsWith("/")
			? location
			: location + "/";
	}

	@Override
	public void addResourceHandlers(
		ResourceHandlerRegistry registry
	) {
		registry
			.addResourceHandler(GALLERY_URL_PATTERN)
			.addResourceLocations(resourceLocation);
	}
}